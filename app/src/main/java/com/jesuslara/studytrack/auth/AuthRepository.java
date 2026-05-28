package com.jesuslara.studytrack.auth;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.firebase.FirestoreUserRepository;
import com.jesuslara.studytrack.models.UserModel;
import com.jesuslara.studytrack.utils.AppLogger;
import com.jesuslara.studytrack.utils.CacheUtils;
import com.jesuslara.studytrack.utils.NetworkUtils;
import com.jesuslara.studytrack.utils.SessionManager;
import com.jesuslara.studytrack.utils.UiState;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

@SuppressWarnings("deprecation")
public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private final Context appContext;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private final FirestoreUserRepository firestoreUserRepository;
    private final SessionManager sessionManager;

    public interface LogoutCallback {
        void onComplete(boolean success, String message);
    }

    public AuthRepository(Context context) {
        appContext = context.getApplicationContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreUserRepository = new FirestoreUserRepository();
        sessionManager = new SessionManager(appContext);

        GoogleSignInOptions.Builder signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail();

        String webClientId = getWebClientId();
        if (!TextUtils.isEmpty(webClientId)) {
            signInOptions.requestIdToken(webClientId);
        }

        googleSignInClient = GoogleSignIn.getClient(appContext, signInOptions.build());
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        callback.onState(UiState.loading());

        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> handleAuthSuccess(authResult, "password", callback))
                .addOnFailureListener(exception -> {
                    AppLogger.logAuthFailure("password", exception.getMessage() == null
                            ? "unknown"
                            : exception.getMessage());
                    callback.onState(UiState.error(AuthErrorMapper.fromException(appContext, exception)));
                });
    }

    public void registerWithEmail(String email, String password, AuthCallback callback) {
        callback.onState(UiState.loading());

        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> handleAuthSuccess(authResult, "password", callback))
                .addOnFailureListener(exception -> {
                    AppLogger.logAuthFailure("password_register", exception.getMessage() == null
                            ? "unknown"
                            : exception.getMessage());
                    callback.onState(UiState.error(AuthErrorMapper.fromException(appContext, exception)));
                });
    }

    public boolean isGoogleConfigurationValid() {
        String webClientId = getWebClientId();
        if (TextUtils.isEmpty(webClientId)) {
            return false;
        }
        return !webClientId.startsWith("YOUR_")
                && !"default_web_client_id".equalsIgnoreCase(webClientId);
    }

    public boolean isGooglePlayServicesAvailable() {
        int statusCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(appContext);
        return statusCode == ConnectionResult.SUCCESS;
    }

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleGoogleIntentResult(@Nullable Intent data, AuthCallback callback) {
        callback.onState(UiState.loading());

        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
            if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                callback.onState(UiState.error(AuthErrorMapper.googleConfigError(appContext)));
                return;
            }

            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException exception) {
            AppLogger.logAuthFailure("google", String.valueOf(exception.getStatusCode()));
            callback.onState(UiState.error(AuthErrorMapper.fromException(appContext, exception)));
        }
    }

    public boolean hasActiveSession() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return true;
        }

        if (sessionManager.isLoggedIn() && !NetworkUtils.isNetworkAvailable(appContext)) {
            AppLogger.logInfo(TAG, "Using offline session cache");
            return true;
        }

        return sessionManager.isLoggedIn();
    }

    public boolean isUsingOfflineSession() {
        return firebaseAuth.getCurrentUser() == null && sessionManager.isLoggedIn();
    }

    @Nullable
    public UserModel getActiveUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            return buildUserFromFirebase(firebaseUser, sessionManager.getUser());
        }
        return sessionManager.getUser();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void logout(LogoutCallback callback) {
        firebaseAuth.signOut();

        googleSignInClient.signOut()
                .addOnCompleteListener(signOutTask -> {
                    googleSignInClient.revokeAccess();
                    sessionManager.logout();
                    CacheUtils.clearAppCache(appContext);
                    AppLogger.logInfo(TAG, "Logout completed");
                    callback.onComplete(true, appContext.getString(R.string.msg_logout_success));
                })
                .addOnFailureListener(exception -> {
                    sessionManager.logout();
                    CacheUtils.clearAppCache(appContext);
                    AppLogger.logError(TAG, "Google sign-out failure", exception);
                    callback.onComplete(false, AuthErrorMapper.fromException(appContext, exception));
                });
    }

    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> handleAuthSuccess(authResult, "google.com", callback))
                .addOnFailureListener(exception -> {
                    AppLogger.logAuthFailure("google_firebase", exception.getMessage() == null
                            ? "unknown"
                            : exception.getMessage());
                    callback.onState(UiState.error(AuthErrorMapper.fromException(appContext, exception)));
                });
    }

    private void handleAuthSuccess(AuthResult authResult, String provider, AuthCallback callback) {
        FirebaseUser firebaseUser = authResult.getUser();
        if (firebaseUser == null) {
            callback.onState(UiState.error(appContext.getString(R.string.error_auth_generic)));
            return;
        }

        firestoreUserRepository.syncUser(firebaseUser, provider, new FirestoreUserRepository.UserRepositoryCallback() {
            @Override
            public void onSuccess(UserModel userModel) {
                cacheAndPublishUser(firebaseUser, userModel, callback);
            }

            @Override
            public void onError(Exception exception) {
                AppLogger.logError(TAG, "Firestore sync failed, using local fallback", exception);
                UserModel fallbackUser = buildUserFromFirebase(firebaseUser, sessionManager.getUser());
                cacheAndPublishUser(firebaseUser, fallbackUser, callback);
            }
        });
    }

    private void cacheAndPublishUser(FirebaseUser firebaseUser, UserModel userModel, AuthCallback callback) {
        final UserModel resolvedUser = userModel != null
                ? userModel
                : buildUserFromFirebase(firebaseUser, sessionManager.getUser());

        firebaseUser.getIdToken(false)
                .addOnCompleteListener(tokenTask -> {
                    String token = "";
                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null
                            && tokenTask.getResult().getToken() != null) {
                        token = tokenTask.getResult().getToken();
                    }

                    sessionManager.saveUser(resolvedUser, token);
                    AppLogger.logAuthSuccess(resolvedUser.getProvider(), resolvedUser.getUid());
                    callback.onState(UiState.success(resolvedUser));
                });
    }

    private UserModel buildUserFromFirebase(@NonNull FirebaseUser firebaseUser,
                                            @Nullable UserModel cachedUser) {
        long now = System.currentTimeMillis();

        String provider = cachedUser != null && !TextUtils.isEmpty(cachedUser.getProvider())
                ? cachedUser.getProvider()
                : inferProvider(firebaseUser);

        long registerDate = cachedUser != null && cachedUser.getFechaRegistro() > 0
                ? cachedUser.getFechaRegistro()
                : (firebaseUser.getMetadata() != null
                ? firebaseUser.getMetadata().getCreationTimestamp()
                : now);

        String displayName = firstNonEmpty(
                firebaseUser.getDisplayName(),
                cachedUser != null ? cachedUser.getNombre() : null,
                appContext.getString(R.string.unknown_user)
        );

        String photoUrl = firstNonEmpty(
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                cachedUser != null ? cachedUser.getFoto() : null,
                ""
        );

        return new UserModel(
                firebaseUser.getUid(),
                displayName,
                firstNonEmpty(firebaseUser.getEmail(), cachedUser != null ? cachedUser.getEmail() : null, ""),
                photoUrl,
                provider,
                registerDate,
                now
        );
    }

    private String inferProvider(FirebaseUser firebaseUser) {
        if (firebaseUser.getProviderData() == null) {
            return "password";
        }

        for (com.google.firebase.auth.UserInfo providerData : firebaseUser.getProviderData()) {
            if ("google.com".equals(providerData.getProviderId())) {
                return "google.com";
            }
        }
        return "password";
    }

    private String getWebClientId() {
        try {
            return appContext.getString(R.string.default_web_client_id);
        } catch (Exception exception) {
            AppLogger.logError(TAG, "default_web_client_id unavailable", exception);
            return "";
        }
    }

    private String firstNonEmpty(String first, String second, String fallback) {
        if (!TextUtils.isEmpty(first)) {
            return first;
        }
        if (!TextUtils.isEmpty(second)) {
            return second;
        }
        return fallback;
    }
}
