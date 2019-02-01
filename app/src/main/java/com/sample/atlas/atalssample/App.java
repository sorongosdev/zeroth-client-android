package com.sample.atlas.atalssample;

import android.app.Application;

import com.atlas.zerothandroid.Zeroth;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Zeroth.initialize(
                this,
                "e6266c3103c34546804ea35287f39f04",
                "4UG2c5i30b8857605dba4af880e945b81ce6088d");
    }
}
