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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.ServiceManager;
import com.reshiftsecurity.analytics.AnalyticsActionCategory;
import com.reshiftsecurity.analytics.AnalyticsEntry;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import com.reshiftsecurity.plugins.intellij.common.util.HashUtil;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsService {
    List<AnalyticsEntry> entries;
    // NOTE: use https://www.google-analytics.com/debug/collect for debugging
    private final String ANALYTICS_BASE_URL = "https://www.google-analytics.com/collect";
    private final String APP_ID = "com.reshiftsecurity.plugins.intellij";
    private final String APP_ID_KEY = "aid";
    private final String APP_NAME = "Reshift Intellij Plugin";
    private final String APP_NAME_KEY = "an";
    private final String APP_VERSION_KEY = "av";
    private final String PROTOCOL_VERSION = "1";
    private final String PROTOCOL_VERSION_KEY = "v";
    private final String HIT_TYPE = "event";
    private final String HIT_TYPE_KEY = "t";
    private final String EVENT_ACTION_KEY = "ea";
    private final String USER_ID_KEY = "cid";
    private final String MEASUREMENT_ID_KEY = "tid";
    private final String NUM_ISSUES_METRIC_KEY = "cm1";
    private final String NUM_FIXES_METRIC_KEY = "cm2";
    private final String EVENT_VALUE_KEY = "ev";
    private final String ACTION_CATEGORY_KEY = "ec";
    private final String ACTION_LABEL_KEY = "el";
    private final String DOC_PATH = "%2Fintellij";
    private final String DOC_PATH_KEY = "dp";
    private final String IDEA_PLATFORM_KEY = "cd1";
    private final String IDEA_VERSION_KEY = "cd2";
    private final String DEV_OS_KEY = "cd3";
    private final String OTHER_PLUGINS_KEY = "cd4";

    private String userID;
    private String applicationVersion;
    private String measurementID;
    private String userAgent;
    private String intellijPlatform;
    private String intellijVersion;
    private String operatingSystem;

    public AnalyticsService() {
        ApplicationInfo appInfo = ApplicationInfo.getInstance();
        this.entries = new ArrayList<>();
        this.applicationVersion = VersionManager.getVersion();
        this.userID = getUserIdentifier();
        this.measurementID = "UA-149586212-2";
        this.operatingSystem = System.getProperty("os.name");
        this.intellijPlatform = String.format("%s %s", appInfo.getVersionName(), appInfo.getBuild().getProductCode());
        this.intellijVersion = appInfo.getFullVersion();
        this.userAgent = buildUserAgent();
    }

    public static AnalyticsService getInstance() {
        return ServiceManager.getService(AnalyticsService.class);
    }

    public void recordDismissConsentAction() {
        AnalyticsAction action = AnalyticsAction.SETTINGS_GATHER_DATA_DISMISS;
        this.entries.add(new AnalyticsEntry(action));
        this.processActions(true);
    }

    public void recordInstall() {
        this.entries.add(new AnalyticsEntry(AnalyticsAction.INSTALL));
        List<IdeaPluginDescriptor> plugins = PluginManagerCore.getLoadedPlugins().stream()
                .filter(p -> p.getVendor() == null ? true :
                        !p.getVendor().startsWith("JetBrains")
                         && !PluginConstants.PLUGIN_ID.equalsIgnoreCase(p.getPluginId().getIdString()))
                .collect(Collectors.toList());
        for (IdeaPluginDescriptor plugin : plugins) {
            String pluginDimensionValue = String.format("%s %s", plugin.getName(), plugin.getVendor());
            AnalyticsEntry pluginEntry = new AnalyticsEntry(AnalyticsAction.OTHER_PLUGINS);
            pluginEntry.setDimensionValue(pluginDimensionValue);
            this.entries.add(pluginEntry);
        }
        processActions(true);
    }

    public void recordConsent(boolean consent) {
        AnalyticsAction action = consent ? AnalyticsAction.SETTINGS_GATHER_DATA_YES : AnalyticsAction.SETTINGS_GATHER_DATA_NO;
        recordConsentExemptAction(action);
    }

    public void recordConsentExemptAction(AnalyticsAction action) {
        this.entries.add(new AnalyticsEntry(action));
        this.processActions(true);
    }

    public void recordAction(AnalyticsAction action, String actionNameOverride) {
        this.entries.add(new AnalyticsEntry(action, null, actionNameOverride));
        this.processActions();
    }

    public void recordAction(AnalyticsAction action) {
        this.entries.add(new AnalyticsEntry(action));
        this.processActions();
    }

    public void recordMetric(AnalyticsAction action, Integer value) {
        this.entries.add(new AnalyticsEntry(action, value));
        this.processActions();
    }

    private String buildUserAgent() {
        StringBuilder agentString = new StringBuilder();
        agentString.append(String.format("%s, ", this.operatingSystem));
        agentString.append(String.format("Reshift Plugin %s, ", this.applicationVersion));
        agentString.append(String.format("%s", ApplicationInfo.getInstance().getFullApplicationName()));
        return agentString.toString();
    }

    private String getUserIdentifier() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                Iterator<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces().asIterator();
                while (ni.hasNext()) {
                    // read the last interface in the list (usually it's the default one
                    network = ni.next();
                }
            }
            byte[] mac = network.getHardwareAddress();

            if (mac == null) {
                mac = ip.getHostName().toUpperCase().getBytes();
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return HashUtil.hashThis(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getMetricKeyByAction(AnalyticsAction action) {
        return (action == AnalyticsAction.FIXES_METRIC ? NUM_FIXES_METRIC_KEY : NUM_ISSUES_METRIC_KEY);
    }

    private String getDimensionKeyByAction(AnalyticsAction action) {
        return (action == AnalyticsAction.OTHER_PLUGINS ? OTHER_PLUGINS_KEY : null);
    }

    private String buildEntryParameters(AnalyticsEntry entry) {
        if (entry == null) {
            return "";
        }
        StringBuilder actionBuilder = new StringBuilder()
            .append(PROTOCOL_VERSION_KEY + "=" + PROTOCOL_VERSION + "&")
            .append(APP_ID_KEY + "=" + APP_ID + "&")
            .append(APP_NAME_KEY + "=" + URLEncoder.encode(APP_NAME, StandardCharsets.UTF_8) + "&")
            .append(USER_ID_KEY + "=" + this.userID + "&")
            .append(APP_VERSION_KEY + "=" + this.applicationVersion + "&")
            .append(MEASUREMENT_ID_KEY + "=" + this.measurementID + "&")
            .append(DOC_PATH_KEY + "=" + DOC_PATH + "&")
            .append(HIT_TYPE_KEY + "=" + HIT_TYPE + "&")
            .append(IDEA_PLATFORM_KEY + "=" + URLEncoder.encode(intellijPlatform, StandardCharsets.UTF_8) + "&")
            .append(IDEA_VERSION_KEY + "=" + intellijVersion + "&")
            .append(DEV_OS_KEY + "=" + URLEncoder.encode(operatingSystem, StandardCharsets.UTF_8) + "&");
        if (entry.getMetric() != null) {
            actionBuilder.append(getMetricKeyByAction(entry.getAction()) + "=" + entry.getMetric() + "&");
            actionBuilder.append(EVENT_VALUE_KEY + "=" + entry.getMetric() + "&");
        }
        if (entry.isDimensionValueSet()) {
            String dimKey = getDimensionKeyByAction(entry.getAction());
            if (!StringUtils.isEmpty(dimKey))
                actionBuilder.append(dimKey + "=" + URLEncoder.encode(entry.getDimensionValue(), StandardCharsets.UTF_8) + "&");
        }
        actionBuilder.append(ACTION_CATEGORY_KEY + "=" + URLEncoder.encode(entry.getCategory(), StandardCharsets.UTF_8) + "&")
            .append(ACTION_LABEL_KEY + "=" + URLEncoder.encode(entry.getLabel(), StandardCharsets.UTF_8) + "&")
            .append(EVENT_ACTION_KEY + "=" + URLEncoder.encode(entry.getActionName(), StandardCharsets.UTF_8));

        return actionBuilder.toString();
    }

    private String buildBatchPayload() {
        StringBuilder payloadBuilder = new StringBuilder();
        for(AnalyticsEntry action : this.entries) {
            payloadBuilder.append(buildEntryParameters(action))
                    .append(this.entries.size() > 1 ? "\n" : "");
        }
        return payloadBuilder.toString();
    }

    private void processActions() {
        this.processActions(false);
    }

    private void processActions(boolean consentExempt) {
        if (!consentExempt) {
            if (!AnalyticsServiceSettings.getInstance().hasConsent()) {
                return;
            }
        }
        if (this.entries.size() > 0) {
            try {
                String requestPayload = buildBatchPayload();
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, requestPayload);
                Request request = new Request.Builder()
                        .url(ANALYTICS_BASE_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("User-Agent", this.userAgent)
                        .build();

                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.entries = new ArrayList<>();
            }
        }
    }
}
