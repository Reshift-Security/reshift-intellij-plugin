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

package com.reshiftsecurity.plugins.intellij.common;

import java.io.File;

public class PluginConstants {

    public static final String PLUGIN_NAME = "Reshift Security IntelliJ plugin";
    public static final String TOOL_WINDOW_ID = "reshift"; // see plugin.xml
    public static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + File.separatorChar + TOOL_WINDOW_ID;

    public static final String PLUGIN_ID = "com.reshiftsecurity.plugins.intellij";

    // The action group for the plug-in tool window.
    public static final String ACTION_GROUP_LEFT = "Reshift.ToolBarActions.left";

    public static final String ACTION_GROUP_RIGHT = "Reshift.ToolBarActions.right";

    public static final String ACTION_GROUP_NAVIGATION = "Reshift.ToolBarActions.navigation";

    public static final String ACTION_GROUP_UTILS = "Reshift.ToolBarActions.utils";

    public static final String FILE_SEPARATOR = File.separator;

    public static final String FINDBUGS_CORE_PLUGIN_ID = "edu.umd.cs.findbugs.plugins.core";

    public static final String DEFAULT_SUPPRESS_WARNINGS_CLASSNAME = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

    public static final String RESHIFT_SITE_URL = "https://www.reshiftsecurity.com/?utm_source=plugin&utm_medium=IntelliJ&utm_campaign=intellij_plugin_reshift";
    public static final String RESHIFT_DEV_EMAIL = "dev@reshiftsecurity.com";
    public static final String UNINSTALL_FEEDBACK_URL = "https://www.reshiftsecurity.com/uninstall-intellij/";
    public static final String DEFAULT_EDU_TAB_TOOLTIP = "Security Expert Tools/Resources to fix vulnerabilities";

    public static final String ANDROID_STUDIO = "Android Studio";
}
