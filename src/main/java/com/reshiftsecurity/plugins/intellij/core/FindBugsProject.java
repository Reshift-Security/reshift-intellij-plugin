/*
 * Copyright 2020 SpotBugs plugin contributors
 *
 * This file is part of IntelliJ SpotBugs plugin.
 *
 * IntelliJ SpotBugs plugin is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * IntelliJ SpotBugs plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IntelliJ SpotBugs plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.reshiftsecurity.plugins.intellij.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.collectors.StatelessClassAdder;
import com.reshiftsecurity.plugins.intellij.common.util.New;

import java.util.Collections;
import java.util.List;

public class FindBugsProject extends edu.umd.cs.findbugs.Project {

	@NotNull
	private final Project project;

	@NotNull
	private final Module module;

	private List<String> _outputFiles;

	private StatelessClassAdder classAdder;

	private FindBugsProject(@NotNull final Project project, @NotNull final Module module) {
		this.project = project;
		this.module = module;
	}

	@NotNull
	public Module getModule() {
		return module;
	}

	@NotNull
	private StatelessClassAdder getClassAdder() {
		if (classAdder == null) {
			classAdder = new StatelessClassAdder(this, project);
		}
		return classAdder;
	}

	void addOutputFile(@NotNull final VirtualFile file) {
		if (_outputFiles == null) {
			_outputFiles = New.arrayList();
		}
		_outputFiles.add(file.getPath());
		getClassAdder().addContainingClasses(file);
	}

	public void addOutputFile(@NotNull final VirtualFile file, @NotNull final PsiClass psiClass) {
		if (_outputFiles == null) {
			_outputFiles = New.arrayList();
		}
		_outputFiles.add(file.getPath());
		getClassAdder().addContainingClasses(file, psiClass);
	}

	@NotNull
	public List<String> getConfiguredOutputFiles() {
		return _outputFiles != null ? _outputFiles : Collections.<String>emptyList();
	}

	@NotNull
	static FindBugsProject create(
			@NotNull final Project project,
			@NotNull final Module module,
			@NotNull final String projectName
	) {


		final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
		final AbstractSettings settings;
		if (moduleSettings.overrideProjectSettings) {
			settings = moduleSettings;
		} else {
			settings = ProjectSettings.getInstance(project);
		}

		final FindBugsProject ret = new FindBugsProject(project, module);
		ret.setProjectName(projectName);
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			if (!plugin.isCorePlugin()) {
				boolean enabled = false;
				for (final PluginSettings pluginSettings : settings.plugins) {
					if (plugin.getPluginId().equals(pluginSettings.id)) {
						if (pluginSettings.enabled) {
							enabled = true; // do not break loop here ; maybe there are multiple plugins (with same plugin id) configured and one is enabled
						}
					}
				}
				ret.setPluginStatusTrinary(plugin.getPluginId(), enabled);
			}
		}
		return ret;
	}
}
