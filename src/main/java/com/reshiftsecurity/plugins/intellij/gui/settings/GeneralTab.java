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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsServiceSettings;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.core.AbstractSettings;
import com.reshiftsecurity.plugins.intellij.core.WorkspaceSettings;
import com.reshiftsecurity.plugins.intellij.gui.common.HAlignment;
import com.reshiftsecurity.plugins.intellij.gui.common.VAlignment;
import com.reshiftsecurity.plugins.intellij.gui.common.VerticalFlowLayout;
import com.reshiftsecurity.plugins.intellij.plugins.Plugins;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import javax.swing.JPanel;
import java.awt.BorderLayout;

final class GeneralTab extends JPanel {
	private JBCheckBox compileBeforeAnalyze;
	private JBCheckBox analyzeAfterCompile;
	private JBCheckBox analyzeAfterAutoMake;
	private JBCheckBox runInBackground;
	private JBCheckBox toolWindowToFront;
	private JBCheckBox analyticsSend;
	private PluginTablePane plugin;

	GeneralTab() {
		super(new BorderLayout());
		compileBeforeAnalyze = new JBCheckBox(ResourcesLoader.getString("general.compileBeforeAnalyze.title"));
		analyzeAfterCompile = new JBCheckBox(ResourcesLoader.getString("general.analyzeAfterCompile.title"));
		analyzeAfterAutoMake = new JBCheckBox(ResourcesLoader.getString("general.analyzeAfterAutoMake.title"));
		runInBackground = new JBCheckBox(ResourcesLoader.getString("general.runInBackground.title"));
		toolWindowToFront = new JBCheckBox(ResourcesLoader.getString("general.toolWindowToFront.title"));
		analyticsSend = new JBCheckBox(ResourcesLoader.getString("general.analyticsSend.title"));
		plugin = new PluginTablePane();

		final JPanel topPane = new JPanel(new VerticalFlowLayout(HAlignment.Left, VAlignment.Top, 0, UIUtil.DEFAULT_VGAP, false, false));
		topPane.add(compileBeforeAnalyze);
		topPane.add(analyzeAfterCompile);
		topPane.add(analyzeAfterAutoMake);
		topPane.add(runInBackground);
		topPane.add(toolWindowToFront);
		topPane.add(analyticsSend);

		add(topPane, BorderLayout.NORTH);
		// Hiding unnecessary plugin tab
		// add(plugin);
	}

	void setProjectSettingsEnabled(final boolean enabled) {
		plugin.setEnabled(enabled);
	}

	boolean isModified(@NotNull final AbstractSettings settings) {
		return plugin.isModified(settings);
	}

	boolean isModifiedWorkspace(@NotNull final WorkspaceSettings settings) {
		return compileBeforeAnalyze.isSelected() != settings.compileBeforeAnalyze ||
				analyzeAfterCompile.isSelected() != settings.analyzeAfterCompile ||
				analyzeAfterAutoMake.isSelected() != settings.analyzeAfterAutoMake ||
				runInBackground.isSelected() != settings.runInBackground ||
				toolWindowToFront.isSelected() != settings.toolWindowToFront ||
				analyticsSend.isSelected() != AnalyticsServiceSettings.getInstance().sendAnonymousUsage;
	}

	void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		plugin.apply(settings);
	}

	void applyWorkspace(@NotNull final WorkspaceSettings settings) throws ConfigurationException {
		settings.compileBeforeAnalyze = compileBeforeAnalyze.isSelected();
		settings.analyzeAfterCompile = analyzeAfterCompile.isSelected();
		settings.analyzeAfterAutoMake = analyzeAfterAutoMake.isSelected();
		settings.runInBackground = runInBackground.isSelected();
		settings.toolWindowToFront = toolWindowToFront.isSelected();
		AnalyticsServiceSettings.getInstance().sendAnonymousUsage = analyticsSend.isSelected();
	}

	void reset(@NotNull final AbstractSettings settings) {
		plugin.reset(settings);
	}

	void resetWorkspace(@NotNull final WorkspaceSettings settings) {
		compileBeforeAnalyze.setSelected(settings.compileBeforeAnalyze);
		analyzeAfterCompile.setSelected(settings.analyzeAfterCompile);
		analyzeAfterAutoMake.setSelected(settings.analyzeAfterAutoMake);
		runInBackground.setSelected(settings.runInBackground);
		toolWindowToFront.setSelected(settings.toolWindowToFront);
		analyticsSend.setSelected(AnalyticsServiceSettings.getInstance().sendAnonymousUsage);
	}

	@NotNull
	PluginTablePane getPluginTablePane() {
		return plugin;
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.general");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"general.compileBeforeAnalyze.title",
				"general.analyzeAfterCompile.title",
				"general.analyzeAfterAutoMake.title",
				"general.runInBackground.title",
				"general.toolWindowToFront.title",
				"general.analyticsSend.title",
				// PluginTablePane
				"plugins.title",
				"plugins.addFromDisk"
		};
	}

	@NotNull
	static String[] getSearchTexts() {
		return new String[]{

				Plugins.fb_contrib.id,
				"fb-contrib plugin",
				"This plugin contains FindBugs detectors from the fb-contrib project",
				"https://github.com/mebigfatguy/fb-contrib",

				Plugins.findsecbugs_plugin.id,
				"Find Security Bugs",
				"The FindBugs plugin for security audits of Java web applications.",
				"https://github.com/h3xstream/find-sec-bugs",

				Plugins.AndroidFindbugs.id,
				"Findbugs plugin for Android",
				"This plugin consist of FindBugs detectors for Android coding"
		};
	}
}
