package com.example.studytrack.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studytrack.R;
import com.example.studytrack.auth.AuthRepository;
import com.example.studytrack.firebase.FirestoreUserRepository;
import com.example.studytrack.models.UserModel;
import com.example.studytrack.utils.SessionManager;
import com.example.studytrack.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.text.DateFormat;
import java.util.Date;

public class ProfileFragment extends Fragment {

    private View rootView;
    private TextInputLayout nameInputLayout;
    private TextInputLayout photoInputLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText photoEditText;
    private TextView emailTextView;
    private TextView providerTextView;
    private TextView registerDateTextView;
    private TextView lastLoginTextView;
    private MaterialButton saveButton;
    private CircularProgressIndicator progressIndicator;

    private AuthRepository authRepository;
    private FirestoreUserRepository firestoreUserRepository;
    private SessionManager sessionManager;

    private UserModel currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository(requireContext());
        firestoreUserRepository = new FirestoreUserRepository();
        sessionManager = new SessionManager(requireContext());

        rootView = view.findViewById(R.id.profileRoot);
        nameInputLayout = view.findViewById(R.id.profileNameInputLayout);
        photoInputLayout = view.findViewById(R.id.profilePhotoInputLayout);
        nameEditText = view.findViewById(R.id.profileNameEditText);
        photoEditText = view.findViewById(R.id.profilePhotoEditText);
        emailTextView = view.findViewById(R.id.profileEmailValue);
        providerTextView = view.findViewById(R.id.profileProviderValue);
        registerDateTextView = view.findViewById(R.id.profileRegisterDateValue);
        lastLoginTextView = view.findViewById(R.id.profileLastLoginValue);
        saveButton = view.findViewById(R.id.profileSaveButton);
        progressIndicator = view.findViewById(R.id.profileProgress);

        saveButton.setOnClickListener(v -> saveProfileChanges());

        currentUser = authRepository.getActiveUser();
        renderUser(currentUser);

        if (currentUser != null && !TextUtils.isEmpty(currentUser.getUid())) {
            requestLatestUser(currentUser.getUid());
        }
    }

    private void requestLatestUser(String uid) {
        firestoreUserRepository.getUserById(uid, new FirestoreUserRepository.UserRepositoryCallback() {
            @Override
            public void onSuccess(UserModel userModel) {
                currentUser = mergeCurrentAndRemote(userModel);
                sessionManager.saveUser(currentUser, sessionManager.getToken());
                renderUser(currentUser);
            }

            @Override
            public void onError(Exception exception) {
                // Offline mode or first login may not have remote doc immediately.
            }
        });
    }

    private void saveProfileChanges() {
        clearErrors();

        if (currentUser == null || TextUtils.isEmpty(currentUser.getUid())) {
            showMessage(getString(R.string.msg_session_expired));
            return;
        }

        String displayName = editableToString(nameEditText).trim();
        String photoUrl = editableToString(photoEditText).trim();

        String nameError = ValidationUtils.validateDisplayName(requireContext(), displayName);
        String photoError = ValidationUtils.validatePhotoUrl(requireContext(), photoUrl);

        if (nameError != null) {
            nameInputLayout.setError(nameError);
        }
        if (photoError != null) {
            photoInputLayout.setError(photoError);
        }

        if (nameError != null || photoError != null) {
            return;
        }

        setLoading(true);
        firestoreUserRepository.updateUserProfile(
                currentUser.getUid(),
                displayName,
                photoUrl,
                new FirestoreUserRepository.UserRepositoryCallback() {
                    @Override
                    public void onSuccess(UserModel userModel) {
                        updateFirebaseAuthProfile(displayName, photoUrl, userModel);
                    }

                    @Override
                    public void onError(Exception exception) {
                        setLoading(false);
                        showMessage(getString(R.string.error_profile_update));
                    }
                }
        );
    }

    private void updateFirebaseAuthProfile(String displayName, String photoUrl, UserModel updatedUser) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            persistUser(updatedUser);
            return;
        }

        UserProfileChangeRequest.Builder profileBuilder =
                new UserProfileChangeRequest.Builder().setDisplayName(displayName);

        if (!TextUtils.isEmpty(photoUrl)) {
            profileBuilder.setPhotoUri(Uri.parse(photoUrl));
        }

        firebaseUser.updateProfile(profileBuilder.build())
                .addOnCompleteListener(task -> persistUser(updatedUser));
    }

    private void persistUser(UserModel updatedUser) {
        UserModel merged = mergeCurrentAndRemote(updatedUser);
        merged.setUltimoLogin(System.currentTimeMillis());

        currentUser = merged;
        sessionManager.saveUser(merged, sessionManager.getToken());

        setLoading(false);
        renderUser(merged);
        showMessage(getString(R.string.msg_profile_updated));
    }

    private UserModel mergeCurrentAndRemote(UserModel remote) {
        if (remote == null) {
            return currentUser;
        }

        if (TextUtils.isEmpty(remote.getProvider()) && currentUser != null) {
            remote.setProvider(currentUser.getProvider());
        }
        if (TextUtils.isEmpty(remote.getEmail()) && currentUser != null) {
            remote.setEmail(currentUser.getEmail());
        }
        if (remote.getFechaRegistro() <= 0 && currentUser != null) {
            remote.setFechaRegistro(currentUser.getFechaRegistro());
        }

        return remote;
    }

    private void renderUser(UserModel userModel) {
        if (userModel == null) {
            emailTextView.setText(getString(R.string.empty_text));
            providerTextView.setText(getString(R.string.empty_text));
            registerDateTextView.setText(getString(R.string.empty_text));
            lastLoginTextView.setText(getString(R.string.empty_text));
            return;
        }

        nameEditText.setText(userModel.getNombre());
        photoEditText.setText(userModel.getFoto());

        emailTextView.setText(emptyAsDefault(userModel.getEmail(), getString(R.string.unknown_email)));
        providerTextView.setText(resolveProviderLabel(userModel.getProvider()));
        registerDateTextView.setText(formatDate(userModel.getFechaRegistro()));
        lastLoginTextView.setText(formatDate(userModel.getUltimoLogin()));
    }

    private String resolveProviderLabel(String provider) {
        if ("google.com".equals(provider)) {
            return getString(R.string.provider_google);
        }
        if ("password".equals(provider)) {
            return getString(R.string.provider_password);
        }
        return getString(R.string.provider_unknown);
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return getString(R.string.empty_text);
        }
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(timestamp));
    }

    private String emptyAsDefault(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
        nameEditText.setEnabled(!loading);
        photoEditText.setEnabled(!loading);
    }

    private void clearErrors() {
        nameInputLayout.setError(null);
        photoInputLayout.setError(null);
    }

    private void showMessage(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    private String editableToString(TextInputEditText editText) {
        Editable editable = editText.getText();
        return editable == null ? "" : editable.toString();
    }
}
