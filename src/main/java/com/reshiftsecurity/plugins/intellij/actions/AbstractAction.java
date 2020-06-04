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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.common.util.IdeaUtilImpl;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.ToolWindowPanel;

abstract class AbstractAction extends AnAction {

	@Override
	public final void update(@NotNull final AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		if (project == null || !project.isInitialized() || !project.isOpen()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null || !toolWindow.isAvailable()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		updateImpl(
				e,
				project,
				toolWindow,
				FindBugsState.get(project)
		);
	}

	abstract void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	);

	@Override
	public final void actionPerformed(@NotNull final AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		if (project == null) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null || !toolWindow.isAvailable()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		actionPerformedImpl(
				e,
				project,
				toolWindow,
				FindBugsState.get(project)
		);
	}

	abstract void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	);
}
