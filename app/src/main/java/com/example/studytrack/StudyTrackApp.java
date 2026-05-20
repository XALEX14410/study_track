package com.example.studytrack;

import android.app.Application;

import com.example.studytrack.firebase.FirebaseInitializer;
import com.example.studytrack.utils.AppLogger;
import com.google.android.material.color.DynamicColors;

public class StudyTrackApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.init(this);
        FirebaseInitializer.initialize(this);
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
