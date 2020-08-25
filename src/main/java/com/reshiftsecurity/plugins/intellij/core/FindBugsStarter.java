/*
 * Copyright 2020 Reshift Security Intellij plugin contributors
 *
 * This file is part of Reshift Security Intellij plugin.
 *
 * Reshift Security Intellij plugin is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Reshift Security Intellij plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Reshift Security Intellij plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.reshiftsecurity.plugins.intellij.core;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.*;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.ToolWindowImpl;
import com.intellij.util.Consumer;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.messages.AnalysisAbortingListener;
import com.reshiftsecurity.plugins.intellij.messages.MessageBusManager;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.config.*;
import org.dom4j.DocumentException;
import org.jetbrains.annotations.*;
import com.reshiftsecurity.plugins.intellij.common.EventDispatchThreadHelper;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.gui.common.BalloonTipFactory;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;
import com.reshiftsecurity.plugins.intellij.plugins.PluginLoader;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FindBugsStarter implements AnalysisAbortingListener {

	private static final Logger LOGGER = Logger.getInstance(FindBugsStarter.class);

	@NotNull
	private final Project project;

	@NotNull
	private final String _title;

	@NotNull
	private final ProjectSettings projectSettings;

	@NotNull
	private final WorkspaceSettings workspaceSettings;

	private final boolean startProgressInBackground;

	private final boolean startProgressModal;

	private final AtomicBoolean _cancellingByUser;


	public FindBugsStarter(
			@NotNull final Project project,
			@NotNull final String title
	) {
		this(project, title, ProgressStartType.RunInBackgroundFromSettings);
	}


	public FindBugsStarter(
			@NotNull final Project project,
			@NotNull final String title,
			@NotNull final ProgressStartType progressStartType
	) {
		this.project = project;
		_title = title;
		this.projectSettings = ProjectSettings.getInstance(project);
		this.workspaceSettings = WorkspaceSettings.getInstance(project);
		switch (progressStartType) {
			case RunInBackgroundFromSettings:
				startProgressInBackground = workspaceSettings.runInBackground;
				startProgressModal = false;
				break;
			case RunInBackground:
				startProgressInBackground = true;
				startProgressModal = false;
				break;
			case Modal:
				startProgressInBackground = false;
				startProgressModal = true;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported " + progressStartType);
		}
		_cancellingByUser = new AtomicBoolean();
		MessageBusManager.subscribe(project, this, AnalysisAbortingListener.TOPIC, this);
	}

	protected boolean isCompileBeforeAnalyze() {
		return workspaceSettings.compileBeforeAnalyze;
	}

	public final void start() {
		EventDispatchThreadHelper.checkEDT();
		if (isCompileBeforeAnalyze()) {
			final boolean isAnalyzeAfterCompile = workspaceSettings.analyzeAfterCompile;
			final CompilerManager compilerManager = CompilerManager.getInstance(project);
			createCompileScope(compilerManager, compileScope -> {
				if (compileScope != null) {
					finalizeCompileScope(compileScope);
					compilerManager.make(compileScope, (aborted, errors, warnings, compileContext) -> {
						if (!aborted && errors == 0 && !isAnalyzeAfterCompile) {
							EventDispatchThreadHelper.checkEDT(); // see javadoc of CompileStatusNotification
							// Compiler can cause dumb mode, and finished() is invoked inside.
							// We need to continue outside dumb mode to make activateToolWindow work f. e.
							DumbService.getInstance(project).runWhenSmart(() -> {
								EventDispatchThreadHelper.checkEDT();
								startImpl(true);
							});
						}
					});
				}
			});
		} else {
			startImpl(false);
		}
	}

	private void startImpl(final boolean justCompiled) {
		MessageBusManager.publishAnalysisStarted(project);

		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null) {
			throw new IllegalStateException("No Reshift ToolWindow");
		}
		/*
		 * Important: Make sure the tool window is initialized.
		 * This call is to important to make it just in case of false = toolWindowToFront
		 * because we have no guarantee that activateToolWindow works.
		 */
		((ToolWindowImpl) toolWindow).ensureContentInitialized();
		if (workspaceSettings.toolWindowToFront) {
			ToolWindowPanel.showWindow(toolWindow);
		}

		final Task task;
		if (startProgressModal) {
			task = new Task.Modal(project, _title, true) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					asyncStart(indicator, justCompiled);
				}
			};
		} else {
			task = new Task.Backgroundable(project, _title, true) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					asyncStart(indicator, justCompiled);
				}

				@Override
				public boolean shouldStartInBackground() {
					return startProgressInBackground;
				}
			};
		}
		task.queue();
	}

	private void asyncStart(@NotNull final ProgressIndicator indicator, final boolean justCompiled) {
		indicator.setIndeterminate(true);
		indicator.setText("Configure Reshift...");
		try {
			asyncStartImpl(indicator, justCompiled);
		} catch (final ProcessCanceledException ignore) {
			MessageBusManager.publishAnalysisAbortedToEDT(project);
		}
	}

	private void asyncStartImpl(@NotNull final ProgressIndicator indicator, final boolean justCompiled) {

		final FindBugsProjects projects = new FindBugsProjects(project);

		boolean canceled = !ApplicationManager.getApplication().runReadAction(
				(Computable<Boolean>) () -> configure(indicator, projects, justCompiled));

		final FindBugsResult result = new FindBugsResult();
		Throwable error = null;

		if (!canceled) {
			try {
				int numClassesOffset = 0;
				for (final Map.Entry<Module, FindBugsProject> entry : projects.getProjects().entrySet()) {
					final FindBugsProject findBugsProject = entry.getValue();
					final Module module = entry.getKey();
					indicator.setText("Start security analysis of " + findBugsProject.getProjectName());
					final Pair<SortedBugCollection, Reporter> data = executeImpl(indicator, module, findBugsProject, numClassesOffset);
					final int numClasses = data.getSecond().getProjectStats().getNumClasses();
					numClassesOffset += numClasses;
					result.put(findBugsProject, data.getFirst());
					if (data.getSecond().isCanceled()) {
						canceled = true;
						break;
					}
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final Throwable e) {
				error = e;
			}
		}

		if (canceled) {
			MessageBusManager.publishAnalysisAbortedToEDT(project);
		} else {
			MessageBusManager.publishAnalysisFinishedToEDT(project, result, error);
		}
	}

	private Pair<SortedBugCollection, Reporter> executeImpl(
			@NotNull final ProgressIndicator indicator,
			@NotNull final Module module,
			@NotNull final FindBugsProject findBugsProject,
			final int analyzedClassCountOffset
	) throws IOException, InterruptedException {

		AnalyticsService.getInstance().recordAction(AnalyticsAction.START_SCAN);

		final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
		AbstractSettings settings = new ProjectSettings();
		PluginSettings findSecBugsPlugin = new PluginSettings();
		findSecBugsPlugin.id = "com.h3xstream.findsecbugs";
		findSecBugsPlugin.bundled = true;
		findSecBugsPlugin.enabled = true;
		settings.plugins.add(findSecBugsPlugin);
		String importFilePathKey = WorkspaceSettings.PROJECT_IMPORT_FILE_PATH_KEY;
		if (moduleSettings.overrideProjectSettings) {
			settings = moduleSettings;
			importFilePathKey = module.getName();
		}

		final String importFilePath = WorkspaceSettings.getInstance(project).importFilePath.get(importFilePathKey);
		if (!StringUtil.isEmptyOrSpaces(importFilePath)) {
			final boolean success = RuntimeSettingsImporter.importSettings(project, module, settings, importFilePath, importFilePathKey);
			/*
			 * Do continue analysis on import settings failure, but invalidate plugin state
			 * on success because the plugins settings can change anytime.
			 */
			if (success) {
				PluginLoader.invalidate();
			}
		}

		if (!PluginLoader.load(project, moduleSettings.overrideProjectSettings ? module : null, settings, true)) {
			throw new ProcessCanceledException();
		}

		final DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();

		final ProjectFilterSettings projectFilterSettings;
		final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		{
			userPrefs.setEffort(settings.analysisEffort);
			projectFilterSettings = userPrefs.getFilterSettings();
			projectFilterSettings.setMinRank(settings.minRank);
			projectFilterSettings.setMinPriority(settings.minPriority);

			for (final String category : DetectorFactoryCollection.instance().getBugCategories()) {
				projectFilterSettings.removeCategory(category);
				projectFilterSettings.addCategory(category);
			}
			for (final String category : settings.hiddenBugCategory) {
				projectFilterSettings.removeCategory(category);
			}

			userPrefs.setIncludeFilterFiles(new HashMap<>(settings.includeFilterFiles));
			userPrefs.setExcludeBugsFiles(new HashMap<>(settings.excludeBugsFiles));
			userPrefs.setExcludeFilterFiles(new HashMap<>(settings.excludeFilterFiles));

			configureDetectors(settings.detectors, detectorFactoryCollection, userPrefs);
			for (final PluginSettings pluginSettings : settings.plugins) {
				configureDetectors(pluginSettings.detectors, detectorFactoryCollection, userPrefs);
			}
		}

		final SortedBugCollection bugCollection = new SortedBugCollection(findBugsProject);

		final Reporter reporter = new Reporter(
				project,
				module,
				bugCollection,
				projectFilterSettings,
				indicator,
				_cancellingByUser,
				analyzedClassCountOffset
		);

		reporter.setPriorityThreshold(userPrefs.getUserDetectorThreshold());
		reporter.setRankThreshold(projectFilterSettings.getMinRank());

		final FindBugs2 engine = new FindBugs2();
		{
			engine.setNoClassOk(true);
			engine.setMergeSimilarWarnings(false);
			engine.setBugReporter(reporter);
			engine.setProject(findBugsProject);
			engine.setProgressCallback(reporter);
			configureFilter(engine, userPrefs);
			engine.setDetectorFactoryCollection(detectorFactoryCollection);
			engine.setUserPreferences(userPrefs);
		}

		try {
			engine.execute();
		} finally {
			engine.dispose();
		}

		bugCollection.setTimestamp(System.currentTimeMillis());

		AnalyticsService.getInstance().recordMetric(AnalyticsAction.SCAN_RESULTS_METRIC, bugCollection.getCollection().size());

		SecurityReportService.getInstance(project).addBugCollection(bugCollection);

		return Pair.create(bugCollection, reporter);
	}

	protected abstract void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer);

	@NotNull
	protected final CompileScope createFilesCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Collection<VirtualFile> files) {
		return createFilesCompileScope(compilerManager, files.toArray(VirtualFile.EMPTY_ARRAY));
	}

	@NotNull
	protected final CompileScope createFilesCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final VirtualFile[] files) {
		final CompileScope facetsCompileScope = createFacetsCompileScope(compilerManager, files);
		if (facetsCompileScope != null) {
			return facetsCompileScope;
		}
		return compilerManager.createFilesCompileScope(files);
	}

	/**
	 * It seems that createFilesCompileScope does not work in all environments, at least with Android, see
	 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/207521525-CompilerManager-make-does-not-all-tasks-in-case-of-a-Android-module-project
	 * <p>
	 * We choose a very defensive solution and use createModulesCompileScope when we detect a facet.
	 */
	@Nullable
	private CompileScope createFacetsCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final VirtualFile[] files) {
		final Set<Module> modules = New.set();
		boolean facetsFound = false;
		for (final VirtualFile file : files) {
			final Module module = ModuleUtilCore.findModuleForFile(file, project);
			if (module != null) {
				modules.add(module);
				if (!facetsFound) {
					facetsFound = hasFacets(module);
				}
			}
		}
		if (facetsFound) {
			return compilerManager.createModulesCompileScope(modules.toArray(Module.EMPTY_ARRAY), true, true);
		}
		return null;
	}

	/**
	 * Depend on selected configuration is creepy but no other way to make it work with Android, see
	 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/207521525-CompilerManager-make-does-not-all-tasks-in-case-of-a-Android-module-project
	 * <p>
	 * Maybe, sometime, we should make this more clever and search for the best configuration instead of just use the selected.
	 */
	private void finalizeCompileScope(@NotNull final CompileScope compileScope) {
		final RunnerAndConfigurationSettings runnerAndConfigurationSettings = RunManager.getInstance(project).getSelectedConfiguration();
		if (runnerAndConfigurationSettings != null) {
			final RunConfiguration runConfiguration = runnerAndConfigurationSettings.getConfiguration();
			compileScope.putUserData(CompilerManager.RUN_CONFIGURATION_KEY, runConfiguration);
			compileScope.putUserData(CompilerManager.RUN_CONFIGURATION_TYPE_ID_KEY, runConfiguration.getType().getId());
		}
	}

	private boolean hasFacets(@NotNull final Module module) {
		return FacetManager.getInstance(module).getAllFacets().length > 0;
	}

	protected abstract boolean configure(
			@NotNull final ProgressIndicator indicator,
			@NotNull final FindBugsProjects projects,
			final boolean justCompiled
	);

	@Override
	public final void analysisAborting() {
		_cancellingByUser.set(true);
	}

	private static void configureDetectors(
			@NotNull final Map<String, Boolean> detectors,
			@NotNull final DetectorFactoryCollection detectorFactoryCollection,
			@NotNull final UserPreferences userPreferences
	) {
		for (final Map.Entry<String, Boolean> enabled : detectors.entrySet()) {
			final DetectorFactory detectorFactory = detectorFactoryCollection.getFactory(enabled.getKey());
			if (detectorFactory != null) {
				userPreferences.enableDetector(detectorFactory, enabled.getValue());
			}
		}
	}

	private static void configureFilter(
			@NotNull final FindBugs2 engine,
			@NotNull final UserPreferences userPrefs
	) {

		final Map<String, Boolean> excludeFilterFiles = userPrefs.getExcludeFilterFiles();
		for (final Map.Entry<String, Boolean> excludeFileName : excludeFilterFiles.entrySet()) {
			if (excludeFileName.getValue()) {
				final String filePath = excludeFileName.getKey();
				try {
					engine.addFilter(filePath, false);
				} catch (final IOException e) {
					LOGGER.error("ExcludeFilter configuration failed.", e);
				}
			}
		}
		final Map<String, Boolean> includeFilterFiles = userPrefs.getIncludeFilterFiles();
		for (final Map.Entry<String, Boolean> includeFileName : includeFilterFiles.entrySet()) {
			if (includeFileName.getValue()) {
				final String filePath = includeFileName.getKey();
				try {
					engine.addFilter(filePath, true);
				} catch (final IOException e) {
					LOGGER.error("IncludeFilter configuration failed.", e);
				}
			}
		}
		final Map<String, Boolean> excludeBugFiles = userPrefs.getExcludeBugsFiles();
		for (final Map.Entry<String, Boolean> excludeBugFile : excludeBugFiles.entrySet()) {
			if (excludeBugFile.getValue()) {
				final String filePath = excludeBugFile.getKey();
				try {
					engine.excludeBaselineBugs(filePath);
				} catch (final IOException | DocumentException e) {
					LOGGER.error("ExcludeBaseLineBug files configuration failed.", e);
				}
			}
		}
	}

	protected final void showWarning(@NotNull final String message) {
		EventDispatchThreadHelper.invokeLater(() -> BalloonTipFactory.showToolWindowWarnNotifier(
				project, message + " " + ResourcesLoader.getString("analysis.aborted")));
	}

	protected final boolean hasTests(@NotNull final Iterable<VirtualFile> virtualFiles) {
		final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
		for (final VirtualFile file : virtualFiles) {
			if (fileIndex.isInTestSourceContent(file)) {
				return true;
			}
		}
		return false;
	}

	protected final boolean hasTests(@NotNull final VirtualFile[] virtualFiles) {
		final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
		for (final VirtualFile file : virtualFiles) {
			if (fileIndex.isInTestSourceContent(file)) {
				return true;
			}
		}
		return false;
	}
}
