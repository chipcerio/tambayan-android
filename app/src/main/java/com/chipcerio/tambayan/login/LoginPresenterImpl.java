package com.chipcerio.tambayan.login;

public class LoginPresenterImpl implements LoginPresenter {

    private final LoginView mView;

    public LoginPresenterImpl(LoginView view) {
        mView = view;
    }

    @Override
    public void googleSignIn() {
        mView.googleSignInClick();
    }

    @Override
    public void setFirebaseUser(String userJson) {
        if (userJson == null || userJson.isEmpty()) {
            mView.showUserNotFound();
        } else {
            mView.startMainActivity(userJson);
        }
    }
}
