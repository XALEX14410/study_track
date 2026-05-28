package com.jesuslara.studytrack.fragments;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.navigation.BasePlaceholderFragment;

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
