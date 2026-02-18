package com.iti.cuisine.auth.sign_up;

import android.util.Patterns;

import com.iti.cuisine.Constants;
import com.iti.cuisine.R;
import com.iti.cuisine.auth.AuthHandler;
import com.iti.cuisine.data.auth.AuthRepo;
import com.iti.cuisine.data.auth.AuthRepoImpl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SignUpPresenterImpl implements SignUpPresenter {

    @Nullable
    private SignUpView view;
    private AuthHandler authHandler;

    private final AuthRepo authRepo;

    public static SignUpPresenterImpl createNewInstance() {
        AuthRepo authRepo = new AuthRepoImpl();
        return new SignUpPresenterImpl(new AuthRepoImpl(), new AuthHandler(authRepo));
    }

    public SignUpPresenterImpl(AuthRepo authRepo, AuthHandler authHandler) {
        this.authRepo = authRepo;
        this.authHandler = authHandler;
    }

    @Override
    public void setView(@NonNull SignUpView view) {
        this.view = view;
        authHandler.setView(view);
    }

    @Override
    public void removeView() {
        authHandler.removeView();
        view = null;
    }

    @Override
    public void onGoToLoginClick() {
        if (view != null) {
            view.navigateToLoginScreen();
        }
    }

    @Override
    public void onSignUpClick(String username, String email, String password, String confirmPassword) {

        boolean isValid = validate(username, email, password, confirmPassword);

        if (!isValid) return;

        authHandler.executeAuthOperation(authRepo.signUpWithEmailAndPassword(username, email, password));
    }

    private boolean validate(String username, String email, String password, String confirmPassword) {
        boolean usernameError = false;
        boolean emailError = false;
        boolean passwordError = false;
        boolean confirmPasswordError = false;

        if (view == null) return false;
        int messageId = -1;

        if (password.equals(confirmPassword)) {
            view.removeConfirmPasswordError();
        } else {
            confirmPasswordError = true;
            view.showConfirmPasswordError();
            messageId = R.string.passwords_do_not_match;
        }

        if (password.length() >= 8 && password.matches(Constants.PASSWORD_PATTERN)) {
            view.removePasswordError();
        } else {
            passwordError = true;
            view.showPasswordError();
            messageId = R.string.invalid_password;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.removeEmailError();
        } else {
            emailError = true;
            view.showEmailError();
            messageId = R.string.invalid_email;
        }

        if (username.length() >= 3) {
            view.removeUsernameError();
        } else {
            usernameError = true;
            view.showUsernameError();
            messageId = R.string.username_is_too_short;
        }

        boolean isError = usernameError || emailError || passwordError || confirmPasswordError;

        if (isError) {
            view.showMessage(messageId);
        }

        return !isError;
    }

    @Override
    public void onGoogleLoginClick() {
        if (view == null) return;
        authHandler.handleGoogleLogin();
    }

    @Override
    public void onGuestLoginClick() {
        authHandler.handleGuestLogin();
    }

    @Override
    public void destroy() {
        if (authHandler != null) {
            authHandler.destroy();
            authHandler = null;
        }
    }
}
