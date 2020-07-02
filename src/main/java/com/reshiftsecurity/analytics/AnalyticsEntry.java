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

public class AnalyticsEntry {
    private AnalyticsAction action;
    private Integer metric;
    private String actionName;
    private String category;
    private String label;

    private void setNameAndCategory() {
        switch (this.action) {
            case START_SCAN:
                this.category = "Scan";
                this.actionName = "Start";
                break;
            case STOP_SCAN:
                this.category = "Scan";
                this.actionName = "Stop";
                break;
            case OPEN_PLUGIN_WINDOW:
                this.category = "Plugin Window";
                this.actionName = "open";
                break;
            case CLEAR_AND_CLOSE_PLUGIN_WINDOW:
                this.category = "Plugin Window";
                this.actionName = "close";
                break;
            case OPEN_HELP:
                this.category = "Info and Help";
                this.actionName = "open help";
                break;
            case OPEN_RESHIFT_WEBSITE:
                this.category = "Info and Help";
                this.actionName = "open website";
                break;
            case COPY_PLUGIN_INFO:
                this.category = "Info and Help";
                this.actionName = "copy plugin info";
                break;
            case ISSUE_REPORT_BROWSE:
                this.category = "Scan Report";
                this.actionName = "browse";
                break;
            case ISSUE_REPORT_BUG_DETAILS:
                this.category = "Scan Report";
                this.actionName = "browse details";
                break;
            case ISSUE_REPORT_EDU:
                this.category = "Education";
                this.actionName = "browse education";
                break;
            case ISSUE_REPORT_MORE_SCAN_INFO:
                this.category = "Scan Report";
                this.actionName = "open report info";
                break;
            case CODE_VIEW_BUG_DETAILS:
                this.category = "Scan Report";
                this.actionName = "open issue details";
                break;
            case OPEN_MORE_SCAN_INFO:
                this.category = "Scan metrics";
                this.actionName = "open scan info";
                break;
            case SCAN_RESULTS_METRIC:
                this.category = "Scan metrics";
                this.actionName = "scan metrics";
                break;
            case OPEN_SETTINGS:
                this.category = "Settings";
                this.actionName = "open settings";
                break;
            case SETTINGS_UPDATED:
                this.category = "Settings";
                this.actionName = "update settings";
                break;
            case SETTINGS_GATHER_DATA_YES:
                this.category = "User Consent";
                this.actionName = "consent accepted";
                break;
            case SETTINGS_GATHER_DATA_NO:
                this.category = "User Consent";
                this.actionName = "consent declined";
                break;
            default:
                this.category = this.action.toString();
                this.actionName = this.action.toString().replace("_"," ");
        }
    }

    public AnalyticsEntry(AnalyticsAction action) {
        this.action = action;
        this.setNameAndCategory();
        this.label = action.toString();
    }
    public AnalyticsEntry(AnalyticsAction action, Integer metric) {
        this.action = action;
        this.metric = metric;
        this.setNameAndCategory();
        this.label = action.toString();
    }
    public AnalyticsEntry(AnalyticsAction action, Integer metric, String label) {
        this.action = action;
        this.metric = metric;
        this.setNameAndCategory();
        this.label = label;
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
