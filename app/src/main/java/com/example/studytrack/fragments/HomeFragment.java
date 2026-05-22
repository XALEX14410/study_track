package com.example.studytrack.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studytrack.R;
import com.example.studytrack.adapters.QuickActionsAdapter;
import com.example.studytrack.adapters.SummaryStatsAdapter;
import com.example.studytrack.adapters.UpcomingEventsAdapter;
import com.example.studytrack.models.QuickActionItem;
import com.example.studytrack.models.SummaryStatItem;
import com.example.studytrack.models.UpcomingEventItem;
import com.example.studytrack.models.UserModel;
import com.example.studytrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface HomeActionListener {
        void onOpenNotificationsRequested();

        void onOpenSettingsRequested();
    }

    private HomeActionListener actionListener;

    private View rootView;
    private ShapeableImageView profileImageView;
    private TextView nameTextView;
    private TextView welcomeTextView;
    private TextView emailTextView;
    private MaterialButton notificationsButton;
    private MaterialButton settingsButton;
    private View notificationDot;
    private RecyclerView summaryRecycler;
    private RecyclerView quickActionsRecycler;
    private RecyclerView eventsRecycler;

    private SessionManager sessionManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeActionListener) {
            actionListener = (HomeActionListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;
        sessionManager = new SessionManager(requireContext());

        profileImageView = view.findViewById(R.id.homeProfileImage);
        nameTextView = view.findViewById(R.id.homeNameText);
        welcomeTextView = view.findViewById(R.id.homeWelcomeText);
        emailTextView = view.findViewById(R.id.homeEmailText);
        notificationsButton = view.findViewById(R.id.homeNotificationsButton);
        settingsButton = view.findViewById(R.id.homeSettingsButton);
        notificationDot = view.findViewById(R.id.homeNotificationDot);
        summaryRecycler = view.findViewById(R.id.homeSummaryRecycler);
        quickActionsRecycler = view.findViewById(R.id.homeQuickActionsRecycler);
        eventsRecycler = view.findViewById(R.id.homeEventsRecycler);

        notificationsButton.setOnClickListener(v -> {
            animateButton(v);
            if (actionListener != null) {
                actionListener.onOpenNotificationsRequested();
            }
        });

        settingsButton.setOnClickListener(v -> {
            animateButton(v);
            if (actionListener != null) {
                actionListener.onOpenSettingsRequested();
            }
        });

        setupSummarySection();
        setupQuickActionsSection();
        setupUpcomingEventsSection();
        bindCurrentUser();
        playEntranceAnimation(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    private void setupSummarySection() {
        List<SummaryStatItem> items = new ArrayList<>();
        items.add(new SummaryStatItem(
                R.drawable.ic_subjects,
                getString(R.string.stat_value_subjects),
                getString(R.string.summary_active_subjects)
        ));
        items.add(new SummaryStatItem(
                R.drawable.ic_task,
                getString(R.string.stat_value_tasks),
                getString(R.string.summary_pending_tasks)
        ));
        items.add(new SummaryStatItem(
                R.drawable.ic_calendar,
                getString(R.string.stat_value_exams),
                getString(R.string.summary_next_exams)
        ));
        items.add(new SummaryStatItem(
                R.drawable.ic_schedule,
                getString(R.string.stat_value_hours),
                getString(R.string.summary_studied_hours)
        ));

        summaryRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );
        summaryRecycler.setAdapter(new SummaryStatsAdapter(items));
    }

    private void setupQuickActionsSection() {
        List<QuickActionItem> items = new ArrayList<>();
        items.add(new QuickActionItem(R.drawable.ic_subjects, getString(R.string.quick_action_subjects)));
        items.add(new QuickActionItem(R.drawable.ic_task, getString(R.string.quick_action_tasks)));
        items.add(new QuickActionItem(R.drawable.ic_calendar, getString(R.string.quick_action_exams)));
        items.add(new QuickActionItem(R.drawable.ic_schedule, getString(R.string.quick_action_schedule)));
        //items.add(new QuickActionItem(R.drawable.ic_calendar, getString(R.string.quick_action_calendar)));
        //items.add(new QuickActionItem(R.drawable.ic_note, getString(R.string.quick_action_notes)));
        //items.add(new QuickActionItem(R.drawable.ic_reminder, getString(R.string.quick_action_reminders)));
        //items.add(new QuickActionItem(R.drawable.ic_ai, getString(R.string.quick_action_ai)));

        quickActionsRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        quickActionsRecycler.setAdapter(new QuickActionsAdapter(items, item -> {
            int iconResId = item.getIconResId();

            if (iconResId == R.drawable.ic_subjects) {
                Navigation.findNavController(rootView)
                        .navigate(R.id.action_nav_home_to_nav_materias);
            } else if (iconResId == R.drawable.ic_task) {
                Navigation.findNavController(rootView)
                        .navigate(R.id.action_nav_home_to_nav_tareas);
            } else if (iconResId == R.drawable.ic_calendar) {
                Navigation.findNavController(rootView)
                        .navigate(R.id.action_nav_home_to_nav_examenes);
            } else if (iconResId == R.drawable.ic_schedule) {
                Navigation.findNavController(rootView)
                        .navigate(R.id.action_nav_home_to_nav_calendar);
            }
        }));
    }

    private void setupUpcomingEventsSection() {
        List<UpcomingEventItem> items = new ArrayList<>();
        items.add(new UpcomingEventItem(
                getString(R.string.event_demo_1_subject),
                getString(R.string.event_demo_1_date),
                getString(R.string.event_priority_high),
                ContextCompat.getColor(requireContext(), R.color.st_priority_high)
        ));
        items.add(new UpcomingEventItem(
                getString(R.string.event_demo_2_subject),
                getString(R.string.event_demo_2_date),
                getString(R.string.event_priority_medium),
                ContextCompat.getColor(requireContext(), R.color.st_priority_medium)
        ));
        items.add(new UpcomingEventItem(
                getString(R.string.event_demo_3_subject),
                getString(R.string.event_demo_3_date),
                getString(R.string.event_priority_low),
                ContextCompat.getColor(requireContext(), R.color.st_priority_low)
        ));

        eventsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventsRecycler.setAdapter(new UpcomingEventsAdapter(items));
    }

    private void bindCurrentUser() {
        UserModel userModel = sessionManager.getUser();
        if (userModel == null) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                userModel = new UserModel(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "",
                        "",
                        0L,
                        System.currentTimeMillis()
                );
            }
        }

        String displayName = userModel != null && !TextUtils.isEmpty(userModel.getNombre())
                ? userModel.getNombre()
                : getString(R.string.unknown_user);

        String email = userModel != null && !TextUtils.isEmpty(userModel.getEmail())
                ? userModel.getEmail()
                : getString(R.string.unknown_email);

        nameTextView.setText(displayName);
        welcomeTextView.setText(getString(R.string.dashboard_greeting, displayName));
        emailTextView.setText(email);

        String photoUrl = userModel != null ? userModel.getFoto() : "";
        if (!TextUtils.isEmpty(photoUrl)) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void animateButton(View button) {
        button.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(90L)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120L)
                        .start())
                .start();
    }

    private void playEntranceAnimation(View view) {
        view.setAlpha(0f);
        view.setTranslationY(20f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(340L)
                .start();
    }
}
