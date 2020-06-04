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
package com.reshiftsecurity.plugins.intellij.gui.tree.model;

import icons.PluginIcons;
import com.reshiftsecurity.plugins.intellij.gui.tree.GroupBy;

import javax.swing.Icon;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractNodeDescriptor<E> {

	GroupBy _groupBy;
	int _depthFromRoot;
	String _simpleName;
	String _groupName;
	int _memberCount; // FIXME: ??? needed ???
	String _tooltip;

	Icon _expandedIcon = PluginIcons.TREENODE_OPEN_ICON;
	Icon _collapsedIcon = PluginIcons.TREENODE_CLOSED_ICON;


	public final GroupBy getGroupBy() {
		return _groupBy;
	}


	public final int getDepth() {
		return _depthFromRoot;
	}


	public final String getSimpleName() {
		return _simpleName;
	}


	public final String getGroupName() {
		return _groupName;
	}


	public int getMemberCount() {
		return _memberCount;
	}


	public String getTooltip() {
		return _tooltip;
	}


	public Icon getExpandedIcon() {
		return _expandedIcon;
	}


	public Icon getCollapsedIcon() {
		return _collapsedIcon;
	}


	public AbstractNodeDescriptor<E> getElement() {
		return this;
	}


}
