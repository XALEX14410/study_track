package com.jesuslara.studytrack.fragments;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.navigation.BasePlaceholderFragment;

public class SubjectsFragment extends BasePlaceholderFragment {

    @Override
    protected int getIconResId() {
        return R.drawable.ic_subjects;
    }

    @Override
    protected int getTitleResId() {
        return R.string.placeholder_subjects_title;
    }

    @Override
    protected int getDescriptionResId() {
        return R.string.placeholder_subjects_desc;
    }
}
