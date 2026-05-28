package com.jesuslara.studytrack.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;

public final class CacheUtils {

    private CacheUtils() {
    }

    public static void clearAppCache(Context context) {
        deleteRecursively(context.getCacheDir());
        deleteRecursively(context.getExternalCacheDir());
    }

    private static void deleteRecursively(@Nullable File target) {
        if (target == null || !target.exists()) {
            return;
        }

        if (target.isDirectory()) {
            File[] children = target.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }

        // Best-effort cache cleanup.
        //noinspection ResultOfMethodCallIgnored
        target.delete();
    }
}
