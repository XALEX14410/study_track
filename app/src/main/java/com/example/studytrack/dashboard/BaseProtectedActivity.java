package com.example.studytrack.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studytrack.activities.LoginActivity;
import com.example.studytrack.auth.AuthRepository;

public abstract class BaseProtectedActivity extends AppCompatActivity {

    protected AuthRepository authRepository;
    private boolean redirecting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!authRepository.hasActiveSession()) {
            redirectToLogin();
        }
    }

    protected void redirectToLogin() {
        if (redirecting) {
            return;
        }

        redirecting = true;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
