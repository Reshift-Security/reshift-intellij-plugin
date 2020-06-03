package com.reshiftsecurity.analytics;

import java.time.ZonedDateTime;

public class AnalyticsAction {
    private AnalyticsActionType actionType;
    private ZonedDateTime actionDateTime;
    private String actionResultComments;
    private int actionResultCount;

    public AnalyticsAction(AnalyticsActionType type) {
        this.actionType = type;
        this.actionDateTime = ZonedDateTime.now();
    }

    public void setActionResultComments(String comments) {
        this.actionResultComments = comments;
    }

    public void setActionResultCount(int actionResultCount) {
        this.actionResultCount = actionResultCount;
    }
}
