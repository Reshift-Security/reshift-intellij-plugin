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

package com.reshiftsecurity.plugins.intellij.service;

import com.intellij.openapi.components.Service;
import com.reshiftsecurity.education.DevContent;
import com.reshiftsecurity.education.EducationService;
import com.reshiftsecurity.education.VulnerabilityDetails;
import com.reshiftsecurity.plugins.intellij.common.ExtendedProblemDescriptor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EducationCachingService {
    private List<VulnerabilityDetails> contentCache;
    private final String SECTION_OVERVIEW="Overview";

    public EducationCachingService() {
        this.contentCache = new ArrayList<>();
    }

    private Optional<VulnerabilityDetails> getContentFromCache(String issueType) {
        for (VulnerabilityDetails vulnerabilityDetails : contentCache) {
            if (vulnerabilityDetails.getVulnerabilityType().equalsIgnoreCase(issueType)) {
                return Optional.of(vulnerabilityDetails);
            }
        }
        return Optional.empty();
    }

    public VulnerabilityDetails getEducationContent(String issueType) {
        Optional<VulnerabilityDetails> cachedDetails = getContentFromCache(issueType);
        if (cachedDetails.isPresent()) {
            return cachedDetails.get();
        }
        VulnerabilityDetails details = EducationService.getVulnerabilityDetails(issueType);
        this.contentCache.add(details);
        return details;
    }

    public String getContentSection(String issueType, String sectionName, boolean textOnly) {
        VulnerabilityDetails details = getEducationContent(issueType);
        Optional<DevContent> devContent = details.getDevContentByTitle(sectionName);
        String contentSection = "";
        if (devContent.isPresent()) {
            contentSection = String.format("<b>%s</b>: %s", details.getFriendlyTypeName(), devContent.get().getContent());
            if (textOnly) {
                contentSection = Jsoup.parse(contentSection).text();
            }
        }
        return contentSection;
    }

    public String getBriefOverview(final List<ExtendedProblemDescriptor> problemDescriptors, boolean textOnly) {
        if (problemDescriptors.size() > 0) {
            return getContentSection(problemDescriptors.get(0).getBug().getInstance().getType(), SECTION_OVERVIEW, textOnly);
        }
        return "";
    }
}
