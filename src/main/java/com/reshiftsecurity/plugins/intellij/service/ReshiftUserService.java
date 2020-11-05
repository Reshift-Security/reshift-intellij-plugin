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

package com.reshiftsecurity.plugins.intellij.service;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.util.GuiUtil;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

@State(
        name = "ReshiftSecurity-IDEA-Data",
        storages = {@Storage(file = "reshift-user.xml")}
)
public class ReshiftUserService implements PersistentStateComponent<ReshiftUserService> {
    @Tag
    private Boolean isReshiftUser = false;

    @Tag
    private Boolean dismissSignupDialogueForever = false;

    @Tag
    private int dayLastShown = -1;

    private String getReferenceId() {
        return AnalyticsService.getInstance().getUserIdentifier();
    }

    public static ReshiftUserService getInstance() {
        return ServiceManager.getService(ReshiftUserService.class);
    }

    public String getSignupURL() {
        String refIdValue = getReferenceId();
        String rawUrl = String.format("%s/reference?ref_type=ide&ref_info=intellij&ref_id=%s&next=onboard",
                PluginConstants.RESHIFT_APP_URL, refIdValue);
        try {
            return new URI(rawUrl).toURL().toString();
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
        // fall back to reshift on-boarding url
        return PluginConstants.RESHIFT_ONBOARDING_URL;
    }

    public void showSignupWindow(JPanel parentPanel, int dayNumber) {
        this.dayLastShown = dayNumber;
        AnalyticsService.getInstance().recordAction(AnalyticsAction.SHOW_SIGNUP_DIALOGUE);

        JEditorPane signupContentPane = new JEditorPane();
        signupContentPane.setContentType(UIUtil.HTML_MIME);
        signupContentPane.setText(
                GuiUtil.getThemeAwareHtml(ResourcesLoader.getString("reshift.user.signup.content")));
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(signupContentPane);
        scrollPane.setPreferredSize(new Dimension(650, 610));

        JBCheckBox dismissForeverCheckbox = new JBCheckBox();
        dismissForeverCheckbox.setSelected(false);
        dismissForeverCheckbox.setText(ResourcesLoader.getString("reshift.user.signup.dismiss_forever"));

        DialogBuilder builder = new DialogBuilder(parentPanel);
        builder.setNorthPanel(scrollPane);
        builder.setCenterPanel(dismissForeverCheckbox);
        builder.setTitle(ResourcesLoader.getString("reshift.user.signup.title"));
        builder.removeAllActions();
        builder.addCancelAction().setText(ResourcesLoader.getString("reshift.user.signup.dismisstext"));
        builder.addOkAction().setText(ResourcesLoader.getString("reshift.user.signup.buttontext"));

        boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;

        if (isOk) {
            AnalyticsService.getInstance().recordAction(AnalyticsAction.CLICK_ON_SIGNUP_DIALOGUE);
            BrowserUtil.browse(getSignupURL());
        } else {
            this.dismissSignupDialogueForever = dismissForeverCheckbox.isSelected();
            if (this.dismissSignupDialogueForever) {
                AnalyticsService.getInstance().recordAction(AnalyticsAction.DISMISS_SIGNUP_DIALOG_FOREVER);
            } else {
                AnalyticsService.getInstance().recordAction(AnalyticsAction.DISMISS_SIGNUP_DIALOG);
            }
        }
    }

    public void postScanProcess(Project project) {
        Calendar todayDate = Calendar.getInstance();
        int today = todayDate.get(Calendar.DAY_OF_WEEK);
        boolean isTimeToShowPopup = Calendar.TUESDAY == today && today != this.dayLastShown;
        if (isTimeToShowPopup && !this.isReshiftUser && !this.dismissSignupDialogueForever) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(String.format("%s/api/user/reference/%s", PluginConstants.RESHIFT_APP_URL, getReferenceId()))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                Response referenceCheckResponse = client.newCall(request).execute();
                if (referenceCheckResponse.code() != 200) {
                    showSignupWindow(ToolWindowPanel.getInstance(project), today);
                } else {
                    isReshiftUser = true;
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public @Nullable ReshiftUserService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ReshiftUserService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
