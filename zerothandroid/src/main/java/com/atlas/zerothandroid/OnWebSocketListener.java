package com.atlas.zerothandroid;

import androidx.annotation.Nullable;

import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public interface OnWebSocketListener {
    /*
    웹소켓에서 정보가 들어올 때 반응하기 위한 인터페이스
    ZerothWebSocket 클래스에 구현
    OnZerothResult를 함수안에 이용하여 처리 방법을 구현
     */

    void onOpen(WebSocket webSocket, Response response);

    void onMessage(WebSocket webSocket, String text);

    void onMessage(WebSocket webSocket, ByteString bytes);

    void onClosing(WebSocket webSocket, int code, String reason);

    void onClosed(WebSocket webSocket, int code, String reason);

    void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response);
}
