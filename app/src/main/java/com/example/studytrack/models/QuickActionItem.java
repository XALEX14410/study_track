package com.example.studytrack.models;

public class QuickActionItem {

    private final int iconResId;
    private final String title;

    public QuickActionItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}
