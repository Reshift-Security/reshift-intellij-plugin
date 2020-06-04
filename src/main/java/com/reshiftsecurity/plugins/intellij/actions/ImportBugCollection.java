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
package com.reshiftsecurity.plugins.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Processor;
import com.intellij.util.containers.TransferToEDTQueue;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.dom4j.DocumentException;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.common.EventDispatchThreadHelper;
import com.reshiftsecurity.plugins.intellij.common.FindBugsPluginConstants;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.Bug;
import com.reshiftsecurity.plugins.intellij.core.FindBugsResult;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.core.PluginSettings;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;
import com.reshiftsecurity.plugins.intellij.core.WorkspaceSettings;
import com.reshiftsecurity.plugins.intellij.gui.common.BalloonTipFactory;
import com.reshiftsecurity.plugins.intellij.gui.common.ImportFileDialog;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;
import com.reshiftsecurity.plugins.intellij.messages.MessageBusManager;
import com.reshiftsecurity.plugins.intellij.tasks.BackgroundableTask;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class ImportBugCollection extends AbstractAction {

	private static final Logger LOGGER = Logger.getInstance(ImportBugCollection.class);

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		e.getPresentation().setEnabled(state.isIdle());
		e.getPresentation().setVisible(true);
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final DialogBuilder dialogBuilder = new DialogBuilder(project);
		dialogBuilder.addOkAction();
		dialogBuilder.addCancelAction();
		dialogBuilder.setTitle("Import previous saved bug collection xml");

		final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);
		String exportDir = StringUtil.isEmptyOrSpaces(workspaceSettings.exportBugCollectionDirectory) ? FindBugsPluginConstants.DEFAULT_EXPORT_DIR : workspaceSettings.exportBugCollectionDirectory;

		final ImportFileDialog importFileDialog = new ImportFileDialog(exportDir, dialogBuilder);
		dialogBuilder.showModal(true);
		if (dialogBuilder.getDialogWrapper().getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
			return;
		}
		final String fileToImport = importFileDialog.getText();
		if (fileToImport == null || fileToImport.trim().isEmpty()) {
			return;
		}


		final FindBugsResult findBugsResult = ToolWindowPanel.getInstance(project).getResult();
		if (findBugsResult != null && !findBugsResult.isBugCollectionEmpty()) {
			//noinspection DialogTitleCapitalization
			final int result = Messages.showYesNoDialog(project, "Current result in the 'Found bugs view' will be cleared. Continue ?", "Clear found bugs?", Messages.getQuestionIcon());
			if (result == 1) {
				return;
			}
		}

		final Set<String> enabledPluginIds = New.set();
		for (final PluginSettings pluginSettings : ProjectSettings.getInstance(project).plugins) {
			if (pluginSettings.enabled) {
				enabledPluginIds.add(pluginSettings.id);
			}
		}

		final AtomicBoolean taskCanceled = new AtomicBoolean();
		final TransferToEDTQueue<Runnable> transferToEDTQueue = new TransferToEDTQueue<Runnable>("Add New Bug Instance", new Processor<Runnable>() {
			@Override
			public boolean process(Runnable runnable) {
				runnable.run();
				return true;
			}
		}, new Condition<Object>() {
			@Override
			public boolean value(Object o) {
				return project.isDisposed() || taskCanceled.get();
			}
		}, 500);

		//Create a task to import the bug collection from XML
		final BackgroundableTask task = new BackgroundableTask(project, "Importing Findbugs Result", true) {
			private ProgressIndicator _indicator;


			@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {

				MessageBusManager.publishAnalysisStartedToEDT(project);
				setProgressIndicator(indicator);
				indicator.setFraction(0.0);
				indicator.setIndeterminate(false);
				indicator.setText(fileToImport);
				SortedBugCollection importBugCollection = null;
				edu.umd.cs.findbugs.Project importProject = null;
				boolean success = false;
				try {
					final SortedBugCollection bugCollection = new SortedBugCollection();
					importBugCollection = bugCollection.createEmptyCollectionWithMetadata();
					importProject = importBugCollection.getProject();
					for (final Plugin plugin : Plugin.getAllPlugins()) {
						importProject.setPluginStatusTrinary(plugin.getPluginId(), plugin.isCorePlugin() || enabledPluginIds.contains(plugin.getPluginId()));
					}
					importBugCollection.readXML(fileToImport);

					final ProjectStats projectStats = importBugCollection.getProjectStats();
					int bugCount = 0;
					for (final BugInstance bugInstance : importBugCollection) {
						if (indicator.isCanceled()) {
							taskCanceled.set(true);
							MessageBusManager.publishAnalysisAbortedToEDT(project);
							Thread.currentThread().interrupt();
							return;
						}
						final int bugCounter = bugCount++;
						final double fraction = (double) bugCounter / projectStats.getTotalBugs();
						indicator.setFraction(fraction);
						indicator.setText2("Importing bug '" + bugCount + "' of '" + projectStats.getTotalBugs() + "' - " + bugInstance.getMessageWithoutPrefix());
						/*
						 * Guarantee thread visibility *one* time.
						 */
						final AtomicReference<BugInstance> bugInstanceRef = New.atomicRef(bugInstance);
						final AtomicReference<SortedBugCollection> importBugCollectionRef = New.atomicRef(importBugCollection);
						final int analyzedClassCount = projectStats.getNumClasses();
						transferToEDTQueue.offer(new Runnable() {
							/**
							 * Invoked by EDT.
							 */
							@Override
							public void run() {
								final Bug bug = new Bug(null, importBugCollectionRef.get(), bugInstanceRef.get());
								MessageBusManager.publishNewBug(project, bug, analyzedClassCount);
							}
						});
					}

					EventDispatchThreadHelper.invokeLater(() -> {
						transferToEDTQueue.drain();
						BalloonTipFactory.showToolWindowInfoNotifier(project, "Imported bug collection from '" + fileToImport + "'.");
					});

					importBugCollection.setTimestamp(System.currentTimeMillis());
					success = true;
				} catch (final IOException | DocumentException e1) {
					final String message = "Import failed";
					showToolWindowErrorNotifier(project, message);
					LOGGER.error(message, e1);

				} finally {
					if (success) {
						final FindBugsResult result = new FindBugsResult();
						result.put(importProject, importBugCollection);
						MessageBusManager.publishAnalysisFinishedToEDT(project, result, null);
						Thread.currentThread().interrupt();
					} else {
						MessageBusManager.publishAnalysisAbortedToEDT(project);
					}
				}
			}


			@Override
			public void setProgressIndicator(@NotNull final ProgressIndicator indicator) {
				_indicator = indicator;
			}


			@Override
			public ProgressIndicator getProgressIndicator() {
				return _indicator;
			}
		};

		task.setCancelText("Cancel");
		task.asBackgroundable();
		task.queue();
	}

	private static void showToolWindowErrorNotifier(@NotNull final Project project, final String message) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				BalloonTipFactory.showToolWindowErrorNotifier(project, message);
			}
		});
	}
}
