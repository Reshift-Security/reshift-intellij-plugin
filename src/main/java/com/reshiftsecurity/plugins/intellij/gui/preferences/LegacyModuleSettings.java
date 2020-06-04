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
package com.reshiftsecurity.plugins.intellij.gui.preferences;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.FindBugsPluginConstants;
import com.reshiftsecurity.plugins.intellij.core.ModuleSettings;
import com.reshiftsecurity.plugins.intellij.core.WorkspaceSettings;
import com.reshiftsecurity.plugins.intellij.preferences.PersistencePreferencesBean;

/**
 * Legacy settings are converted (by {@link LegacyProjectSettingsConverter}) to {@link ModuleSettings}.
 * The settings are removed when the .iml is stored next time.
 */
@State(
		name = FindBugsPluginConstants.PLUGIN_ID,
		storages = {@Storage(file = "$MODULE_FILE$", deprecated = true)})
public final class LegacyModuleSettings implements PersistentStateComponent<PersistencePreferencesBean> {

	private static final Logger LOGGER = Logger.getInstance(LegacyModuleSettings.class);

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

	public static LegacyModuleSettings getInstance(@NotNull final Module module) {
		return ModuleServiceManager.getService(module, LegacyModuleSettings.class);
	}

	void applyTo(@NotNull final ModuleSettings settings, @Nullable final WorkspaceSettings workspaceSettings) {
		if (state == null) {
			return;
		}
		LOGGER.info("Start convert legacy findbugs-idea module settings");
		LegacyAbstractSettingsConverter.applyTo(state, settings, workspaceSettings, WorkspaceSettings.PROJECT_IMPORT_FILE_PATH_KEY);
	}
}
