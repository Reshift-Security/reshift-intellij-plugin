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
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.collectors.RecurseFileCollector;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.FindBugsProject;
import com.reshiftsecurity.plugins.intellij.core.FindBugsProjects;
import com.reshiftsecurity.plugins.intellij.core.FindBugsStarter;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import java.io.File;
import java.util.List;

public abstract class AnalyzeProjectFiles extends AbstractAnalyzeAction {
	private final boolean includeTests;

	AnalyzeProjectFiles(boolean includeTests) {
		this.includeTests = includeTests;
	}

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final boolean enable = state.isIdle();

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		new FindBugsStarter(project, "Running security analysis for project '" + project.getName() + "'...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(compilerManager.createProjectCompileScope(project));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				final Module[] modules = ModuleManager.getInstance(project).getModules();
				final List<Pair.NonNull<Module, VirtualFile>> compilerOutputPaths = New.arrayList();
				for (final Module module : modules) {
					final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
					if (extension == null) {
						throw new IllegalStateException("No compiler extension for module " + module.getName());
					}
					final VirtualFile compilerOutputPath = extension.getCompilerOutputPath();
					if (compilerOutputPath != null) {
						/*
						 * Otherwise ignore it. Maybe this module is only used to contains fact (think of Android)
						 * or to aggregate modules (think of maven).
						 */
						compilerOutputPaths.add(Pair.createNonNull(module, compilerOutputPath));
					}
					if (includeTests) {
						final VirtualFile compilerOutputPathForTests = extension.getCompilerOutputPathForTests();
						if (compilerOutputPathForTests != null) {
							compilerOutputPaths.add(Pair.createNonNull(module, compilerOutputPathForTests));
						}
					}
				}

				if (compilerOutputPaths.isEmpty()) {
					showWarning(ResourcesLoader.getString("analysis.noOutputPaths"));
					return false;
				}

				indicator.setText("Collecting files for analysis...");
				final int[] count = new int[1];
				for (final Pair.NonNull<Module, VirtualFile> compilerOutputPath : compilerOutputPaths) {
					final FindBugsProject findBugsProject = projects.get(compilerOutputPath.getFirst(), includeTests);
					RecurseFileCollector.addFiles(project, indicator, findBugsProject, new File(compilerOutputPath.getSecond().getCanonicalPath()), count);
				}
				return true;
			}
		}.start();
	}
}
