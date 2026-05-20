package com.example.studytrack.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.studytrack.R;
import com.example.studytrack.activities.LoginActivity;
import com.example.studytrack.fragments.CalendarFragment;
import com.example.studytrack.fragments.HelpFragment;
import com.example.studytrack.fragments.HomeFragment;
import com.example.studytrack.fragments.NotificationsFragment;
import com.example.studytrack.fragments.ProfileFragment;
import com.example.studytrack.fragments.SettingsFragment;
import com.example.studytrack.fragments.SubjectsFragment;
import com.example.studytrack.models.UserModel;
import com.example.studytrack.ui.EdgeToEdgeUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class DashboardActivity extends BaseProtectedActivity
        implements HomeFragment.HomeActionListener, SettingsFragment.SettingsActionListener {

    private DrawerLayout rootView;
    private View fragmentContainer;
    private View loadingOverlay;
    private MaterialCardView bottomNavigationCard;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;

    private ShapeableImageView drawerProfileImage;
    private TextView drawerUserName;
    private TextView drawerUserEmail;
    private MaterialSwitch drawerDarkModeSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.enable(this);
        setContentView(R.layout.activity_dashboard);

        rootView = findViewById(R.id.dashboardRoot);
        fragmentContainer = findViewById(R.id.dashboardFragmentContainer);
        loadingOverlay = findViewById(R.id.logoutLoadingOverlay);
        bottomNavigationCard = findViewById(R.id.bottomNavigationCard);
        toolbar = findViewById(R.id.dashboardToolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        navigationView = findViewById(R.id.dashboardNavigationView);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> rootView.openDrawer(GravityCompat.START));

        setupEdgeToEdge();
        setupDrawerHeader();
        setupDrawerMenu();
        setupBackPressedHandler();

        bottomNavigationView.setOnItemSelectedListener(item -> navigateTo(item.getItemId()));

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            navigationView.setCheckedItem(R.id.drawer_home);
        }

        if (getIntent().getBooleanExtra("extra_offline_session", false)) {
            Snackbar.make(rootView, R.string.msg_offline_session, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestLogout() {
        performLogout();
    }

    @Override
    public void onOpenNotificationsRequested() {
        openDrawerDestination(R.id.drawer_notifications);
    }

    @Override
    public void onOpenSettingsRequested() {
        openDrawerDestination(R.id.drawer_settings);
    }

    private boolean navigateTo(int itemId) {
        Fragment fragment;
        String title;

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            title = getString(R.string.dashboard_menu_home);
            navigationView.setCheckedItem(R.id.drawer_home);
        } else if (itemId == R.id.nav_calendar) {
            fragment = new CalendarFragment();
            title = getString(R.string.dashboard_menu_calendar);
            navigationView.setCheckedItem(R.id.drawer_home);
        } else if (itemId == R.id.nav_subjects) {
            fragment = new SubjectsFragment();
            title = getString(R.string.dashboard_menu_subjects);
            navigationView.setCheckedItem(R.id.drawer_home);
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = getString(R.string.dashboard_menu_profile);
            navigationView.setCheckedItem(R.id.drawer_home);
        } else {
            return false;
        }

        showFragment(fragment);
        toolbar.setTitle(title);
        return true;
    }

    private void openDrawerDestination(int drawerItemId) {
        if (drawerItemId == R.id.drawer_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (drawerItemId == R.id.drawer_notifications) {
            showFragment(new NotificationsFragment());
            toolbar.setTitle(R.string.drawer_notifications);
            navigationView.setCheckedItem(R.id.drawer_notifications);
        } else if (drawerItemId == R.id.drawer_settings) {
            showFragment(new SettingsFragment());
            toolbar.setTitle(R.string.drawer_settings);
            navigationView.setCheckedItem(R.id.drawer_settings);
        } else if (drawerItemId == R.id.drawer_help) {
            showFragment(new HelpFragment());
            toolbar.setTitle(R.string.drawer_help);
            navigationView.setCheckedItem(R.id.drawer_help);
        } else if (drawerItemId == R.id.drawer_dark_mode) {
            toggleDarkMode();
        } else if (drawerItemId == R.id.drawer_logout) {
            performLogout();
        }

        rootView.closeDrawer(GravityCompat.START);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.dashboardFragmentContainer, fragment)
                .commit();
    }

    private void performLogout() {
        setLoading(true);

        authRepository.logout((success, message) -> {
            setLoading(false);
            openLogin(message);
        });
    }

    private void setLoading(boolean loading) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        bottomNavigationView.setEnabled(!loading);
        navigationView.setEnabled(!loading);
    }

    private void openLogin(String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        if (!TextUtils.isEmpty(message)) {
            intent.putExtra("extra_message", message);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupEdgeToEdge() {
        EdgeToEdgeUtils.applyInsetsAsPadding(toolbar, true, false, true, true);
        EdgeToEdgeUtils.applyInsetsAsPadding(fragmentContainer, false, false, true, true);
        EdgeToEdgeUtils.applyInsetsAsPadding(bottomNavigationCard, false, true, true, true);
        EdgeToEdgeUtils.applyInsetsAsPadding(navigationView, true, true, false, false);
        EdgeToEdgeUtils.applyInsetsAsPadding(loadingOverlay, true, true, false, false);
    }

    private void setupDrawerHeader() {
        View headerView = navigationView.getHeaderView(0);
        drawerProfileImage = headerView.findViewById(R.id.drawerProfileImage);
        drawerUserName = headerView.findViewById(R.id.drawerUserName);
        drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);

        UserModel userModel = authRepository.getActiveUser();
        if (userModel == null) {
            return;
        }

        drawerUserName.setText(TextUtils.isEmpty(userModel.getNombre())
                ? getString(R.string.unknown_user)
                : userModel.getNombre());

        drawerUserEmail.setText(TextUtils.isEmpty(userModel.getEmail())
                ? getString(R.string.unknown_email)
                : userModel.getEmail());

        if (TextUtils.isEmpty(userModel.getFoto())) {
            drawerProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            com.bumptech.glide.Glide.with(this)
                    .load(userModel.getFoto())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(drawerProfileImage);
        }
    }

    private void setupDrawerMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            openDrawerDestination(item.getItemId());
            return true;
        });

        View actionView = navigationView.getMenu().findItem(R.id.drawer_dark_mode).getActionView();
        if (actionView != null) {
            drawerDarkModeSwitch = actionView.findViewById(R.id.drawerDarkModeSwitch);
            if (drawerDarkModeSwitch != null) {
                drawerDarkModeSwitch.setChecked(isDarkModeEnabled());
                drawerDarkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    AppCompatDelegate.setDefaultNightMode(isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
                });
            }
        }
    }

    private void toggleDarkMode() {
        if (drawerDarkModeSwitch != null) {
            drawerDarkModeSwitch.toggle();
        } else {
            AppCompatDelegate.setDefaultNightMode(isDarkModeEnabled()
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private boolean isDarkModeEnabled() {
        return (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (rootView.isDrawerOpen(GravityCompat.START)) {
                    rootView.closeDrawer(GravityCompat.START);
                    return;
                }

                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
    }
}
