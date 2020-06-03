package com.reshiftsecurity.analytics;

public enum ReshiftAnalyticsActionType {
    SCAN, CLICK;

    @Override
    public String toString() {
        return name();
    }
}
