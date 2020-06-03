package com.reshiftsecurity.analytics;

public enum AnalyticsActionType {
    SCAN, CLICK;

    @Override
    public String toString() {
        return name();
    }
}
