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
package com.reshiftsecurity.plugins.intellij.gui.settings;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.core.ModuleSettings;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

final class ModuleSettingsPane extends SettingsPane {

	private JBCheckBox overrideProjectSettingsCheckbox;

	ModuleSettingsPane(@NotNull final Project project, @NotNull final Module module) {
		super(project, module);
	}

	@Override
	void initHeaderPane(@NotNull final JPanel topPanel) {
		overrideProjectSettingsCheckbox = new JBCheckBox(ResourcesLoader.getString("settings.module.overrideProjectSettings"));
		overrideProjectSettingsCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (overrideProjectSettingsCheckbox.isSelected()) {
					if (Messages.YES == Messages.showYesNoDialog(
							overrideProjectSettingsCheckbox,
							ResourcesLoader.getString("settings.module.loadProject.text"),
							StringUtil.capitalizeWords(ResourcesLoader.getString("settings.module.loadProject.title"), true),
							Messages.getQuestionIcon()
					)) {
						reset(ProjectSettings.getInstance(project));
					}
				} else {
					if (Messages.YES == Messages.showYesNoDialog(
							overrideProjectSettingsCheckbox,
							ResourcesLoader.getString("settings.module.reset.text"),
							StringUtil.capitalizeWords(ResourcesLoader.getString("settings.action.reset.title"), true),
							Messages.getQuestionIcon()
					)) {
						final ModuleSettings settings = new ModuleSettings();
						reset(settings);
						resetModule(settings);
					}
				}
				updateControls();
			}
		});
		topPanel.add(overrideProjectSettingsCheckbox);
	}

	private void updateControls() {
		setProjectSettingsEnabled(overrideProjectSettingsCheckbox.isSelected());
	}

	@Override
	boolean isModifiedModule(@NotNull final ModuleSettings settings) {
		return settings.overrideProjectSettings != overrideProjectSettingsCheckbox.isSelected();
	}

	@Override
	void applyModule(@NotNull final ModuleSettings settings) {
		settings.overrideProjectSettings = overrideProjectSettingsCheckbox.isSelected();
		updateControls();
	}

	@Override
	void resetModule(@NotNull final ModuleSettings settings) {
		overrideProjectSettingsCheckbox.setSelected(settings.overrideProjectSettings); // does not fire action listener
		updateControls();
	}
}
