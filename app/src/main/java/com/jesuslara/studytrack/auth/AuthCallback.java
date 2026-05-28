package com.jesuslara.studytrack.auth;

import com.jesuslara.studytrack.models.UserModel;
import com.jesuslara.studytrack.utils.UiState;

public interface AuthCallback {
    void onState(UiState<UserModel> state);
}
