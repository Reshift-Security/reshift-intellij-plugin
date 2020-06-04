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
package com.reshiftsecurity.plugins.intellij.common.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public final class FileUtilFb {

	private FileUtilFb() {
	}

	public static void mkdirs(@NotNull final File directory) throws IOException {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IOException("Can not create directory " + directory);
			}
		}
	}

	@Nullable
	public static String toSystemDependentName(@NonNls @Nullable final String fileName) {
		if (StringUtil.isEmptyOrSpaces(fileName)) {
			return null;
		}
		return FileUtil.toSystemDependentName(fileName);
	}

	@Nullable
	public static String toSystemIndependentName(@NonNls @Nullable final String fileName) {
		if (StringUtil.isEmptyOrSpaces(fileName)) {
			return null;
		}
		return FileUtil.toSystemIndependentName(fileName);
	}
}
