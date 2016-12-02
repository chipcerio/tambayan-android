package com.chipcerio.tambayan.login;

public interface LoginPresenter {

    void googleSignIn();

    void setFirebaseUser(String userJson);

}
