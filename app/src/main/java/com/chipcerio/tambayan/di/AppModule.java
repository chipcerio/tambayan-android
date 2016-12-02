package com.chipcerio.tambayan.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        // return new Gson();
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
