package com.example.studytrack.fragments;

import com.example.studytrack.R;
import com.example.studytrack.navigation.BasePlaceholderFragment;

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
