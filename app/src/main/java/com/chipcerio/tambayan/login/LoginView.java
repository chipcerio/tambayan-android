package com.chipcerio.tambayan.login;

public interface LoginView {

    void googleSignInClick();

    void startMainActivity(String userJson);

    void showUserNotFound();

}
