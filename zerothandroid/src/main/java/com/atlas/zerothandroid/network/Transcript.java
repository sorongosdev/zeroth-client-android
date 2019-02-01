package com.atlas.zerothandroid.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transcript {

    @Expose public String transcript;

    @SerializedName("final")
    @Expose public Boolean finalText;
}
