package com.reshiftsecurity.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class ReshiftAnalyticsService {
    List<ReshiftAnalyticsAction> actions;
    Gson jsonSerializer;

    public ReshiftAnalyticsService() {
        this.actions = new ArrayList<>();
        this.jsonSerializer = new GsonBuilder().setPrettyPrinting().create();
    }

    public void recordAction(ReshiftAnalyticsAction action) {
        this.actions.add(action);
        this.processActions();
    }

    private void processActions() {
        if (actions.size() >= 100) {
            new Thread(() -> {
                // TODO: send actions async and reset
                String actionsJson = this.jsonSerializer.toJson(this.actions);
                this.actions = new ArrayList<>();
            }).start();
        }
    }
}
