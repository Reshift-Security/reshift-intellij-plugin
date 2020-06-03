package com.reshiftsecurity.analytics;

import java.time.ZonedDateTime;

public class ReshiftAnalyticsAction {
    private ReshiftAnalyticsActionType actionType;
    private ZonedDateTime actionDateTime;
    private String actionResultComments;
    private int actionResultCount;

    public ReshiftAnalyticsAction(ReshiftAnalyticsActionType type) {
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
