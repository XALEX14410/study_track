package com.jesuslara.studytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.auth.AuthRepository;
import com.jesuslara.studytrack.dashboard.DashboardActivity;
import com.jesuslara.studytrack.utils.AppLogger;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
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
        boolean activeSession = authRepository.hasActiveSession();
        boolean offlineSession = authRepository.isUsingOfflineSession();

        AppLogger.logInfo(TAG, "navigateNext: activeSession=" + activeSession
                + ", offlineSession=" + offlineSession);

        if (activeSession) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("extra_offline_session", offlineSession);
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
