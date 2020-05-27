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
package org.jetbrains.plugins.spotbugs.core;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
		name = "FindBugs-IDEA",
		storages = {
				// Remove both @Storage entry below and use only this if IDEA 15 support is gone: @Storage("findbugs-idea.xml")
				@Storage(file = "$PROJECT_FILE$"),
				@Storage(file = "$PROJECT_CONFIG_DIR$/reshiftsecurity-intellij.xml")
		}
)
public final class ProjectSettings extends AbstractSettings implements PersistentStateComponent<ProjectSettings> {

	@Nullable
	@Override
	public ProjectSettings getState() {
		return this;
	}

	@Override
	public void loadState(final ProjectSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	@NotNull
	public static ProjectSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, ProjectSettings.class);
	}
}
