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

import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.analytics.AnalyticsActionCategory;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {
    List<AnalyticsAction> actions;
    private final String ANALYTICS_BASE_URL = "https://www.google-analytics.com/batch";
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
    private final String USER_ID_KEY = "uid";
    private final String MEASUREMENT_ID_KEY = "tid";
    private final String SCAN_RESULTS_METRIC_KEY = "cm1";
    private final String ACTION_CATEGORY_KEY = "ec";

    private String userID;
    private String applicationVersion;
    private String measurementID;

    public AnalyticsService() {
        this.actions = new ArrayList<>();
        this.applicationVersion = VersionManager.getVersion();
        this.userID = getUserIdentifier();
        this.measurementID = "UA-XXXX-Y"; // TODO: confirm this value
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
            .append(APP_NAME_KEY + "=" + APP_NAME + "&")
            .append(USER_ID_KEY + "=" + this.userID + "&")
            .append(APP_VERSION_KEY + "=" + this.applicationVersion + "&")
            .append(MEASUREMENT_ID_KEY + "=" + this.measurementID + "&");
        if (action.getMetric() != null) {
            actionBuilder.append(SCAN_RESULTS_METRIC_KEY + "=" + action.getMetric() + "&");
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
        if (StringUtils.isEmpty(this.userID)) {
            // FIXME: how to handle this scenario?
            return;
        }
        // FIXME: look into setting date time of each event since we are sending in bulk
        // Checkout "Queue Time" https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#qt
        if (actions.size() >= 20) { // 20 is a limit on batch uploads by Google.
            new Thread(() -> {
                String batchPayload = this.buildBatchPayload();
                try {
                    URL obj = new URL(ANALYTICS_BASE_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    // For POST only - START
                    httpURLConnection.setDoOutput(true);
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(batchPayload.getBytes());
                    os.flush();
                    os.close();
                    // For POST only - END
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.actions = new ArrayList<>();
                }
            }).start();
        }
    }
}
