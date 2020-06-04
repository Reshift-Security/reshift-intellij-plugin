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
package com.reshiftsecurity.plugins.intellij.common;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class FindBugsPluginUtil {

	private FindBugsPluginUtil() {
	}

	@NotNull
	public static IdeaPluginDescriptor getIdeaPluginDescriptor() {
		final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(FindBugsPluginConstants.PLUGIN_ID));
		if (plugin == null) {
			throw new IllegalStateException(FindBugsPluginConstants.PLUGIN_ID + " could not be instantiated! PluginManager returned null!");
		}
		return plugin;
	}
}
