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

import java.util.Optional;

public class AnalyticsEntry {
    private AnalyticsAction action;
    private Integer metric;
    private String actionName;
    private String category;
    private String label;

    private void setNameAndCategory(Optional<String> actionNameOverride) {
        switch (this.action) {
            case CLEAR_AND_CLOSE_PLUGIN_WINDOW:
                this.category = AnalyticsActionCategory.Plugin_Window.toString();
                this.actionName = "close";
                break;
            case CODE_VIEW_BUG_DETAILS:
                this.category = AnalyticsActionCategory.Code_View.toString();
                this.actionName = "click bug icon";
                break;
            case CODE_VIEW_OPEN_FILE:
                this.category = AnalyticsActionCategory.Code_View.toString();
                this.actionName = "click file name";
                break;
            case COPY_PLUGIN_INFO:
                this.category = AnalyticsActionCategory.Info_and_Help.toString();
                this.actionName = "copy plugin info";
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
            case OPEN_HELP:
                this.category = AnalyticsActionCategory.Info_and_Help.toString();
                this.actionName = "open help";
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
            case SCAN_RESULTS_METRIC:
                this.category = AnalyticsActionCategory.Scan_Metrics.toString();
                this.actionName = "scan metrics";
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
        this.setNameAndCategory(Optional.ofNullable(actionNameOverride));
    }

    public AnalyticsAction getAction() {
        return action;
    }
    public Integer getMetric() {
        return this.metric;
    }
    public String getActionName() { return this.actionName; }
    public String getCategory() { return this.category; }
    public String getLabel() { return this.label; }
}
