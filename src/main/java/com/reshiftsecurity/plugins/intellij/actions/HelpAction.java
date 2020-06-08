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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.text.DateFormatUtil;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import com.reshiftsecurity.plugins.intellij.common.util.FindBugsUtil;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.gui.common.BalloonTipFactory;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import javax.swing.event.HyperlinkEvent;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public final class HelpAction extends AbstractAction {
	// TODO: fill proper values
	private static final String DOWNLOADS_WEBSITE = "https://spotbugs.readthedocs.io/en/stable/installing.html";

	private static final String A_HREF_COPY = "#copy";

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		e.getPresentation().setEnabled(true);
		e.getPresentation().setVisible(true);
	}

	@Override
	void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		toolWindow.setShowStripeButton(true);

		BalloonTipFactory.showToolWindowInfoNotifier(
				project,
				createHelpInfo().toString(),
				evt -> {
					if (HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())) {
						if (A_HREF_COPY.equals(evt.getDescription())) {
							final String info = createProductInfo().toString();
							CopyPasteManager.getInstance().setContents(new StringSelection(info));
						} else {
							BrowserUtil.browse(evt.getURL());
						}
					}
				}
		);
	}

	@NonNls
	@NotNull
	private static StringBuilder createHelpInfo() {
		final StringBuilder ret = new StringBuilder();
		ret.append("<h2>").append(VersionManager.getFullVersion()).append("</h2>");
		ret.append("Website: <a href='").append(VersionManager.getWebsite()).append("'>").append(VersionManager.getWebsite()).append("</a>");
		ret.append("<br>");
		ret.append("Support & Feedback: <a href='mailto:dev@reshiftsecurity.com'>dev@reshiftsecurity.com</a>");
		ret.append("<br/>");
		ret.append("<br/>");
		ret.append("Would you like to integrate Reshift into your CI pipline? Sign up today for free on <a href='")
				.append(PluginConstants.RESHIFT_SITE_URL)
				.append("'>reshiftsecurity.com</a>");
//		ret.append("Issue tracker: <a href='").append(VersionManager.getIssueTracker()).append("'>").append(VersionManager.getIssueTracker()).append("</a>");
//		ret.append("<h3>SpotBugs ").append(FindBugsUtil.getFindBugsFullVersion()).append("</h3>");
//		ret.append("Website: <a href='").append(Version.WEBSITE).append("'>").append(Version.WEBSITE).append("</a>");
//		ret.append("<br>");
//		ret.append("Download: <a href='").append(DOWNLOADS_WEBSITE).append("'>").append(DOWNLOADS_WEBSITE).append("</a>");
		ret.append("<br><br>");
		ret.append("<p>");
		ret.append("<a href='").append(A_HREF_COPY).append("'>").append(ResourcesLoader.getString("help.copyInfos")).append("</a>");
		ret.append("</p>");
		return ret;
	}

	/**
	 * Based on com.intellij.ide.actions.AboutPopup
	 */
	@NonNls
	@NotNull
	public static StringBuilder createProductInfo() {

		final StringBuilder ret = new StringBuilder("\n");
		ret.append("Product Infos");
		ret.append("\n    Reshift Security: ").append(FindBugsUtil.getFindBugsFullVersion());
		ret.append("\n    Reshift IntelliJ plugin: ").append(VersionManager.getVersion());

		boolean ideaVersionAvailable = false;
		try {
			final ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
			if (appInfo != null) {
				ret.append("\n    IDEA: ").append(appInfo.getFullApplicationName());
				ret.append("\n    IDEA-Build: ").append(appInfo.getBuild().asString());
				final Calendar cal = appInfo.getBuildDate();
				ret.append(", ").append(DateFormatUtil.formatAboutDialogDate(cal.getTime()));
				if (appInfo.getBuild().isSnapshot()) {
					ret.append(" ").append(new SimpleDateFormat("HH:mm, ").format(cal.getTime()));
				}
				ideaVersionAvailable = true;
			}
		} catch (final Throwable e) { // maybe ApplicationInfoEx API changed
			e.printStackTrace();
		}
		if (!ideaVersionAvailable) {
			ret.append("\n    IDEA: [Please type IDEA version here]");
		}

		final Properties systemProps = System.getProperties();
		final String javaVersion = systemProps.getProperty("java.runtime.version", systemProps.getProperty("java.version", "unknown"));
		final String arch = systemProps.getProperty("os.arch", "");
		ret.append("\n    JRE: ").append(javaVersion).append(" ").append(arch);

		final String vmVersion = systemProps.getProperty("java.vm.name", "unknown");
		final String vmVendor = systemProps.getProperty("java.vendor", "unknown");
		ret.append("\n    JVM: ").append(vmVersion).append(" ").append(vmVendor);

		return ret;
	}
}
