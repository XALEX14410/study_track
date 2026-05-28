package com.jesuslara.studytrack.auth;

import android.content.Context;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.utils.NetworkUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Locale;

public final class AuthErrorMapper {

    private AuthErrorMapper() {
    }

    public static String fromException(Context context, Exception exception) {
        if (exception instanceof ApiException) {
            return fromGoogleApiException(context, (ApiException) exception);
        }

        if (exception instanceof FirebaseNetworkException) {
            return context.getString(R.string.error_no_internet);
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            FirebaseAuthInvalidUserException invalidUserException =
                    (FirebaseAuthInvalidUserException) exception;
            String errorCode = invalidUserException.getErrorCode();

            if ("ERROR_USER_DISABLED".equals(errorCode)) {
                return context.getString(R.string.error_account_disabled);
            }
            if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                return context.getString(R.string.error_user_not_found);
            }
            if ("ERROR_USER_TOKEN_EXPIRED".equals(errorCode)
                    || "ERROR_INVALID_USER_TOKEN".equals(errorCode)) {
                return context.getString(R.string.error_token_invalid);
            }
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException invalidCredentialsException =
                    (FirebaseAuthInvalidCredentialsException) exception;
            String errorCode = invalidCredentialsException.getErrorCode();

            if ("ERROR_INVALID_CREDENTIAL".equals(errorCode)
                    || "ERROR_WRONG_PASSWORD".equals(errorCode)) {
                return context.getString(R.string.error_invalid_credentials);
            }
            if ("ERROR_USER_TOKEN_EXPIRED".equals(errorCode)
                    || "ERROR_INVALID_USER_TOKEN".equals(errorCode)) {
                return context.getString(R.string.error_token_invalid);
            }
        }

        if (exception instanceof FirebaseTooManyRequestsException) {
            return context.getString(R.string.error_too_many_requests);
        }

        String message = exception.getMessage();
        if (message != null) {
            String lowered = message.toLowerCase(Locale.US);
            if (lowered.contains("timeout")) {
                return context.getString(R.string.error_firebase_timeout);
            }
            if (lowered.contains("google-services") || lowered.contains("configuration_not_found")) {
                return context.getString(R.string.error_google_services_invalid);
            }
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            return context.getString(R.string.error_no_internet);
        }

        return context.getString(R.string.error_auth_generic);
    }

    public static String googleConfigError(Context context) {
        return context.getString(R.string.error_google_config_invalid);
    }

    private static String fromGoogleApiException(Context context, ApiException exception) {
        int statusCode = exception.getStatusCode();

        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
            return context.getString(R.string.error_google_cancelled);
        }
        if (statusCode == CommonStatusCodes.NETWORK_ERROR) {
            return context.getString(R.string.error_no_internet);
        }
        if (statusCode == CommonStatusCodes.TIMEOUT) {
            return context.getString(R.string.error_firebase_timeout);
        }
        if (statusCode == CommonStatusCodes.API_NOT_CONNECTED
                || statusCode == ConnectionResult.SERVICE_MISSING
                || statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
                || statusCode == ConnectionResult.SERVICE_DISABLED) {
            return context.getString(R.string.error_google_play_services_unavailable);
        }
        if (statusCode == CommonStatusCodes.DEVELOPER_ERROR || statusCode == 10 || statusCode == 12500) {
            return context.getString(R.string.error_sha1_mismatch);
        }

        return context.getString(R.string.error_google_failed);
    }
}
