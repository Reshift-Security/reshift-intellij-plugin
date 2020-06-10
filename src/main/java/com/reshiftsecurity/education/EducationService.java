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

package com.reshiftsecurity.education;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EducationService {

    private static final String reshiftEduBaseUrl = "https://d20h2meksv6k0s.cloudfront.net";
    private static final Type devContentType = new TypeToken<List<DevContent>>() {}.getType();

    public static VulnerabilityDetails getVulnerabilityDetails(String vulnerabilityType) {
        List<DevContent> devContent = new ArrayList<>();
        Gson gson = new Gson();
        InputStream input = null;
        try {
            input = new URL(getDevContentURL(vulnerabilityType)).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Reader jsonReader = null;
        try {
            jsonReader = new InputStreamReader(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        devContent = gson.fromJson(jsonReader, devContentType);
        VulnerabilityDetails details = new VulnerabilityDetails(vulnerabilityType);
        details.getDevContent().addAll(devContent);
        return details;
    }

    private static String getDevContentURL(String vulnerabilityType) {
        URI eduBaseUri = null;
        try {
            eduBaseUri = new URI(reshiftEduBaseUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return eduBaseUri.resolve(String.format("/%s/data.json", vulnerabilityType)).toString();
    }
}
