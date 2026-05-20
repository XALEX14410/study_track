package com.example.studytrack.fragments;

import com.example.studytrack.R;
import com.example.studytrack.navigation.BasePlaceholderFragment;

public class NotificationsFragment extends BasePlaceholderFragment {

    @Override
    protected int getIconResId() {
        return R.drawable.ic_notifications;
    }

    @Override
    protected int getTitleResId() {
        return R.string.placeholder_notifications_title;
    }

    @Override
    protected int getDescriptionResId() {
        return R.string.placeholder_notifications_desc;
    }
}
