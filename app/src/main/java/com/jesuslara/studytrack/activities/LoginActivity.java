package com.jesuslara.studytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.auth.AuthCallback;
import com.jesuslara.studytrack.auth.AuthRepository;
import com.jesuslara.studytrack.dashboard.DashboardActivity;
import com.jesuslara.studytrack.models.UserModel;
import com.jesuslara.studytrack.utils.UiState;
import com.jesuslara.studytrack.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private View rootView;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialButton googleButton;
    private CircularProgressIndicator progressIndicator;

    private AuthRepository authRepository;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getData() == null) {
                            setLoading(false);
                            showMessage(getString(R.string.error_google_cancelled));
                            return;
                        }
                        authRepository.handleGoogleIntentResult(result.getData(), this::renderAuthState);
                    }
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        rootView = findViewById(R.id.loginRoot);
        emailLayout = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleButton = findViewById(R.id.googleButton);
        progressIndicator = findViewById(R.id.authProgress);

        loginButton.setOnClickListener(view -> attemptEmailAuthentication(false));
        registerButton.setOnClickListener(view -> attemptEmailAuthentication(true));
        googleButton.setOnClickListener(view -> attemptGoogleAuthentication());

        String incomingMessage = getIntent().getStringExtra("extra_message");
        if (!TextUtils.isEmpty(incomingMessage)) {
            showMessage(incomingMessage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authRepository.hasActiveSession()) {
            openDashboard();
        }
    }

    private void attemptEmailAuthentication(boolean register) {
        animateButtonPress(register ? registerButton : loginButton);
        clearErrors();

        String email = editableToString(emailEditText).trim();
        String password = editableToString(passwordEditText);

        String emailError = ValidationUtils.validateEmail(this, email);
        String passwordError = ValidationUtils.validatePassword(this, password);

        if (emailError != null) {
            emailLayout.setError(emailError);
        }
        if (passwordError != null) {
            passwordLayout.setError(passwordError);
        }

        if (emailError != null || passwordError != null) {
            return;
        }

        AuthCallback callback = this::renderAuthState;
        if (register) {
            authRepository.registerWithEmail(email, password, callback);
        } else {
            authRepository.signInWithEmail(email, password, callback);
        }
    }

    private void attemptGoogleAuthentication() {
        animateButtonPress(googleButton);
        if (!authRepository.isGoogleConfigurationValid()) {
            showMessage(getString(R.string.error_google_config_invalid));
            return;
        }
        if (!authRepository.isGooglePlayServicesAvailable()) {
            showMessage(getString(R.string.error_google_play_services_unavailable));
            return;
        }

        setLoading(true);
        googleSignInLauncher.launch(authRepository.getGoogleSignInIntent());
    }

    private void renderAuthState(UiState<UserModel> state) {
        if (state.getStatus() == UiState.Status.LOADING) {
            setLoading(true);
            return;
        }

        if (state.getStatus() == UiState.Status.ERROR) {
            setLoading(false);
            showMessage(state.getErrorMessage());
            return;
        }

        if (state.getStatus() == UiState.Status.SUCCESS) {
            setLoading(false);
            UserModel userModel = state.getData();
            String name = userModel != null && !TextUtils.isEmpty(userModel.getNombre())
                    ? userModel.getNombre()
                    : getString(R.string.unknown_user);
            showMessage(getString(R.string.msg_welcome_user, name));
            openDashboard();
            return;
        }

        setLoading(false);
    }

    private void openDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("extra_offline_session", authRepository.isUsingOfflineSession());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        googleButton.setEnabled(!loading);
        emailEditText.setEnabled(!loading);
        passwordEditText.setEnabled(!loading);
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void showMessage(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void animateButtonPress(View target) {
        target.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(90L)
                .withEndAction(() -> target.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90L)
                        .start())
                .start();
    }

    private String editableToString(TextInputEditText editText) {
        Editable editable = editText.getText();
        return editable == null ? "" : editable.toString();
    }
}
