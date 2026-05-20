package com.example.studytrack.auth;

import com.example.studytrack.models.UserModel;
import com.example.studytrack.utils.UiState;

public interface AuthCallback {
    void onState(UiState<UserModel> state);
}
