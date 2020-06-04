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

package com.reshiftsecurity.plugins.intellij.gui.common;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Component;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public final class ScrollPaneFacade implements ScrollPaneConstants {

	private static final Logger LOGGER = Logger.getInstance(ScrollPaneFacade.class.getName());


	private ScrollPaneFacade() {
	}


	@SuppressWarnings( {"UndesirableClassUsage"})
	public static JScrollPane createScrollPane(@Nullable final Component view, final int vsbPolicy, final int hsbPolicy) {
		return new JBScrollPane(view, vsbPolicy, hsbPolicy);
	}


	public static JScrollPane createScrollPane(final Component view) {
		return createScrollPane(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}


	public static JScrollPane createScrollPane(final int vsbPolicy, final int hsbPolicy) {
		return createScrollPane(null, vsbPolicy, hsbPolicy);
	}


	public static JScrollPane createScrollPane() {
		return createScrollPane(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
