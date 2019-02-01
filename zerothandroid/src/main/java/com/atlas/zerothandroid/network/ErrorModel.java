package com.atlas.zerothandroid.network;

public class ErrorModel {

    public int code;
    public String message;
    public String reason;

    public ErrorModel(int code, String message, String reason) {
        this.code = code;
        this.message = message;
        this.reason = reason;
    }
}
