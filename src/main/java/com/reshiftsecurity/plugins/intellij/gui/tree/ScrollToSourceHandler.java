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
package com.reshiftsecurity.plugins.intellij.gui.tree;

import com.reshiftsecurity.plugins.intellij.gui.toolwindow.view.BugTreePanel;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class ScrollToSourceHandler extends com.intellij.ui.AutoScrollToSourceHandler {

	private final BugTreePanel _panel;


	public ScrollToSourceHandler(final BugTreePanel panel) {
		_panel = panel;
	}


	@Override
	protected boolean isAutoScrollMode() {
		return _panel.isScrollToSource();
	}


	@Override
	protected void setAutoScrollMode(final boolean flag) {
		_panel.setScrollToSource(flag);
	}


	public void scollToSelectionSource() {
		onMouseClicked(_panel.getBugTree());
	}

	// FIXME: call super.install(JTree tree)


}
