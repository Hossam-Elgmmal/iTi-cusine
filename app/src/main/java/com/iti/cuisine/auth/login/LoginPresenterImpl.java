package com.iti.cuisine.auth.login;

import android.util.Patterns;

import com.iti.cuisine.Constants;
import com.iti.cuisine.R;
import com.iti.cuisine.auth.AuthHandler;
import com.iti.cuisine.data.auth.AuthRepo;
import com.iti.cuisine.data.auth.AuthRepoImpl;

import org.jspecify.annotations.Nullable;

public class LoginPresenterImpl implements LoginPresenter {

    @Nullable
    private LoginView view;
    private AuthHandler authHandler;

    private final AuthRepo authRepo;

    public static LoginPresenterImpl createNewInstance() {
        AuthRepo authRepo = new AuthRepoImpl();
        return new LoginPresenterImpl(new AuthRepoImpl(), new AuthHandler(authRepo));
    }

    public LoginPresenterImpl(AuthRepo authRepo, AuthHandler authHandler) {
        this.authRepo = authRepo;
        this.authHandler = authHandler;
    }

    @Override
    public void setView(LoginView view) {
        this.view = view;
        authHandler.setView(view);
    }

    @Override
    public void removeView() {
        authHandler.removeView();
        view = null;
    }

    @Override
    public void onLoginClick(String email, String password) {
        boolean isValid = validate(email, password);

        if (!isValid) return;

        authHandler.executeAuthOperation(
                authRepo.loginWithEmailAndPassword(email, password)
        );
    }

    private boolean validate(String email, String password) {
        boolean emailError = false;
        boolean passwordError = false;

        if (view == null) return false;

        if (password.length() >= 8 && password.matches(Constants.PASSWORD_PATTERN)) {
            view.removePasswordError();
        } else {
            passwordError = true;
            view.showPasswordError();
        }

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.removeEmailError();
        } else {
            emailError = true;
            view.showEmailError();
        }
        boolean isError = emailError || passwordError;

        if (isError) {
            view.showMessage(R.string.wrong_email_or_password);
        }

        return !isError;
    }

    @Override
    public void onGoToSignUpClick() {
        if (view != null) {
            view.navigateToSignUpScreen();
        }
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
