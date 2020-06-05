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
package com.reshiftsecurity.plugins.intellij.preferences;

import edu.umd.cs.findbugs.config.UserPreferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import java.util.Locale;


/**
 * @see UserPreferences#setEffort(String)
 */
public enum AnalysisEffort { // FIXME Cleanup unused

	MIN(UserPreferences.EFFORT_MIN, "effort.minimal", "Minimal", 10),
	DEFAULT(UserPreferences.EFFORT_DEFAULT, "effort.default", "Default", 20),
	MAX(UserPreferences.EFFORT_MAX, "effort.maximal", "Maximal", 30);

	@NotNull
	private final String effortLevel;

	@NotNull
	private final String propertyKey;

	@Deprecated
	private final String message;

	@Deprecated
	private final int value;


	AnalysisEffort(
			@NotNull final String effortLevel,
			@NotNull @PropertyKey(resourceBundle = ResourcesLoader.BUNDLE) String propertyKey,
			final String message,
			final int value
	) {
		this.effortLevel = effortLevel;
		this.propertyKey = propertyKey;
		this.message = message;
		this.value = value;
	}


	@NotNull
	public String getEffortLevel() {
		return effortLevel;
	}


	public String getText() {
		return ResourcesLoader.getString(propertyKey);
	}

	@NotNull
	public static AnalysisEffort of(@Nullable final String value) {
		for (final AnalysisEffort effort : values()) {
			if (effort.effortLevel.equalsIgnoreCase(value)) {
				return effort;
			}
		}
		return AnalysisEffort.DEFAULT;
	}

	public static AnalysisEffort valueOfLevel(final String effortLevel) {
		return valueOf(effortLevel.toUpperCase(Locale.ENGLISH));
	}


	public String getMessage() {
		return message;
	}


	public int getValue() {
		return value;
	}


	public static String getEffortLevelByValue(final int value) {
		for (final AnalysisEffort effort : AnalysisEffort.values()) {
			if (effort.getValue() == value) {
				return effort.getEffortLevel();
			}
		}
		return AnalysisEffort.DEFAULT.getEffortLevel();
	}

	@Override
	public String toString() {
		return getText();
	}
}
