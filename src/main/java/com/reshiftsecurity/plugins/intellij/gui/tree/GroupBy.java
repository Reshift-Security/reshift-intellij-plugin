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

import com.reshiftsecurity.results.FindBugsRankRenamer;
import edu.umd.cs.findbugs.BugRankCategory;
import edu.umd.cs.findbugs.I18N;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.core.Bug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum GroupBy {

	BugShortDescription("by bug short description"),
	BugCategory("by bug category"),
	BugType("by bug type"),
	Class("by class"),
	Package("by package"),
	Priority("by priority"),
	BugRank("by bug rank");

	private final String _description;

	GroupBy(final String description) {
		_description = description;
	}

	public String getDescription() {
		return _description;
	}

	public static List<GroupBy> getGroupByList() {
		final List<GroupBy> list = new ArrayList<GroupBy>();
		list.addAll(Arrays.asList(GroupBy.values()));

		return list;
	}

	public static String getBugCategory(@NotNull final Bug bug) {
		return BugInstanceComparator.BugInstanceCategoryComparator.getCategory(bug);
	}

	public static String getGroupName(final GroupBy groupBy, @NotNull final Bug bug) {

		if (!Locale.ENGLISH.equals(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
		}

		final String groupName;
		switch (groupBy) {

			case BugCategory:
				final String category = BugInstanceComparator.BugInstanceCategoryComparator.getCategory(bug);
				groupName = I18N.instance().getBugCategoryDescription(category);
				break;
			case BugShortDescription:
				groupName = BugInstanceComparator.BugInstanceShortDescrComparator.getShortDescr(bug);
				break;
			case BugType:
				final String type = BugInstanceComparator.BugInstanceTypeComparator.getTypeDescription(bug);
				groupName = I18N.instance().getBugTypeDescription(type);
				break;
			case Class:
				groupName = BugInstanceComparator.BugInstanceClassComparator.getClassName(bug);
				break;
			case Package:
				groupName = BugInstanceComparator.BugInstancePackageComparator.getPackageName(bug);
				break;
			case Priority:
				groupName = FindBugsRankRenamer.getRankNameByPriorityValue(bug.getInstance().getPriority());
				break;
			case BugRank:
				groupName = FindBugsRankRenamer.rename(bug.getInstance().getBugRankCategory());
				break;
			default:
				throw new IllegalStateException("Unknown group order: " + groupBy);
		}
		return groupName;
	}

	/**
	 * @param groupBy the primary group
	 * @return the specific sort order group
	 * @see GroupBy#BugCategory
	 * @see GroupBy#Class
	 * @see GroupBy#Package
	 * @see GroupBy#Priority
	 */
	public static GroupBy[] getSortOrderGroup(final GroupBy groupBy) {

		// Always group by Priority
		return new GroupBy[]{Priority, BugType};
//		switch (groupBy) {
//
//			case BugCategory:
//				return new GroupBy[]{BugCategory, BugType, BugShortDescription}; // FIXME: 2:Package, 3:Class, 4:Priority
//			case Class:
//				return new GroupBy[]{Class, BugCategory, BugType, BugShortDescription}; // FIXME: 1:Package, 3:Priority
//			case Package:
//				return new GroupBy[]{Package, BugCategory, BugType, BugShortDescription}; // FIXME: 2:Priority, 3:Class
//			case Priority:
//				return new GroupBy[]{Priority, BugCategory, BugType, BugShortDescription}; // FIXME: 2:Package, 3:Class
//			case BugRank:
//				return new GroupBy[]{BugRank, /*BugCategory,*/ BugType, BugShortDescription};
//			default:
//				throw new IllegalStateException("Unknown sort order group: " + groupBy);
//		}
	}

	// FIXME: getAvailGroupsForPrimaryGroup ??? static !!??

	public static GroupBy[] getAvailableGroups(final GroupBy[] currentGroupBy) {
		final List<GroupBy> result = new ArrayList<GroupBy>();
		final List<GroupBy> list = Arrays.asList(currentGroupBy);

		for (final GroupBy groupBy : GroupBy.values()) {
			if (!list.contains(groupBy)) {
				result.add(groupBy);
			}
		}

		return result.toArray(new GroupBy[result.size()]);
	}
}
