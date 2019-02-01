package com.atlas.zerothandroid;

public interface ZerothMic {

    void startListener();

    void stopListener();

    boolean shutdown();

    void setWebSocketListener(OnWebSocketListener l);

}
