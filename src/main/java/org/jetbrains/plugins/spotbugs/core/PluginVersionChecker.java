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
package org.jetbrains.plugins.spotbugs.core;

import com.google.gson.GsonBuilder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.RequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.spotbugs.common.util.IoUtil;
import org.jetbrains.plugins.spotbugs.resources.ResourcesLoader;

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
		return "https://api.github.com/repos/reshift-projects/reshift-intellij-plugin/releases/latest";
	}

	private static class LatestRelease {
		public String name;
	}
}
