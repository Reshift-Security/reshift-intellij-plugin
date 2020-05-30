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

package com.reshiftsecurity.education;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReshiftEducationService {
    public static ReshiftVulnerabilityDetails getVulnerabilityDetails(String vulnerabilityType) {
        List<ReshiftDevContent> devContent = new ArrayList<>();
        String rawHtml = getRawVulnerabilityDetails(vulnerabilityType);
        String[] sections = rawHtml.split("<h1>\\|");
        for (String section : sections) {
            if (!StringUtils.isEmpty(section)) {
                String titleEnd = "||</h1>";
                int titleEndIndex = section.indexOf(titleEnd);
                String sectionTitle = section.substring(1, titleEndIndex);
                String sectionContent = section.substring(titleEndIndex + titleEnd.length());
                String sectionTitleH1 = String.format("<h1>%s</h1>", sectionTitle);
                devContent.add(new ReshiftDevContent(sectionTitle,sectionTitleH1 + sectionContent));
            }
        }
        ReshiftVulnerabilityDetails details = new ReshiftVulnerabilityDetails(vulnerabilityType);
        details.getDevContent().addAll(devContent);
        return details;
    }

    public static String getRawVulnerabilityDetails(String vulnerabilityType) {
        // TODO: look into switching source data to JSON
        String html = "";
        String reshiftEduBaseUrl = System.getenv("RESHIFT_EDU_BASE_URL");
        if (StringUtils.isEmpty(reshiftEduBaseUrl)) {
            reshiftEduBaseUrl = "https://reshiftsecurity.com"; // TODO: fix this to correct URL
        }
        final String bugEduUrl = String.format("%s/%s/", reshiftEduBaseUrl, vulnerabilityType);
        try {
            html = Jsoup.connect(bugEduUrl).get().html();
            if (!StringUtils.isEmpty(html)) {
                Whitelist htmlTagWhiteList = Whitelist.relaxed();
                html = Jsoup.clean(html, htmlTagWhiteList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }
}
