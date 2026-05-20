package com.example.studytrack.fragments;

import com.example.studytrack.R;
import com.example.studytrack.navigation.BasePlaceholderFragment;

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
