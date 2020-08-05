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

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.ServiceManager;
import com.reshiftsecurity.analytics.AnalyticsEntry;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import okhttp3.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

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
    private final String ACTION_VALUE_KEY = "cm1";
    private final String EVENT_VALUE_KEY = "ev";
    private final String ACTION_CATEGORY_KEY = "ec";
    private final String ACTION_LABEL_KEY = "el";
    private final String DOC_PATH = "%2Fintellij";
    private final String DOC_PATH_KEY = "dp";

    private String userID;
    private String applicationVersion;
    private String measurementID;
    private String userAgent;

    public AnalyticsService() {
        this.entries = new ArrayList<>();
        this.applicationVersion = VersionManager.getVersion();
        this.userID = getUserIdentifier();
        this.measurementID = "UA-149586212-2";
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

    public void recordConsentAction(boolean consent) {
        AnalyticsAction action = consent ? AnalyticsAction.SETTINGS_GATHER_DATA_YES : AnalyticsAction.SETTINGS_GATHER_DATA_NO;
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
        agentString.append(String.format("%s, ", System.getProperty("os.name")));
        agentString.append(String.format("Reshift Plugin %s, ", VersionManager.getVersion()));
        agentString.append(String.format("%s", ApplicationInfo.getInstance().getFullApplicationName()));
        return agentString.toString();
    }

    private String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            hexString.append(String.format("%02X", hash[i]));
        }
        return hexString.toString();
    }

    private String getUserIdentifier() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            String mAddress = sb.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(mAddress.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
            .append(HIT_TYPE_KEY + "=" + HIT_TYPE + "&");
        if (entry.getMetric() != null) {
            actionBuilder.append(ACTION_VALUE_KEY + "=" + entry.getMetric() + "&");
            actionBuilder.append(EVENT_VALUE_KEY + "=" + entry.getMetric() + "&");
        }
        actionBuilder.append(ACTION_CATEGORY_KEY + "=" + URLEncoder.encode(entry.getCategory(), StandardCharsets.UTF_8) + "&")
            .append(ACTION_LABEL_KEY + "=" + URLEncoder.encode(entry.getLabel(), StandardCharsets.UTF_8) + "&")
            .append(EVENT_ACTION_KEY + "=" + URLEncoder.encode(entry.getActionName(), StandardCharsets.UTF_8));

        return actionBuilder.toString();
    }

    private String buildBatchPayload() {
        StringBuilder payloadBuilder = new StringBuilder();
        for(AnalyticsEntry action : this.entries) {
            payloadBuilder.append(buildEntryParameters(action) + "\n");
        }
        return payloadBuilder.toString();
    }

    private void processActions() {
        this.processActions(false);
    }

    private void processActions(boolean forConsent) {
        if (!forConsent) {
            if (!AnalyticsServiceSettings.getInstance().hasConsent()) {
                return;
            }
        }
        if (this.entries.size() > 0) {
            try {
                String requestPayload = buildEntryParameters(this.entries.get(0));
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
