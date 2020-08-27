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

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(
        name = "ReshiftSecurity-IDEA-Analytics",
        storages = {@Storage(file = "reshift-analytics-settings.xml")}
)
public final class AnalyticsServiceSettings implements PersistentStateComponent<AnalyticsServiceSettings> {
    @Tag
    private Boolean sendAnonymousUsage = false;

    @Tag
    private Boolean consentResponseReceived = false;

    @Nullable
    @Override
    public AnalyticsServiceSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AnalyticsServiceSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {

    }

    @Override
    public void initializeComponent() {

    }

    public static AnalyticsServiceSettings getInstance() {
        return ServiceManager.getService(AnalyticsServiceSettings.class);
    }

    public void recordConsent(boolean consent) {
        this.consentResponseReceived = true;
        this.sendAnonymousUsage = consent;
        AnalyticsService.getInstance().recordConsent(consent);
    }

    public void recordConsentDismiss() {
        AnalyticsService.getInstance().recordDismissConsentAction();
    }

    public boolean hasConsent() {
        return this.consentResponseReceived;
    }
}
