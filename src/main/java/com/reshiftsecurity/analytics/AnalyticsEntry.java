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

package com.reshiftsecurity.analytics;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class AnalyticsEntry {
    private AnalyticsAction action;
    private Integer metric;
    private String actionName;
    private String category;
    private String label;
    private String dimensionValue;

    private void setNameAndCategory(Optional<String> actionNameOverride) {
        switch (this.action) {
            case CLEAR_AND_CLOSE_PLUGIN_WINDOW:
                this.category = AnalyticsActionCategory.Plugin_Window.toString();
                this.actionName = "close";
                break;
            case CLICK_ON_SIGNUP_DIALOGUE:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "click signup from dialogue";
                break;
            case CLICK_ON_SIGNUP_IN_INFO:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "click signup from help and info notification";
                break;
            case CLICK_ON_SIGNUP_IN_NOTIFICATION:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "click signup from post-scan notification";
                break;
            case CODE_VIEW_BUG_DETAILS:
                this.category = AnalyticsActionCategory.Code_View.toString();
                this.actionName = "click bug icon";
                break;
            case CODE_VIEW_OPEN_FILE:
                this.category = AnalyticsActionCategory.Code_View.toString();
                this.actionName = "click file name";
                break;
            case DISABLE_NOTIFICATION:
                this.category = AnalyticsActionCategory.Plugin_Window.toString();
                this.actionName = "disable scan completed notification";
                break;
            case DISMISS_SIGNUP_DIALOG:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "dismiss signup dialogue";
                break;
            case DISMISS_SIGNUP_DIALOG_FOREVER:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "do not show signup dialogue again";
                break;
            case COPY_PLUGIN_INFO:
                this.category = AnalyticsActionCategory.Info_and_Help.toString();
                this.actionName = "copy plugin info";
                break;
            case FIXES_METRIC:
                this.category = AnalyticsActionCategory.Scan_Metrics.toString();
                this.actionName = "fix count";
                break;
            case ISSUE_REPORT_AUTO_PREVIEW_DISABLE:
                this.category = AnalyticsActionCategory.Scan_Report.toString();
                this.actionName = "disable auto file preview";
                break;
            case ISSUE_REPORT_AUTO_PREVIEW_ENABLE:
                this.category = AnalyticsActionCategory.Scan_Report.toString();
                this.actionName = "enable auto file preview";
                break;
            case ISSUE_REPORT_BROWSE:
                this.category = AnalyticsActionCategory.Scan_Report.toString();
                this.actionName = "browse";
                break;
            case ISSUE_REPORT_EDU:
                this.category = AnalyticsActionCategory.Education.toString();
                this.actionName = "browse education";
                break;
            case ISSUE_REPORT_MORE_SCAN_INFO:
                this.category = AnalyticsActionCategory.Scan_Report.toString();
                this.actionName = "open report info";
                break;
            case ISSUE_REPORT_SEARCH:
                this.category = AnalyticsActionCategory.Scan_Report.toString();
                this.actionName = "quick search";
                break;
            case NAVIGATE_TO_SIGNUP:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "click on signup link";
                break;
            case OPEN_HELP:
                this.category = AnalyticsActionCategory.Info_and_Help.toString();
                this.actionName = "open help";
                break;
            case OPEN_INTELLIJ:
                this.category = AnalyticsActionCategory.Intellij.toString();
                this.actionName = "open intellij";
                break;
            case OPEN_PLUGIN_WINDOW:
                this.category = AnalyticsActionCategory.Plugin_Window.toString();
                this.actionName = "open";
                break;
            case OPEN_RESHIFT_WEBSITE:
                this.category = AnalyticsActionCategory.Info_and_Help.toString();
                this.actionName = "open website";
                break;
            case OPEN_SETTINGS:
                this.category = AnalyticsActionCategory.Settings.toString();
                this.actionName = "open settings";
                break;
            case OTHER_PLUGINS:
                this.category = AnalyticsActionCategory.Other.toString();
                this.actionName = "other plugins";
                break;
            case SCAN_RESULTS_METRIC:
                this.category = AnalyticsActionCategory.Scan_Metrics.toString();
                this.actionName = "issues found count";
                break;
            case SETTINGS_GATHER_DATA_DISMISS:
                this.category = AnalyticsActionCategory.User_Consent.toString();
                this.actionName = "consent popup dismissed";
                break;
            case SETTINGS_GATHER_DATA_NO:
                this.category = AnalyticsActionCategory.User_Consent.toString();
                this.actionName = "consent declined";
                break;
            case SETTINGS_GATHER_DATA_YES:
                this.category = AnalyticsActionCategory.User_Consent.toString();
                this.actionName = "consent accepted";
                break;
            case SETTINGS_UPDATED:
                this.category = AnalyticsActionCategory.Settings.toString();
                this.actionName = "update settings";
                break;
            case SHOW_SIGNUP_DIALOGUE:
                this.category = AnalyticsActionCategory.Reshift_Signup.toString();
                this.actionName = "signup dialogue shown";
                break;
            case START_SCAN:
                this.category = AnalyticsActionCategory.Scan.toString();
                this.actionName = "start";
                break;
            case STOP_SCAN:
                this.category = AnalyticsActionCategory.Scan.toString();
                this.actionName = "stop";
                break;
            default:
                this.category = this.action.toString();
                this.actionName = this.action.toString().replace("_"," ");
        }

        if (actionNameOverride.isPresent()) {
            this.actionName = actionNameOverride.get().toLowerCase();
        }
    }

    public AnalyticsEntry(AnalyticsAction action) {
        this(action, null);
    }
    public AnalyticsEntry(AnalyticsAction action, Integer metric) {
        this(action, metric, null);
    }
    public AnalyticsEntry(AnalyticsAction action, Integer metric, String actionNameOverride) {
        this.action = action;
        this.metric = metric;
        this.label = action.toString();
        this.dimensionValue = null;
        this.setNameAndCategory(Optional.ofNullable(actionNameOverride));
    }

    public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }

    public AnalyticsAction getAction() {
        return action;
    }
    public Integer getMetric() {
        return this.metric;
    }
    public String getActionName() { return this.actionName; }
    public String getCategory() { return this.category; }
    public String getLabel() { return this.label; }
    public String getDimensionValue() { return this.dimensionValue; }
    public boolean isDimensionValueSet() { return !StringUtils.isEmpty(this.dimensionValue); }
}
