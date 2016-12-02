package com.chipcerio.tambayan;

import android.app.Application;

import com.chipcerio.tambayan.di.AndroidModule;
import com.chipcerio.tambayan.di.AppComponent;
import com.chipcerio.tambayan.di.AppModule;
import com.chipcerio.tambayan.di.DaggerAppComponent;

public class TambayanApplication extends Application {

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .androidModule(new AndroidModule(this))
                .build();
    }

    public AppComponent getComponent() {
        return mAppComponent;
    }
}
