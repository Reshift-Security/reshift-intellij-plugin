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
package com.reshiftsecurity.plugins.intellij.gui.settings;

import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.util.New;

import java.util.Map;

final class DetectorNode extends AbstractDetectorNode {

	@NotNull
	private final DetectorFactory detector;

	@NotNull
	private final Map<String, Map<String, Boolean>> enabled;

	DetectorNode(
			@NotNull final DetectorFactory detector,
			@NotNull final Map<String, Map<String, Boolean>> enabled
	) {
		super(detector.getShortName());
		this.detector = detector;
		this.enabled = enabled;
	}

	@Override
	boolean isGroup() {
		return false;
	}

	@Override
	@Nullable
	Boolean getEnabled() {
		final Map<String, Boolean> enabledMap = enabled.get(detector.getPlugin().getPluginId());
		if (enabledMap != null) {
			final Boolean ret = enabledMap.get(detector.getShortName());
			if (ret != null) {
				return ret;
			}
		}
		return detector.isDefaultEnabled();
	}

	@Override
	void setEnabled(@Nullable final Boolean enabled) {
		final boolean e = null != enabled && enabled;
		final String pluginId = detector.getPlugin().getPluginId();
		if (e != detector.isDefaultEnabled()) {
			Map<String, Boolean> settings = this.enabled.get(pluginId);
			if (settings == null) {
				settings = New.map();
				this.enabled.put(pluginId, settings);
			}
			settings.put(detector.getShortName(), e);
		} else {
			final Map<String, Boolean> settings = this.enabled.get(pluginId);
			if (settings != null) {
				settings.remove(detector.getShortName());
				if (settings.isEmpty()) {
					this.enabled.remove(pluginId);
				}
			}
		}
	}

	@NotNull
	DetectorFactory getDetector() {
		return detector;
	}
}
