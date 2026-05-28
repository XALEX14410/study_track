package com.jesuslara.studytrack.fragments;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.navigation.BasePlaceholderFragment;

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
