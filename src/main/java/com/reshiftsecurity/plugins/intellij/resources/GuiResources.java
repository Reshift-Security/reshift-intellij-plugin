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
package com.reshiftsecurity.plugins.intellij.resources;

import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.UIUtil;
import org.jdesktop.swingx.color.ColorUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.Color;

@SuppressWarnings({"HardcodedFileSeparator"})
public class GuiResources {

	public static final Color HIGH_PRIORITY_COLOR = JBColor.RED;
	public static final Color MIDDLE_PRIORITY_COLOR = JBColor.YELLOW;
	public static final Color LOW_PRIORITY_COLOR = JBColor.GREEN;
	public static final Color EXP_PRIORITY_COLOR = JBColor.BLACK;

	private static final StyleSheet EDITORPANE_STYLESHEET;

	static {

		final String black = ColorUtil.toHexString(JBColor.black);
		final String gray = ColorUtil.toHexString(JBColor.gray);
		final String blue = ColorUtil.toHexString(JBColor.blue);
		final String green = ColorUtil.toHexString(JBColor.green);
		final String yellow = ColorUtil.toHexString(JBColor.yellow);
		final String red = ColorUtil.toHexString(JBColor.red);
		final String cremeWhite = ColorUtil.toHexString(new JBColor(new Color(0x005555), JBColor.green));
		final String fontColor = UIUtil.isUnderDarcula() ? "#bbbbbb" : ColorUtil.toHexString(JBColor.black);
		final int fontSize = JBUIScale.scale(12);
		final int h1FontSize = JBUIScale.scale(16);
		final int h2FontSize = JBUIScale.scale(14);

		EDITORPANE_STYLESHEET = new StyleSheet();
		EDITORPANE_STYLESHEET.addRule("body {font-size: " + fontSize + "pt; color: " + fontColor + "}");
		EDITORPANE_STYLESHEET.addRule("p {margin-top:4px;margin-bottom:8px;}");
		EDITORPANE_STYLESHEET.addRule("code {font-family: courier; font-size: " + fontSize + "pt; background-color:#f5f5f5;padding:4px;}");
		EDITORPANE_STYLESHEET.addRule("pre {color: " + gray + "; font-family: courier; font-size: " + fontSize + "pt; padding:9px; background-color:#f5f5f5; border:1px solid #cccccc;color:#333333;}");
		EDITORPANE_STYLESHEET.addRule("blockquote {padding: 10px 20px; margin: 0 0 20px; border-left: 5px solid #bbbbbb;");
		EDITORPANE_STYLESHEET.addRule("a {color: " + blue + "; font-decoration: underline}");
		EDITORPANE_STYLESHEET.addRule("li {margin-left: 10px; list-style-type: none}");
		EDITORPANE_STYLESHEET.addRule("#Low {background-color: " + green + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#Medium {background-color: " + yellow + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#High {background-color: " + red + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#Exp {background-color: " + black + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("H1 {color: " + cremeWhite + ";  font-size: " + h1FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H1 a {color: " + cremeWhite + ";  font-size: " + h1FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H2, .fakeH2 {color: " + cremeWhite + ";  font-size: " + h2FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H3 {color: " + cremeWhite + ";  font-size: " + fontSize + "pt; font-weight: bold;}");
	}

	@NotNull
	public static HTMLEditorKit createHtmlEditorKit() {
		return new HTMLEditorKit() {
			@Override
			public StyleSheet getStyleSheet() {
				return GuiResources.EDITORPANE_STYLESHEET;
			}
		};
	}

	public static final Color HIGHLIGHT_COLOR = new JBColor(new Color(219, 219, 137), new Color(189, 189, 120));
	public static final Color HIGHLIGHT_COLOR_DARKER = new JBColor(new Color(135, 135, 69, 254), new Color(112, 112, 56, 254));
	public static final Color HIGHLIGHT_COLOR_LIGHTER = new JBColor(new Color(255, 255, 204), new Color(86, 86, 43, 254));


	private GuiResources() {
	}
}
