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

package com.reshiftsecurity.analytics;

public class AnalyticsAction {
    private AnalyticsActionCategory category;
    private Integer metric;

    public AnalyticsAction(AnalyticsActionCategory category) {
        this.category = category;
    }
    public AnalyticsAction(AnalyticsActionCategory category, Integer metric) {
        this.category = category;
        this.metric = metric;
    }

    public AnalyticsActionCategory getCategory() {
        return category;
    }
    public void setCategory(AnalyticsActionCategory category) {
        this.category = category;
    }
    
    public Integer getMetric() {
        return this.metric;
    }
    public void setMetric(Integer metric) {
        this.metric = metric;
    }
}
