package com.example.studytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.example.studytrack.R;
import com.example.studytrack.auth.AuthRepository;
import com.example.studytrack.dashboard.DashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1400L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = this::navigateNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MotionLayout motionLayout = findViewById(R.id.splashMotionRoot);
        if (motionLayout != null) {
            motionLayout.transitionToEnd();
        }

        handler.postDelayed(navigateRunnable, SPLASH_DELAY_MS);
    }

    private void navigateNext() {
        AuthRepository authRepository = new AuthRepository(this);
        if (authRepository.hasActiveSession()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("extra_offline_session", authRepository.isUsingOfflineSession());
            startActivity(intent);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }
}
