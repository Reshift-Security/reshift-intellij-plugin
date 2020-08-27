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

package com.reshiftsecurity.plugins.intellij.core;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.intellij.util.xmlb.Constants;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.util.HashUtil;
import com.reshiftsecurity.plugins.intellij.common.util.SourceCodeUtil;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;
import com.reshiftsecurity.results.SecurityIssue;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@State(
        name = "ReshiftSecurity-Intellij-Report",
        storages = {
                @Storage("$PROJECT_CONFIG_DIR$/reshiftsecurity-intellij-report.xml")
        }
)
public class SecurityReportService implements PersistentStateComponent<SecurityReportService> {

    private VirtualFile[] projectSourceFiles;

    private Project currentProject;

    @Tag
    private Boolean isGitProject = false;

    @Tag
    private String gitBranch = "";

    @Tag
    private String gitRepo = "";

    @Tag
    private String projectName;

    @Tag
    public int totalVulnerabilityCount = 0;

    @Tag
    public int newVulnerabilityCount = 0;

    @Tag
    public int totalFixCount = 0;

    @Tag
    public int newFixCount = 0;

    @Tag(value = "securityIssues")
    @XCollection(elementName = Constants.LIST)
    public SmartList<SecurityIssue> securityIssues;

    @Nullable
    @Override
    public SecurityReportService getState() {
        return this;
    }

    @Override
    public void loadState(final SecurityReportService state) { XmlSerializerUtil.copyBean(state, this); }

    @NotNull
    public static SecurityReportService getInstance(@NotNull final Project project) {
        SecurityReportService service = ServiceManager.getService(project, SecurityReportService.class);
        service.projectName = project.getName();
        service.currentProject = project;
        service.projectSourceFiles = ProjectRootManager.getInstance(project).getContentSourceRoots();
        try {
            Repository existingRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(String.format("%s/.git", project.getBasePath())))
                    .build();
            service.isGitProject = true;
            Set<String> remoteNames = existingRepo.getRemoteNames();
            service.gitRepo = existingRepo.getConfig().getString(
                    ConfigConstants.CONFIG_KEY_REMOTE,
                    CollectionUtils.isEmpty(remoteNames) ? "origin" : remoteNames.stream().findFirst().get(),
                    ConfigConstants.CONFIG_KEY_URL);
            service.gitBranch = existingRepo.getBranch();
        } catch (IOException e) {
            // might not be a git repo, fail silently as this is not a core function of the plugin
            e.printStackTrace();
        }
        return service;
    }

    private boolean issueExistsInSet(SecurityIssue issue, SmartList<SecurityIssue> issueSet) {
        if (issueSet == null)
            return false;

        for (SecurityIssue i : issueSet) {
            if (i.isSameAs(issue)) {
                return true;
            }
        }
        return false;
    }

    private boolean issueExists(SecurityIssue issue) {
        return issueExistsInSet(issue, securityIssues);
    }

    public void addBugCollection(BugCollection bugCollection) {
        SmartList<SecurityIssue> latestIssues = new SmartList<>();
        SmartList<SecurityIssue> fixedIems = new SmartList<>();
        SmartList<SecurityIssue> newItems = new SmartList<>();

        for (BugInstance bug: bugCollection.getCollection()) {
            SourceLineAnnotation mainSourceLineAnnotation = bug.getPrimarySourceLineAnnotation();
            SecurityIssue issue = new SecurityIssue();
            issue.categoryName = bug.getType();
            issue.classFQN = mainSourceLineAnnotation.getClassName();
            issue.lineNumber = mainSourceLineAnnotation.getStartLine();
            issue.methodFullSignature = bug.getPrimaryMethod().getFullMethod(null);
            issue.code = SourceCodeUtil.getTrimmedSourceLine(projectSourceFiles, mainSourceLineAnnotation);
            issue.cweId = bug.getCWEid();
            issue.issueHash = HashUtil.hashThis(String.format("%s|%s", issue.cweId, bug.getInstanceKey()));
            issue.isNew = !issueExists(issue);
            if (issue.isNew) {
                newItems.add(issue);
                issue.detectionDatetime = LocalDateTime.now().toString();
            }
            latestIssues.add(issue);
        }

        if (securityIssues != null) {
            // in this case there might be existing issues in the last report (securityIssues)
            // that has been fixed. loop through and mark those as fixed in the new report
            for (SecurityIssue existingIssue: securityIssues) {
                if (!existingIssue.isFixed) { // only process issues that have not been fixed before
                    existingIssue.isFixed = !issueExistsInSet(existingIssue, latestIssues);
                    if (existingIssue.isFixed) {
                        fixedIems.add(existingIssue);
                        existingIssue.fixDatetime = LocalDateTime.now().toString();
                        latestIssues.add(existingIssue);
                    }
                }
            }
        }
        securityIssues = latestIssues;
        newFixCount = fixedIems.size();
        newVulnerabilityCount = newItems.size();
        totalFixCount += newFixCount;
        totalVulnerabilityCount = securityIssues.size();

        AnalyticsService.getInstance().recordMetric(AnalyticsAction.FIXES_METRIC, newFixCount);
    }
}
