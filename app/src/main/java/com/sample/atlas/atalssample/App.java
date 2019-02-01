package com.sample.atlas.atalssample;

import android.app.Application;

import com.atlas.zerothandroid.Zeroth;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Zeroth.initialize(
                this,
                /* your App Id*/"",
                /* your App Secret Id*/"");
    }
}
