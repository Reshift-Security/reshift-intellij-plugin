/*
 * Copyright 2020 SpotBugs plugin contributors
 *
 * This file is part of IntelliJ SpotBugs plugin.
 *
 * IntelliJ SpotBugs plugin is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * IntelliJ SpotBugs plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IntelliJ SpotBugs plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */

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
                // send actions async and reset
                String actionsJson = this.jsonSerializer.toJson(this.actions);
                this.actions = new ArrayList<>();
            }).start();
        }
    }
}
