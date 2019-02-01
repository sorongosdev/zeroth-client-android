package com.atlas.zerothandroid;

import android.support.annotation.Nullable;

import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public interface OnWebSocketListener {

    void onOpen(WebSocket webSocket, Response response);

    void onMessage(WebSocket webSocket, String text);

    void onMessage(WebSocket webSocket, ByteString bytes);

    void onClosing(WebSocket webSocket, int code, String reason);

    void onClosed(WebSocket webSocket, int code, String reason);

    void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response);
}
