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
    CODE_VIEW_BUG_DETAILS,
    CODE_VIEW_OPEN_FILE,
    COPY_PLUGIN_INFO,
    INSTALL,
    ISSUE_REPORT_BROWSE,
    ISSUE_REPORT_EDU,
    ISSUE_REPORT_MORE_SCAN_INFO,
    OPEN_HELP,
    OPEN_PLUGIN_WINDOW,
    OPEN_RESHIFT_WEBSITE,
    OPEN_SETTINGS,
    SCAN_RESULTS_METRIC,
    SETTINGS_GATHER_DATA_NO,
    SETTINGS_GATHER_DATA_YES,
    SETTINGS_UPDATED,
    START_SCAN,
    STOP_SCAN,
    UNINSTALL;

    @Override
    public String toString() {
        return name();
    }
}
