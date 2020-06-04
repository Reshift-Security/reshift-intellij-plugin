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
package com.reshiftsecurity.plugins.intellij.gui.tree.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.Bug;
import com.reshiftsecurity.plugins.intellij.gui.tree.NodeVisitor;
import com.reshiftsecurity.plugins.intellij.gui.tree.RecurseNodeVisitor;
import com.reshiftsecurity.plugins.intellij.gui.tree.RecurseNodeVisitor.RecurseVisitCriteria;
import com.reshiftsecurity.plugins.intellij.gui.tree.view.MaskIcon;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

public class RootNode extends AbstractTreeNode<VisitableTreeNode> implements VisitableTreeNode {

	private static final Icon EXPAND_ICON = new MaskIcon(ResourcesLoader.loadIcon("bluesheild.svg"));
	private static final Icon COLLAPSE_ICON = EXPAND_ICON;

	private int _bugCount;
	private int _classesCount;
	private final List<VisitableTreeNode> _childs;
	private final RecurseNodeVisitor<RootNode> _recurseNodeVisitor = new RecurseNodeVisitor<RootNode>(this);


	public RootNode(final String simpleName) {
		setParent(null);
		_childs = new ArrayList<VisitableTreeNode>();
		_simpleName = simpleName;
		_bugCount = -1;
		_classesCount = 0;

		setCollapsedIcon(COLLAPSE_ICON);
		setExpandedIcon(EXPAND_ICON);
	}

	public int getBugCount() {
		return _bugCount;
	}

	public void setBugCount(final int bugCount) {
		_bugCount = bugCount;
	}

	public int getClassesCount() {
		return _classesCount;
	}

	public String getLinkHtml() {
		if (_bugCount > -1) {
			return new StringBuilder().append("<html><body>").append(" <a href='#more'>more...</a>").append("</body></html>").toString();
		}
		return "";
	}

	@Nullable
	BugInstanceGroupNode findChildNode(final Bug bug, final int depth, final String groupName) {
		final RecurseVisitCriteria criteria = new RecurseVisitCriteria(bug, depth, groupName);
		return _recurseNodeVisitor.findChildNode(criteria);
	}

	@NotNull
	List<Bug> getAllChildBugs() {
		final List<Bug> ret = New.arrayList();
		for (final TreeNode child : _childs) {
			if (child instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode node = (BugInstanceGroupNode) child;
				final List<Bug> bugs = node.getAllChildBugs();
				ret.addAll(ret.size(), bugs);
			}
		}
		return ret;
	}

	public void setClassesCount(final int classesCount) {
		_classesCount = classesCount;
	}

	@Override
	public String toString() {
		return "RootNode" +
				"{_bugCount=" + _bugCount +
				", _classesCount=" + _classesCount +
				", _childs=" + _childs +
				", _recurseNodeVisitor=" + _recurseNodeVisitor +
				'}';
	}

	@Override
	public void accept(final NodeVisitor visitor) {
		//visitor.visitGroupNode(this);
	}

	@Override
	public List<VisitableTreeNode> getChildsList() {
		return _childs;
	}

	@Override
	public RootNode getTreeNode() {
		return this;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return _childs.isEmpty();
	}
}
