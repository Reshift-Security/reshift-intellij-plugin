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
import com.reshiftsecurity.analytics.AnalyticsEntry;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import com.reshiftsecurity.plugins.intellij.common.util.HashUtil;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsService {
    List<AnalyticsEntry> entries;
    // NOTE: use https://www.google-analytics.com/debug/collect for debugging
    private final String ANALYTICS_BASE_URL = "https://www.google-analytics.com/collect";
    private final String APP_ID = "com.reshiftsecurity.plugins.intellij";
    private final String APP_NAME = "Reshift Intellij Plugin";
    private final String PROTOCOL_VERSION = "1";
    private final String HIT_TYPE = "event";
    private final String NUM_ISSUES_METRIC_KEY = "cm1";
    private final String NUM_FIXES_METRIC_KEY = "cm2";
    private final String DOC_PATH = "%2Fintellij";
    private final String DOC_LOCATION_VALUE = "https%3A%2F%2www.reshiftsecurity.com%2Fintellij";
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
        this.userID = generateUserIdentifier();
        this.measurementID = "UA-149586212-2";
        this.operatingSystem = System.getProperty("os.name");
        this.intellijPlatform = String.format("%s %s", appInfo.getVersionName(), appInfo.getBuild().getProductCode());
        this.intellijVersion = appInfo.getFullVersion();
        this.userAgent = buildUserAgent();
        // Record initial "pageview" hit
        this.recordInitialIntellijOpen();
    }

    public static AnalyticsService getInstance() {
        return ServiceManager.getService(AnalyticsService.class);
    }

    public void recordInitialIntellijOpen() {
        this.recordConsentExemptAction(AnalyticsAction.OPEN_INTELLIJ);
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

    public String getUserIdentifier() {
        return this.userID;
    }

    private String buildUserAgent() {
        StringBuilder agentString = new StringBuilder();
        agentString.append(String.format("%s, ", this.operatingSystem));
        agentString.append(String.format("Reshift Plugin %s, ", this.applicationVersion));
        agentString.append(String.format("%s", ApplicationInfo.getInstance().getFullApplicationName()));
        return agentString.toString();
    }

    private String generateUserIdentifier() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                Enumeration ni = NetworkInterface.getNetworkInterfaces();
                while (ni.hasMoreElements()) {
                    // read the last interface in the list (usually it's the default one
                    network = (NetworkInterface) ni.nextElement();
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

    private String getHitTypeByAction(AnalyticsAction action) {
        if (AnalyticsAction.OPEN_INTELLIJ.equals(action)) {
            return "pageview";
        }
        return HIT_TYPE;
    }

    private String buildEntryParameters(AnalyticsEntry entry) {
        if (entry == null) {
            return "";
        }
        StringBuilder actionBuilder = new StringBuilder()
            .append("v=" + PROTOCOL_VERSION + "&")
            .append("aid=" + APP_ID + "&")
            .append("an=" + HashUtil.urlEncode(APP_NAME) + "&")
            .append("cid=" + this.userID + "&")
            .append("av=" + this.applicationVersion + "&")
            .append("tid=" + this.measurementID + "&")
            .append("dp=" + DOC_PATH + "&")
            .append("t=" + getHitTypeByAction(entry.getAction()) + "&")
            .append(IDEA_PLATFORM_KEY + "=" + HashUtil.urlEncode(intellijPlatform) + "&")
            .append(IDEA_VERSION_KEY + "=" + intellijVersion + "&")
            .append(DEV_OS_KEY + "=" + HashUtil.urlEncode(operatingSystem) + "&")
            .append("dl=" + DOC_LOCATION_VALUE + "&");
        if (entry.getMetric() != null) {
            actionBuilder.append(getMetricKeyByAction(entry.getAction()) + "=" + entry.getMetric() + "&");
            actionBuilder.append("ev=" + entry.getMetric() + "&");
        }
        if (entry.isDimensionValueSet()) {
            String dimKey = getDimensionKeyByAction(entry.getAction());
            if (!StringUtils.isEmpty(dimKey))
                actionBuilder.append(dimKey + "=" + HashUtil.urlEncode(entry.getDimensionValue()) + "&");
        }
        actionBuilder.append("ec=" + HashUtil.urlEncode(entry.getCategory()) + "&")
            .append("el=" + HashUtil.urlEncode(entry.getLabel()) + "&")
            .append("ea=" + HashUtil.urlEncode(entry.getActionName()));

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
            if (!AnalyticsServiceSettings.getInstance().sendAnonymousUsage()) {
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

                client.newCall(request).execute().body().close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.entries = new ArrayList<>();
            }
        }
    }
}
