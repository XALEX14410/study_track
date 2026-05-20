package com.example.studytrack.firebase;

import android.content.Context;

import com.example.studytrack.utils.AppLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public final class FirebaseInitializer {

    private static final String TAG = "FirebaseInitializer";
    private static boolean initialized;

    private FirebaseInitializer() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }

        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception exception) {
            AppLogger.logError(TAG, "FirebaseApp initialization issue", exception);
        }

        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (Exception exception) {
            AppLogger.logError(TAG, "Firestore offline persistence configuration failed", exception);
        }

        initialized = true;
    }
}
