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

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.util.GuiUtil;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsServiceSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class InstallationListener implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
         if (!AnalyticsServiceSettings.getInstance().hasConsent()) {
             JEditorPane termsContentPane = new JEditorPane();
             termsContentPane.setContentType(UIUtil.HTML_MIME);
             termsContentPane.setText(
                     GuiUtil.getThemeAwareHtml(ResourcesLoader.getString("analytics.confirmation.terms")));

             JBCheckBox usageDataConsent = new JBCheckBox();
             usageDataConsent.setSelected(true);
             usageDataConsent.setText(ResourcesLoader.getString("analytics.confirmation.text"));

             DialogBuilder builder = new DialogBuilder(ToolWindowPanel.getInstance(project));
             builder.setNorthPanel(termsContentPane);
             builder.setCenterPanel(usageDataConsent);
             builder.setTitle(ResourcesLoader.getString("analytics.confirmation.title"));
             builder.removeAllActions();
             builder.addOkAction().setText(ResourcesLoader.getString("analytics.confirmation.save"));

             boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;

             if (isOk) {
                 AnalyticsServiceSettings.getInstance().recordConsent(usageDataConsent.isSelected());
             } else {
                 // pop-up has been dismissed
                 AnalyticsServiceSettings.getInstance().recordConsentDismiss();
             }
        }

        com.intellij.ide.plugins.PluginInstaller.addStateListener(new PluginStateListener() {
            @Override
            public void install(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
                if (PluginConstants.PLUGIN_ID.equalsIgnoreCase(ideaPluginDescriptor.getPluginId().getIdString())) {
                    AnalyticsService.getInstance().recordInstall();
                }
            }

            @Override
            public void uninstall(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
                if (PluginConstants.PLUGIN_ID.equalsIgnoreCase(ideaPluginDescriptor.getPluginId().getIdString())) {
                    AnalyticsService.getInstance().recordConsentExemptAction(AnalyticsAction.UNINSTALL);
                    BrowserUtil.browse(PluginConstants.UNINSTALL_FEEDBACK_URL);
                }
            }
        });
    }
}
