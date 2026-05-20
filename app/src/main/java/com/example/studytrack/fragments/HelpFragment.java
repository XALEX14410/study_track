package com.example.studytrack.fragments;

import com.example.studytrack.R;
import com.example.studytrack.navigation.BasePlaceholderFragment;

public class HelpFragment extends BasePlaceholderFragment {

    @Override
    protected int getIconResId() {
        return R.drawable.ic_help;
    }

    @Override
    protected int getTitleResId() {
        return R.string.placeholder_help_title;
    }

    @Override
    protected int getDescriptionResId() {
        return R.string.placeholder_help_desc;
    }
}
