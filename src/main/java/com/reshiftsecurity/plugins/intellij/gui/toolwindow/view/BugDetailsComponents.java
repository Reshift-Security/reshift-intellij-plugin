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
package com.reshiftsecurity.plugins.intellij.gui.toolwindow.view;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.*;
import com.reshiftsecurity.analytics.AnalyticsActionCategory;
import com.reshiftsecurity.education.DevContent;
import com.reshiftsecurity.education.VulnerabilityDetails;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;
import com.reshiftsecurity.plugins.intellij.service.EducationCachingService;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import icons.PluginIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.common.util.BugInstanceUtil;
import com.reshiftsecurity.plugins.intellij.gui.common.*;
import com.reshiftsecurity.plugins.intellij.gui.tree.view.BugTree;
import com.reshiftsecurity.plugins.intellij.resources.GuiResources;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("MagicNumber")
public final class BugDetailsComponents {

	private static final Logger LOGGER = Logger.getInstance(BugDetailsComponents.class);
	private static final String EDU_UMD_CS_FINDBUGS_PLUGINS_WEB_CLOUD = "edu.umd.cs.findbugs.plugins.webCloud";

	private HTMLEditorKit _htmlEditorKit;
	private JEditorPane _bugDetailsPane;
	private JEditorPane _explanationPane;
	private JPanel _bugDetailsPanel;
	private JPanel _explanationPanel;
	private JPanel _cloudCommentsPanel;
	private final ToolWindowPanel _parent;
	private double _splitPaneHorizontalWeight = 0.6;
	private SortedBugCollection _lastBugCollection;
	private BugInstance _lastBugInstance;
	private JTabbedPane _jTabbedPane;
	private MultiSplitPane _bugDetailsSplitPane;
	private VulnerabilityDetails _reshiftVulnDetails;
	private String _currentReshiftSection;
	private HashMap<String, Component> _reshiftContentPanes;
	private EducationCachingService _eduCacheService;
	private ChangeListener _eduChangeListener;
	AnalyticsService _analyticsService;

