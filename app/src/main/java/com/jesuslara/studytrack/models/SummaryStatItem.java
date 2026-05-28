package com.jesuslara.studytrack.models;

public class SummaryStatItem {

    private final int iconResId;
    private final String value;
    private final String title;

    public SummaryStatItem(int iconResId, String value, String title) {
        this.iconResId = iconResId;
        this.value = value;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }
}
