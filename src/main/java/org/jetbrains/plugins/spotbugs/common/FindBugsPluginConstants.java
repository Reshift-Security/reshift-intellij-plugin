/*
 * Copyright 2020 SpotBugs plugin contributors
 *
 * This file is part of IntelliJ SpotBugs plugin.
 *
 * IntelliJ SpotBugs plugin is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * IntelliJ SpotBugs plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IntelliJ SpotBugs plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.jetbrains.plugins.spotbugs.common;

import java.io.File;

@SuppressWarnings({"HardCodedStringLiteral"})
public final class FindBugsPluginConstants {

	public static final String PLUGIN_NAME = "Reshift Security IntelliJ plugin";
	public static final String TOOL_WINDOW_ID = "reshift"; // see plugin.xml
	public static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + File.separatorChar + TOOL_WINDOW_ID;

	public static final String PLUGIN_ID = "com.reshiftsecurity.plugins.intellij";
	public static final String MODULE_ID = "org.jetbrains.plugins.spotbugs.module";

	// The action group for the plug-in tool window.
	public static final String ACTION_GROUP_LEFT = "SpotBugs.ToolBarActions.left";

	public static final String ACTION_GROUP_RIGHT = "SpotBugs.ToolBarActions.right";

	public static final String ACTION_GROUP_NAVIGATION = "SpotBugs.ToolBarActions.navigation";

	public static final String ACTION_GROUP_UTILS = "SpotBugs.ToolBarActions.utils";

	public static final String FILE_SEPARATOR = File.separator;

	public static final String FINDBUGS_CORE_PLUGIN_ID = "edu.umd.cs.findbugs.plugins.core";

	public static final String DEFAULT_SUPPRESS_WARNINGS_CLASSNAME = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

	private FindBugsPluginConstants() {
	}
}
