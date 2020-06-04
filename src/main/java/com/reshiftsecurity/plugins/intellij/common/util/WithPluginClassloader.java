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
package com.reshiftsecurity.plugins.intellij.common.util;

import com.intellij.openapi.util.NotNullComputable;
import com.intellij.openapi.util.Throwable2Computable;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;

/**
 * This must be used when FindBugs will create a SAXReader because this use the default DocumentFactory instance
 * (org.dom4j.DocumentFactory#getInstance) which is created by createSingleton which uses Class.forName.
 * <p>
 * Note that all our classes (incl transitive) must be loaded by the PluginClassLoader instance of SpotBugs-IDEA plugin.
 * Check: DebugUtil.dumpClasses(TheClass.class);
 */
public final class WithPluginClassloader {
	@NotNull
	public static final ClassLoader PLUGIN_CLASS_LOADER = ProjectSettings.class.getClassLoader();

	private WithPluginClassloader() {
	}

	public static <V, E extends Throwable, E2 extends Throwable> V compute(@NotNull final Throwable2Computable<V, E, E2> computable) throws E, E2 {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader cl = currentThread.getContextClassLoader();
		try {
			currentThread.setContextClassLoader(PLUGIN_CLASS_LOADER);
			return computable.compute();
		} finally {
			currentThread.setContextClassLoader(cl);
		}
	}

	@NotNull
	public static <V> V notNull(@NotNull final NotNullComputable<V> computable) {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader cl = currentThread.getContextClassLoader();
		try {
			currentThread.setContextClassLoader(PLUGIN_CLASS_LOADER);
			return computable.compute();
		} finally {
			currentThread.setContextClassLoader(cl);
		}
	}
}
