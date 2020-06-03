package com.reshiftsecurity.education;

public class ReshiftDevContent {
    private String sectionTitle;
    private String sectionHtml;

    public ReshiftDevContent(String sectionTitle, String sectionHtml) {
        this.sectionTitle = sectionTitle;
        this.sectionHtml = sectionHtml;
    }

    public String getSectionHtml() {
        return sectionHtml;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }
}
