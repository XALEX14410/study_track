package com.example.studytrack.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.example.studytrack.R;

public final class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private ValidationUtils() {
    }

    public static String validateEmail(Context context, String email) {
        if (TextUtils.isEmpty(email)) {
            return context.getString(R.string.validation_email_required);
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return context.getString(R.string.validation_email_invalid);
        }
        return null;
    }

    public static String validatePassword(Context context, String password) {
        if (TextUtils.isEmpty(password)) {
            return context.getString(R.string.validation_password_required);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return context.getString(R.string.validation_password_length);
        }
        return null;
    }

    public static String validateDisplayName(Context context, String displayName) {
        if (TextUtils.isEmpty(displayName) || displayName.trim().length() < 2) {
            return context.getString(R.string.validation_name_required);
        }
        return null;
    }

    public static String validatePhotoUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (!Patterns.WEB_URL.matcher(url.trim()).matches()) {
            return context.getString(R.string.validation_photo_url_invalid);
        }
        return null;
    }
}
