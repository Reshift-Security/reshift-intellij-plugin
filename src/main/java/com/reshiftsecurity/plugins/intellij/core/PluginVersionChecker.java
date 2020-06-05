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
package com.reshiftsecurity.plugins.intellij.core;

import com.google.gson.GsonBuilder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.RequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.util.IoUtil;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import java.io.IOException;
import java.io.Reader;

final class PluginVersionChecker {

	private PluginVersionChecker() {
	}

	@Nullable
	static String getLatestVersion(@NotNull final ProgressIndicator indicator) throws IOException {
		final String url = getLatestReleaseUrl();
		indicator.setText(ResourcesLoader.getString("error.submitReport.retrieve", url));
		final RequestBuilder builder = HttpRequests
				.request(url)
				.accept("application/vnd.github.v2+json");
		return builder.connect(request -> {
			final Reader reader = request.getReader();
			try {
				LatestRelease latestRelease = new GsonBuilder().create().fromJson(reader, LatestRelease.class);
				return latestRelease.name;
			} finally {
				IoUtil.safeClose(reader);
			}
		});
	}

	@NotNull
	private static String getLatestReleaseUrl() {
		// https support only
		return "https://api.github.com/repos/softwaresecured/reshift-intellij-plugin/releases/latest";
	}

	private static class LatestRelease {
		public String name;
	}
}