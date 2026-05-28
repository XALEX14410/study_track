package com.jesuslara.studytrack.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppLogger {

    private static final String TAG = "StudyTrackLogger";
    private static final String LOG_FILE_NAME = "studytrack_events.log";
    private static final ExecutorService LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    private static Context appContext;

    private AppLogger() {
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void logInfo(String source, String message) {
        write("INFO", source, message, null);
    }

    public static void logError(String source, String message, @Nullable Throwable throwable) {
        write("ERROR", source, message, throwable);
    }

    public static void logCritical(String source, String message, @Nullable Throwable throwable) {
        write("CRITICAL", source, message, throwable);
    }

    public static void logAuthSuccess(String provider, String uid) {
        write("AUTH_SUCCESS", "Auth", "provider=" + provider + ", uid=" + uid, null);
    }

    public static void logAuthFailure(String provider, String reason) {
        write("AUTH_FAILURE", "Auth", "provider=" + provider + ", reason=" + reason, null);
    }

    private static void write(String level, String source, String message, @Nullable Throwable throwable) {
        String finalMessage = level + " | " + source + " | " + message;
        if (throwable == null) {
            Log.i(TAG, finalMessage);
        } else {
            Log.e(TAG, finalMessage, throwable);
        }

        if (appContext == null) {
            return;
        }

        LOG_EXECUTOR.execute(() -> {
            File logFile = new File(appContext.getFilesDir(), LOG_FILE_NAME);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(new Date());

            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.append(timestamp)
                        .append(" | ")
                        .append(level)
                        .append(" | ")
                        .append(source)
                        .append(" | ")
                        .append(message)
                        .append('\n');
            } catch (IOException exception) {
                Log.e(TAG, "Failed to write log file", exception);
            }
        });
    }
}
