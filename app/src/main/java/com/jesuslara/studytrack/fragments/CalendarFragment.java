package com.jesuslara.studytrack.fragments;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.navigation.BasePlaceholderFragment;

public class CalendarFragment extends BasePlaceholderFragment {

    @Override
    protected int getIconResId() {
        return R.drawable.ic_calendar;
    }

    @Override
    protected int getTitleResId() {
        return R.string.placeholder_calendar_title;
    }

    @Override
    protected int getDescriptionResId() {
        return R.string.placeholder_calendar_desc;
    }
}
