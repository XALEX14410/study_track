package com.example.studytrack.models;

public class UpcomingEventItem {

    private final String subject;
    private final String date;
    private final String priority;
    private final int priorityColor;

    public UpcomingEventItem(String subject, String date, String priority, int priorityColor) {
        this.subject = subject;
        this.date = date;
        this.priority = priority;
        this.priorityColor = priorityColor;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getPriority() {
        return priority;
    }

    public int getPriorityColor() {
        return priorityColor;
    }
}
