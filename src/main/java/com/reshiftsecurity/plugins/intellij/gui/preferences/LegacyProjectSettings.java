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
package com.reshiftsecurity.plugins.intellij.gui.preferences;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;
import com.reshiftsecurity.plugins.intellij.core.WorkspaceSettings;
import com.reshiftsecurity.plugins.intellij.preferences.PersistencePreferencesBean;

/**
 * Legacy settings are converted (by {@link LegacyProjectSettingsConverter}) to {@link ProjectSettings}.
 * The settings are removed when the .ipr or config xml is stored next time.
 */
@State(
		name = PluginConstants.PLUGIN_ID,
		storages = {
				@Storage(file = "$PROJECT_FILE$", deprecated = true),
				@Storage(file = "$PROJECT_CONFIG_DIR$/reshiftsecurity-intellij.xml", deprecated = true)})
public final class LegacyProjectSettings implements PersistentStateComponent<PersistencePreferencesBean> {

	private static final Logger LOGGER = Logger.getInstance(LegacyProjectSettings.class);

	private PersistencePreferencesBean state;

	@Nullable
	@Override
	public PersistencePreferencesBean getState() {
		return state;
	}

	@Override
	public void loadState(final PersistencePreferencesBean state) {
		this.state = state;
	}

	public static LegacyProjectSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, LegacyProjectSettings.class);
	}

	void applyTo(@NotNull final ProjectSettings settings, @NotNull final WorkspaceSettings workspaceSettings) {
		if (state == null) {
			return;
		}
		LOGGER.info("Start convert legacy findbugs-idea project settings");
		LegacyAbstractSettingsConverter.applyTo(state, settings, workspaceSettings, WorkspaceSettings.PROJECT_IMPORT_FILE_PATH_KEY);
	}
}