	BugDetailsComponents(final ToolWindowPanel toolWindowPanel) {
		_parent = toolWindowPanel;
		_htmlEditorKit = GuiResources.createHtmlEditorKit();
		_reshiftContentPanes = new HashMap<>();
		_eduCacheService = ServiceManager.getService(EducationCachingService.class);
		_analyticsService = AnalyticsService.getInstance();
		_eduChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				if (index >= 0 && index < sourceTabbedPane.getTabCount()) {
					_analyticsService.recordAction(
							AnalyticsActionCategory.ISSUE_REPORT_EDU, sourceTabbedPane.getToolTipTextAt(index));
				}
			}
		};
	}

	JTabbedPane getTabbedPane() {
		if (_jTabbedPane == null) {
			if (SystemInfo.isMac) {
				// use JTabbedPane because JBTabbedPane does not work with tabPlacement=RIGHT
				// on OS X (Aqua) at least with IDEA 14.1.4 (141.1532.4) and OS X 10.10 Yosemite.
				//noinspection UndesirableClassUsage
				_jTabbedPane = new JTabbedPane(SwingConstants.RIGHT);
			} else {
				_jTabbedPane = new JBTabbedPane(SwingConstants.RIGHT);
				((JBTabbedPane) _jTabbedPane).setTabComponentInsets(JBUI.insets(0, 0, 0, 5));
			}

			_jTabbedPane.setFocusable(false);
			_jTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			_jTabbedPane.addChangeListener(this._eduChangeListener);
			_analyticsService.recordAction(AnalyticsActionCategory.OPEN_PLUGIN_WINDOW);

			resetTabPane();
		}

		return _jTabbedPane;
	}

	private Component getReshiftContentPane(DevContent reshiftContent) {
		ExplanationEditorPane reshiftContentPane = new ExplanationEditorPane();
		reshiftContentPane.setBorder(JBUI.Borders.empty(10));
		reshiftContentPane.setEditable(false);
		reshiftContentPane.setContentType("text/html");
		reshiftContentPane.setEditorKit(_htmlEditorKit);
		reshiftContentPane.addHyperlinkListener(this::editorPaneHyperlinkUpdate);
		try (StringReader reader = new StringReader(reshiftContent.getContentWithTitle())) {
			reshiftContentPane.setToolTipText(edu.umd.cs.findbugs.L10N
					.getLocalString("tooltip.longer_description", "This gives more details on the detected vulnerability"));
			reshiftContentPane.read(reader, "html bug description");
		} catch (final IOException e) {
			reshiftContentPane.setText("Could not find bug description: " + e.getMessage());
			LOGGER.warn(e.getMessage(), e);
		}
		scrollRectToVisible(reshiftContentPane);

		final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(reshiftContentPane);
		//scrollPane.setBorder(BorderFactory.createCompoundBorder(new CustomLineBorder(new JBColor(new Color(208, 206, 203), new Color(170, 168, 165)), 1, 0, 0, 0), new CustomLineBorder(new JBColor(new Color(98, 95, 89), new Color(71, 68, 62)), 1, 0, 0, 0)));

		JPanel reshiftPanel = new JPanel();
		reshiftPanel.setBorder(JBUI.Borders.empty());
		reshiftPanel.setLayout(new BorderLayout());
		reshiftPanel.add(scrollPane, BorderLayout.CENTER);

		_reshiftContentPanes.put(reshiftContent.getTitle(), reshiftPanel);

		return reshiftPanel;
	}

	private Icon getReshiftSectionIcon(DevContent devContent) {
		switch (devContent.getTitle()) {
			case "Fixes":
				return PluginIcons.RESHIFT_FIXES;
			case "Impact":
				return PluginIcons.RESHIFT_IMPACT;
			case "Overview":
				return PluginIcons.RESHIFT_OVERVIEW;
			case "References":
				return PluginIcons.RESHIFT_REFERENCES;
			case "Tales":
				return PluginIcons.RESHIFT_TALES;
			case "Testing":
				return PluginIcons.RESHIFT_TESTING;
			case "Examples":
				return PluginIcons.RESHIFT_EXAMPLES;
			default:
				return PluginIcons.RESHIFT_ICON;
		}
	}

	private void resetTabPane() {
		if (_jTabbedPane == null) {
			return;
		}
		_jTabbedPane.removeAll();
		if (SystemInfo.isMac) {
			// Aqua LF will rotate content
			_jTabbedPane.addTab("", PluginIcons.RESHIFT_ICON, getBugDetailsSplitPane(), "Security Expert Tools/Resources to fix vulnerabilities");
		} else {
			_jTabbedPane.addTab(null, new VerticalTextIcon("", true, PluginIcons.RESHIFT_ICON), getBugDetailsSplitPane(), "Security Expert Tools/Resources to fix vulnerabilities");
		}

		_jTabbedPane.setMnemonicAt(0, KeyEvent.VK_1);


		if (Plugin.getByPluginId(EDU_UMD_CS_FINDBUGS_PLUGINS_WEB_CLOUD) != null) {
			if (SystemInfo.isMac) {
				// Aqua LF will rotate content
				_jTabbedPane.addTab("Comments", PluginIcons.FINDBUGS_CLOUD_ICON, getCloudCommentsPanel(), "Comments from the FindBugs Cloud");
			} else {
				_jTabbedPane.addTab(null, new VerticalTextIcon("Comments", true, PluginIcons.FINDBUGS_CLOUD_ICON), getCloudCommentsPanel(), "Comments from the FindBugs Cloud");
			}
			_jTabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		}

	}

	public void clearReshiftTabs() {
		clearReshiftTabs(false);
	}

	public void clearReshiftTabs(boolean removeAllTabs) {
		if (removeAllTabs) {
			_jTabbedPane.removeAll();
		} else {
			resetTabPane();
		}
		_reshiftContentPanes.clear();
	}

	private void refreshReshiftTabs() {
		getTabbedPane();
		// Get Reshift contents and set tabs
		populateReshiftDevContent();
		if (_reshiftVulnDetails != null && !_reshiftVulnDetails.isEmpty()) {
			clearReshiftTabs(true);
			int tabIndex = 0;
			for (DevContent reshiftSection : _reshiftVulnDetails.getDevContent()) {
				Icon tabIcon = getReshiftSectionIcon(reshiftSection);
				String tabTooltip = reshiftSection.getTitle();
				Component tabComponent = getReshiftContentPane(reshiftSection);
				_jTabbedPane.insertTab(
					null,
					tabIcon,
					tabComponent,
					tabTooltip,
					tabIndex);

				tabIndex++;
			}
		}
	}

	private Component getBugDetailsSplitPane() {
		if (_bugDetailsSplitPane == null) {
			_bugDetailsSplitPane = new MultiSplitPane();
			_bugDetailsSplitPane.setContinuousLayout(true);
			final String layoutDef = "(ROW weight=1.0 (COLUMN weight=1.0 top bottom))";
			final MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
			final MultiSplitLayout multiSplitLayout = _bugDetailsSplitPane.getMultiSplitLayout();
			multiSplitLayout.setDividerSize(3);
			multiSplitLayout.setModel(modelRoot);
			multiSplitLayout.setFloatingDividers(true);
			_bugDetailsSplitPane.add(getBugDetailsPanel(), "top");
			_bugDetailsSplitPane.add(getBugExplanationPanel(), "bottom");


		}
		return _bugDetailsSplitPane;
	}

	private JPanel getBugDetailsPanel() {
		if (_bugDetailsPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getBugDetailsPane());

			_bugDetailsPanel = new JPanel();
			_bugDetailsPanel.setBorder(JBUI.Borders.empty());
			_bugDetailsPanel.setLayout(new BorderLayout());
			_bugDetailsPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _bugDetailsPanel;
	}

	private JEditorPane getBugDetailsPane() {
		if (_bugDetailsPane == null) {
			_bugDetailsPane = new BugDetailsEditorPane();
			_bugDetailsPane.setBorder(JBUI.Borders.empty(5));
			_bugDetailsPane.setEditable(false);
			_bugDetailsPane.setContentType(UIUtil.HTML_MIME);
			_bugDetailsPane.setEditorKit(_htmlEditorKit);
			_bugDetailsPane.addHyperlinkListener(evt -> {
				if (_parent != null) {
					handleDetailsClick(evt);
				}
			});
		}

		return _bugDetailsPane;
	}

	private JPanel getBugExplanationPanel() {
		if (_explanationPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getExplanationPane());
			//scrollPane.setBorder(BorderFactory.createCompoundBorder(new CustomLineBorder(new JBColor(new Color(208, 206, 203), new Color(170, 168, 165)), 1, 0, 0, 0), new CustomLineBorder(new JBColor(new Color(98, 95, 89), new Color(71, 68, 62)), 1, 0, 0, 0)));

			_explanationPanel = new JPanel();
			_explanationPanel.setBorder(JBUI.Borders.empty());
			_explanationPanel.setLayout(new BorderLayout());
			_explanationPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _explanationPanel;
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private JEditorPane getExplanationPane() {
		if (_explanationPane == null) {
			_explanationPane = new ExplanationEditorPane();
			_explanationPane.setBorder(JBUI.Borders.empty(10));
			_explanationPane.setEditable(false);
			_explanationPane.setContentType("text/html");
			_explanationPane.setEditorKit(_htmlEditorKit);
			_explanationPane.addHyperlinkListener(this::editorPaneHyperlinkUpdate);
		}

		return _explanationPane;
	}

	private JPanel getCloudCommentsPanel() {
		throw new UnsupportedOperationException();
	}


	private void handleDetailsClick(final HyperlinkEvent evt) {
		if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			if (_parent != null) {
				final String desc = evt.getDescription();
				if ("#class".equals(desc)) {
					final BugTreePanel bugTreePanel = _parent.getBugTreePanel();
					final BugTree tree = bugTreePanel.getBugTree();
					if (bugTreePanel.isScrollToSource()) {
						tree.getScrollToSourceHandler().scollToSelectionSource();
					} else {
						bugTreePanel.setScrollToSource(true);
						tree.getScrollToSourceHandler().scollToSelectionSource();
						bugTreePanel.setScrollToSource(false);
					}
				} else if ("#comments".equals(desc)) {
					getTabbedPane().setSelectedComponent(getCloudCommentsPanel());
				}
			}
		}
	}

	private void editorPaneHyperlinkUpdate(final HyperlinkEvent evt) {
		try {
			if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				final URL url = evt.getURL();
				BrowserUtil.browse(url);
				_explanationPane.setPage(url);
			}
		} catch (final Exception e) {
			LOGGER.debug(e);
		}
	}

	@SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@SuppressWarnings({"HardCodedStringLiteral"})
	void setBugsDetails(@NotNull final BugInstance bugInstance) {
		final int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		final MethodAnnotation methodAnnotation = BugInstanceUtil.getPrimaryMethod(bugInstance);
		final FieldAnnotation fieldAnnotation = BugInstanceUtil.getPrimaryField(bugInstance);

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<h2>");
		html.append(bugInstance.getAbridgedMessage());
		html.append("</h2><p/>");

		html.append("<table border=0><tr valign=top><td valign=top>");
		html.append("<h3>Class:</h3>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<a href='#class'><u>");
		html.append(BugInstanceUtil.getSimpleClassName(bugInstance));
		html.append("</u></a>");
		html.append(" <font color='gray'>(");
		final String packageName = BugInstanceUtil.getPackageName(bugInstance);
		html.append(packageName);
		html.append(")</font>");

		if (lines[0] > -1) {
			final boolean singleLine = lines[1] == lines[0];
			if (singleLine) {
				html.append(" line ");
			} else {
				html.append(" lines ");
			}
			html.append(lines[0]);
			if (!singleLine) {
				html.append('-').append(lines[1]);
			}
		}
		html.append("</ul>");

		if (methodAnnotation != null) {
			html.append("<p><h3>Method:</p>");
			html.append("<ul>");
			html.append("<li>");

			if ("<init>".equals(methodAnnotation.getMethodName())) {
				html.append(BugInstanceUtil.getJavaSourceMethodName(bugInstance)).append("&lt;init&gt; <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			} else {
				html.append(BugInstanceUtil.getMethodName(bugInstance)).append(" <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			}
			html.append("</li>");
			html.append("</ul>");
		}

		if (fieldAnnotation != null) {
			html.append("<p><h3>Field:</p>");
			html.append("<ul>");
			html.append("<li>");
			html.append(BugInstanceUtil.getFieldName(bugInstance));
			html.append("</li>");
			html.append("</ul>");
		}

		html.append("<p><h3>Priority:</p>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<span width='15px' height='15px;' id='").append(BugInstanceUtil.getPriorityString(bugInstance)).append("'> &nbsp; &nbsp; </span>&nbsp;");
		html.append(BugInstanceUtil.getPriorityTypeString(bugInstance));
		html.append("</li>");
		html.append("</ul>");
		html.append("</td><td width='20px'>&nbsp;</td><td valign=top>");

		html.append("<h3>Problem classification:</h3>");
		html.append("<ul>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugCategoryDescription(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugTypeDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugType(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugPatternShortDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");

		final Iterable<BugAnnotation> annotations = bugInstance.getAnnotationsForMessage(false);
		if (annotations.iterator().hasNext()) {
			html.append("<p><h3>Notes:</p>");
			html.append("<ul>");
			for (final BugAnnotation annotation : annotations) {
				html.append("<li>").append(annotation.toString(bugInstance.getPrimaryClass())).append("</li>");
			}
			html.append("</ul>");
		}

		final DetectorFactory detectorFactory = bugInstance.getDetectorFactory();
		if (detectorFactory != null) {
			html.append("<li>");
			html.append(detectorFactory.getShortName());
			html.append(" <font color='gray'>(");
			html.append(createBugsAbbreviation(detectorFactory));
			html.append(")</font>");
			html.append("</li>");
		}
		html.append("</ul>");
		html.append("</tr></table>");
		html.append("</body></html>");

		// FIXME: set Suppress actions hyperlink

		_bugDetailsPane.setText(html.toString());
		scrollRectToVisible(_bugDetailsPane);

	}

	void setBugExplanation(final SortedBugCollection bugCollection, final BugInstance bugInstance) {
		_lastBugCollection = bugCollection;
		_lastBugInstance = bugInstance;
		refreshDetailsShown();
	}

	private void populateReshiftDevContent() {
		if (_lastBugInstance != null) {
			if (_reshiftVulnDetails == null) {
				_reshiftVulnDetails = _eduCacheService.getEducationContent(_lastBugInstance.getType());
			} else if (!_reshiftVulnDetails.getVulnerabilityType().equalsIgnoreCase(_lastBugInstance.getType())) {
				_reshiftVulnDetails = _eduCacheService.getEducationContent(_lastBugInstance.getType());
			}
		}
	}

	private String getCurrentReshiftExplanation() {
		populateReshiftDevContent();
		if (StringUtils.isEmpty(_currentReshiftSection)) {
			_currentReshiftSection = "Overview";
		}

		if (_reshiftVulnDetails != null) {
			Optional<DevContent> bugDevContent = _reshiftVulnDetails.getDevContentByTitle(_currentReshiftSection);
			if (bugDevContent.isPresent()) {
				return bugDevContent.get().getContentWithTitle();
			}
		}
		return "";
	}

	private void refreshDetailsShown() {
		String html = BugInstanceUtil.getDetailHtml(_lastBugInstance);
		String reshiftSectionContent = getCurrentReshiftExplanation();

		if (!StringUtils.isEmpty(reshiftSectionContent)) {
			refreshReshiftTabs();
		} else {
			// no need for BufferedReader
			try (StringReader reader = new StringReader(html)) {
				_explanationPane.setToolTipText(edu.umd.cs.findbugs.L10N
						.getLocalString("tooltip.longer_description", "This gives more details on the detected vulnerability"));
				_explanationPane.read(reader, "html bug description");
			} catch (final IOException e) {
				_explanationPane.setText("Could not find bug description: " + e.getMessage());
				LOGGER.warn(e.getMessage(), e);
			}
			scrollRectToVisible(_explanationPane);
		}
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private static void scrollRectToVisible(final JEditorPane pane) {
		SwingUtilities.invokeLater(() -> pane.scrollRectToVisible(new Rectangle(0, 0, 0, 0)));
	}

	void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * _splitPaneHorizontalWeight);
		final int expHeight = (int) (height);
		final int detailsHeight = (int) (height * 0); // hide details pane for now; might need it later

		//if(_bugDetailsPanel.getPreferredSize().width != newWidth && _bugDetailsPanel.getPreferredSize().height != detailsHeight) {
		_bugDetailsPanel.setPreferredSize(new Dimension(width, detailsHeight));
		_bugDetailsPanel.setSize(new Dimension(width, detailsHeight));
		//_bugDetailsPanel.doLayout();
		_bugDetailsPanel.validate();
		//_parent.validate();
		//}

		//if(_explanationPanel.getPreferredSize().width != newWidth && _explanationPanel.getPreferredSize().height != expHeight) {
		_explanationPanel.setPreferredSize(new Dimension(width, expHeight));
		_explanationPanel.setSize(new Dimension(width, expHeight));
		//_explanationPanel.doLayout();
		_explanationPanel.validate();
		getBugDetailsSplitPane().validate();
		//_parent.validate();
		//}

		for (Component rPane : _reshiftContentPanes.values()) {
			rPane.setPreferredSize(new Dimension(width, expHeight));
			rPane.setSize(new Dimension(width, expHeight));
			rPane.validate();
		}
	}

	public void issueUpdated(final BugInstance bug) {
		//noinspection ObjectEquality
		if (bug == _lastBugInstance) {
			refreshDetailsShown();
		}
	}

	public double getSplitPaneHorizontalWeight() {
		return _splitPaneHorizontalWeight;
	}

	public void setSplitPaneHorizontalWeight(final double splitPaneHorizontalWeight) {
		_splitPaneHorizontalWeight = splitPaneHorizontalWeight;
	}

	public void clear() {
		if (_bugDetailsPane != null) {
			_bugDetailsPane.setText(null);
		}
		if (_explanationPane != null) {
			_explanationPane.setText(null);
		}
	}

	public static String createBugsAbbreviation(final DetectorFactory factory) {
		final Collection<BugPattern> patterns = factory.getReportedBugPatterns();
		return patterns.stream().map(BugPattern::getAbbrev)
				.distinct().collect(Collectors.joining("|"));
	}

	private static class BugDetailsEditorPane extends JEditorPane {
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}

	private static class ExplanationEditorPane extends JEditorPane {
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}
}
