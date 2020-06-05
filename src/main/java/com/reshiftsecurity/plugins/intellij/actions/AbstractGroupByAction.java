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
package com.reshiftsecurity.plugins.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.core.AbstractSettings;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;
import com.reshiftsecurity.plugins.intellij.core.WorkspaceSettings;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;
import com.reshiftsecurity.plugins.intellij.gui.tree.GroupBy;

import java.util.Arrays;

abstract class AbstractGroupByAction extends AbstractToggleAction {

	private final GroupBy _groupBy;

	AbstractGroupByAction(@NotNull final GroupBy groupBy) {
		_groupBy = groupBy;
	}

	@Override
	final boolean isSelectedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final ToolWindowPanel panel,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	) {

		final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);
		final String groupByProperty = workspaceSettings.toolWindowGroupBy;
		final boolean equals = _groupBy.name().equals(groupByProperty);
		final GroupBy[] sortOrderGroup = GroupBy.getSortOrderGroup(_groupBy);
		if (equals && !Arrays.equals(panel.getBugTreePanel().getGroupBy(), sortOrderGroup)) {
			panel.getBugTreePanel().setGroupBy(sortOrderGroup);
		}
		return equals;
	}

	@Override
	final void setSelectedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final ToolWindowPanel panel,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings,
			final boolean select
	) {

		if (select) {
			final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);
			workspaceSettings.toolWindowGroupBy = _groupBy.name();
			panel.getBugTreePanel().setGroupBy(GroupBy.getSortOrderGroup(_groupBy));
		}
	}
}
