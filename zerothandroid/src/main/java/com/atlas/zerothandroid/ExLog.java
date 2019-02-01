package com.atlas.zerothandroid;

import android.util.Log;

public class ExLog {

    public static void e(String tag, String text) {
        Log.e(tag, text);
    }

    public static void i(String tag, String text) {
        if(text == null || text.length() == 0) {
            return;
        }
        Log.i(tag, text);
    }
}
