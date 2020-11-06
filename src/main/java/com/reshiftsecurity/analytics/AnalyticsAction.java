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

public enum AnalyticsAction {
    CLEAR_AND_CLOSE_PLUGIN_WINDOW,
    CLICK_ON_SIGNUP_DIALOGUE,
    CLICK_ON_SIGNUP_IN_INFO,
    CLICK_ON_SIGNUP_IN_NOTIFICATION,
    CODE_VIEW_BUG_DETAILS,
    CODE_VIEW_OPEN_FILE,
    COPY_PLUGIN_INFO,
    DISMISS_SIGNUP_DIALOG,
    DISMISS_SIGNUP_DIALOG_FOREVER,
    FIXES_METRIC,
    INSTALL,
    ISSUE_REPORT_AUTO_PREVIEW_DISABLE,
    ISSUE_REPORT_AUTO_PREVIEW_ENABLE,
    ISSUE_REPORT_BROWSE,
    ISSUE_REPORT_EDU,
    ISSUE_REPORT_MORE_SCAN_INFO,
    ISSUE_REPORT_SEARCH,
    NAVIGATE_TO_SIGNUP,
    OPEN_HELP,
    OPEN_INTELLIJ,
    OPEN_PLUGIN_WINDOW,
    OPEN_RESHIFT_WEBSITE,
    OPEN_SETTINGS,
    OTHER_PLUGINS,
    SCAN_RESULTS_METRIC,
    SETTINGS_GATHER_DATA_DISMISS,
    SETTINGS_GATHER_DATA_NO,
    SETTINGS_GATHER_DATA_YES,
    SETTINGS_UPDATED,
    SHOW_SIGNUP_DIALOGUE,
    START_SCAN,
    STOP_SCAN,
    UNINSTALL;

    @Override
    public String toString() {
        return name();
    }
}
