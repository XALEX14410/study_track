package com.example.studytrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.studytrack.models.UserModel;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "studytrack_secure_session";

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_REGISTER_DATE = "register_date";
    private static final String KEY_LAST_LOGIN = "last_login";
    private static final String KEY_TOKEN = "token";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        this.sharedPreferences = createPreferences(context.getApplicationContext());
    }

    private SharedPreferences createPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception exception) {
            AppLogger.logError(TAG, "Encrypted preferences unavailable, using fallback", exception);
            return context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    public void saveUser(UserModel userModel, String token) {
        if (userModel == null || TextUtils.isEmpty(userModel.getUid())) {
            return;
        }

        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_UID, safe(userModel.getUid()))
                .putString(KEY_NAME, safe(userModel.getNombre()))
                .putString(KEY_EMAIL, safe(userModel.getEmail()))
                .putString(KEY_PHOTO, safe(userModel.getFoto()))
                .putString(KEY_PROVIDER, safe(userModel.getProvider()))
                .putLong(KEY_REGISTER_DATE, userModel.getFechaRegistro())
                .putLong(KEY_LAST_LOGIN, userModel.getUltimoLogin())
                .putString(KEY_TOKEN, safe(token))
                .apply();
    }

    public UserModel getUser() {
        if (!isLoggedIn()) {
            return null;
        }

        UserModel userModel = new UserModel();
        userModel.setUid(sharedPreferences.getString(KEY_UID, ""));
        userModel.setNombre(sharedPreferences.getString(KEY_NAME, ""));
        userModel.setEmail(sharedPreferences.getString(KEY_EMAIL, ""));
        userModel.setFoto(sharedPreferences.getString(KEY_PHOTO, ""));
        userModel.setProvider(sharedPreferences.getString(KEY_PROVIDER, ""));
        userModel.setFechaRegistro(sharedPreferences.getLong(KEY_REGISTER_DATE, 0L));
        userModel.setUltimoLogin(sharedPreferences.getLong(KEY_LAST_LOGIN, 0L));
        return userModel;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
                && !TextUtils.isEmpty(sharedPreferences.getString(KEY_UID, ""));
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, "");
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, safe(token)).apply();
    }

    public void updateLastLogin(long timestamp) {
        sharedPreferences.edit().putLong(KEY_LAST_LOGIN, timestamp).apply();
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
