package com.chipcerio.tambayan.di;

import com.chipcerio.tambayan.TambayanApplication;
import com.chipcerio.tambayan.login.LoginActivity;
import com.chipcerio.tambayan.view.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class, AndroidModule.class })
public interface AppComponent {

    void inject(TambayanApplication target);

    void inject(LoginActivity target);

    void inject(MainActivity target);
}
