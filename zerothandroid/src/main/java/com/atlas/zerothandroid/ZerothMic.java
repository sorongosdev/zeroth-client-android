package com.atlas.zerothandroid;

public interface ZerothMic {
    /*
    마이크 구현을 위한 인터페이스
    구현은 Zeroth클래스에서 이루어짐
     */

    void startListener();

    void stopListener();

    boolean shutdown();

    void setWebSocketListener(OnWebSocketListener l);

}
