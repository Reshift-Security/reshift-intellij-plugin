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

import com.intellij.openapi.components.ServiceManager;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.analytics.AnalyticsActionCategory;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {
    List<AnalyticsAction> actions;
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
    private final String EVENT_ACTION_DEFAULT = "click";
    private final String EVENT_ACTION_KEY = "ea";
    private final String USER_ID_KEY = "cid";
    private final String MEASUREMENT_ID_KEY = "tid";
    private final String ACTION_VALUE_KEY = "ev";
    private final String ACTION_CATEGORY_KEY = "ec";
    private final String ACTION_LABEL_KEY = "el";
    private final String DOC_PATH = "%2Fintellij";
    private final String DOC_PATH_KEY = "dp";

    private String userID;
    private String applicationVersion;
    private String measurementID;

    public AnalyticsService() {
        this.actions = new ArrayList<>();
        this.applicationVersion = VersionManager.getVersion();
        this.userID = getUserIdentifier();
        this.measurementID = "UA-149586212-2";
    }

    public static AnalyticsService getInstance() {
        return ServiceManager.getService(AnalyticsService.class);
    }

    public void recordAction(AnalyticsActionCategory category, String label) {
        this.actions.add(new AnalyticsAction(category));
        this.processActions();
    }

    public void recordAction(AnalyticsActionCategory category) {
        this.actions.add(new AnalyticsAction(category));
        this.processActions();
    }

    public void recordMetric(AnalyticsActionCategory category, Integer value) {
        this.actions.add(new AnalyticsAction(category, value));
        this.processActions();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
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

    private String buildActionParameters(AnalyticsAction action) {
        if (action == null) {
            return "";
        }
        StringBuilder actionBuilder = new StringBuilder()
            .append(PROTOCOL_VERSION_KEY + "=" + PROTOCOL_VERSION + "&")
            .append(APP_ID_KEY + "=" + APP_ID + "&")
            .append(APP_NAME_KEY + "=" + URLEncoder.encode(APP_NAME, StandardCharsets.UTF_8) + "&")
            .append(USER_ID_KEY + "=" + this.userID + "&")
            .append(APP_VERSION_KEY + "=" + this.applicationVersion + "&")
            .append(MEASUREMENT_ID_KEY + "=" + this.measurementID + "&")
            .append(ACTION_LABEL_KEY + "=" + action.getLabel() + "&")
            .append(DOC_PATH_KEY + "=" + DOC_PATH);
        if (action.getMetric() != null) {
            actionBuilder.append(ACTION_VALUE_KEY + "=" + action.getMetric() + "&");
            actionBuilder.append(EVENT_ACTION_KEY + "=report&");
            actionBuilder.append(HIT_TYPE_KEY + "=transaction&");
        } else {
            actionBuilder.append(HIT_TYPE_KEY + "=" + HIT_TYPE + "&");
            actionBuilder.append(EVENT_ACTION_KEY + "=" + EVENT_ACTION_DEFAULT + "&");
        }
        actionBuilder.append(ACTION_CATEGORY_KEY + "=" + action.getCategory().toString());

        return actionBuilder.toString();
    }

    private String buildBatchPayload() {
        StringBuilder payloadBuilder = new StringBuilder();
        for(AnalyticsAction action : this.actions) {
            payloadBuilder.append(buildActionParameters(action) + "\n");
        }
        return payloadBuilder.toString();
    }

    private void processActions() {
        if (!AnalyticsServiceSettings.getInstance().sendAnonymousUsage) {
            return;
        }
        try {
            String requestPayload = buildActionParameters(this.actions.get(0));
            URL requestURL = new URL(ANALYTICS_BASE_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) requestURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            // set payload - START
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            os.write(requestPayload.getBytes());
            os.flush();
            os.close();
            // set payload - END
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.actions = new ArrayList<>();
        }
    }
}
