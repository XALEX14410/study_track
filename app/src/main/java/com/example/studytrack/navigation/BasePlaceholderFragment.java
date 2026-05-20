package com.example.studytrack.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.example.studytrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public abstract class BasePlaceholderFragment extends Fragment {

    @DrawableRes
    protected abstract int getIconResId();

    @StringRes
    protected abstract int getTitleResId();

    @StringRes
    protected abstract int getDescriptionResId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView iconView = view.findViewById(R.id.placeholderIcon);
        TextView titleView = view.findViewById(R.id.placeholderTitle);
        TextView descriptionView = view.findViewById(R.id.placeholderDescription);
        MaterialButton actionButton = view.findViewById(R.id.placeholderActionButton);

        iconView.setImageResource(getIconResId());
        titleView.setText(getTitleResId());
        descriptionView.setText(getDescriptionResId());

        actionButton.setOnClickListener(v ->
                Snackbar.make(view, R.string.placeholder_action_soon, Snackbar.LENGTH_SHORT).show()
        );
    }
}
