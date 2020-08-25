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
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;
import com.reshiftsecurity.results.SecurityIssue;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@State(
        name = "ReshiftSecurity-Intellij-Report",
        storages = {
                @Storage("$PROJECT_CONFIG_DIR$/reshiftsecurity-intellij-report.xml")
        }
)
public class SecurityReportService implements PersistentStateComponent<SecurityReportService> {

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
    @XCollection(elementName = "securityIssue")
    public Set<SecurityIssue> securityIssues;

    private boolean issueExistsInSet(SecurityIssue issue, Set<SecurityIssue> issueSet) {
        if (CollectionUtils.isEmpty(issueSet))
            return false;

        for (SecurityIssue i: issueSet) {
            if (i.instanceHash.equalsIgnoreCase(issue.instanceHash)) {
                return true;
            }
        }
        return false;
    }

    private boolean issueExists(SecurityIssue issue) {
        return issueExistsInSet(issue, securityIssues);
    }

    public void addBugCollection(BugCollection bugCollection) {
        Set<SecurityIssue> latestIssues = new HashSet<>();
        int newFixes = 0;
        int newVulnCount = 0;

        for (BugInstance bug: bugCollection) {
            SecurityIssue issue = new SecurityIssue();
            String methodAnnotation = bug.getPrimaryMethod().getFullMethod(null);
            issue.categoryName = bug.getType();
            issue.lineNumber = bug.getPrimarySourceLineAnnotation().getStartLine();
            issue.methodFQN = methodAnnotation.split("[(]")[0];
            issue.cweId = bug.getCWEid();
            issue.instanceHash = bug.getInstanceHash();
            issue.issueIdentifier = String.format("%s|%s", issue.cweId, bug.getInstanceKey());
            issue.isNew = !issueExists(issue);
            if (issue.isNew) {
                newVulnCount++;
                issue.detectionDatetime = LocalDateTime.now();
            }
            latestIssues.add(issue);
        }

        if (!CollectionUtils.isEmpty(securityIssues)) {
            // in this case there might be existing issues in the last report (securityIssues)
            // that has been fixed. loop through and mark those as fixed in the new report
            for (SecurityIssue existingIssue: securityIssues) {
                if (!existingIssue.isFixed) { // only process issues that have not been fixed before
                    existingIssue.isFixed = !issueExistsInSet(existingIssue, latestIssues);
                    if (existingIssue.isFixed) {
                        newFixes++;
                        existingIssue.fixDatetime = LocalDateTime.now();
                        latestIssues.add(existingIssue);
                    }
                }
            }
        }
        securityIssues = latestIssues;
        newFixCount = newFixes;
        newVulnerabilityCount = newVulnCount;
        totalFixCount += newFixCount;
        totalVulnerabilityCount = securityIssues.size();

        AnalyticsService.getInstance().recordMetric(AnalyticsAction.FIXES_METRIC, newFixCount);
    }
}
