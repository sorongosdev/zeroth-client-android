package com.atlas.zerothandroid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.atlas.zerothandroid.network.GsonManager;
import com.atlas.zerothandroid.network.Transcript;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ZerothWebSocket extends WebSocketListener {

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private ArrayList<OnWebSocketListener> listeners;

    private String websocketUrl;
    private OkHttpClient client;
    private WebSocket mWebSocket;
    private OnZerothResult onZerothResult;
    public static ZerothWebSocket instance;

    public static ZerothWebSocket getInstance() {
        if(instance == null) {
            synchronized (ZerothWebSocket.class) {
                if(instance == null) {
                    instance = new ZerothWebSocket();
                }
            }
        }
        return instance;
    }

    public void init(@NonNull String accessToken,
                     @NonNull String language,
                     @NonNull boolean finalOnly,
                     @NonNull String contentType,
                     @NonNull OnZerothResult listener) {
        client = new OkHttpClient();
        websocketUrl = createWWSUrl(accessToken, language, finalOnly, contentType);
        listeners = new ArrayList<>();
        mWebSocket = client.newWebSocket(
                new Request.Builder().url(websocketUrl).build(),
                this);
        onZerothResult = listener;
        ExLog.i("ZerothWebSocket", "ZerothWebSocket init");
    }


    private static String createWWSUrl(String accessToken,
                                      String language,
                                      boolean finalOnly,
                                      String contentType) {
        return String.format(ZerothDefine.API_WWS_URL,
                accessToken,
                language,
                String.valueOf(finalOnly),
                contentType);
    }

    public void stop() {
        mWebSocket.send("\'EOS\'");
    }

    public void shutdown() {
        client.dispatcher().cancelAll();
        client.dispatcher().executorService().shutdown();
    }

    public void send(ByteString byteString) {
        mWebSocket.send(byteString);
    }

    public void setWebSocketListener(@NonNull OnWebSocketListener ls) {
        listeners.add(ls);
    }

    public void addWebSocketListener(@NonNull OnWebSocketListener ls) {
        listeners.add(ls);
    }

    public void removeWebSocketListener(@NonNull OnWebSocketListener ls) {
        listeners.remove(ls);
    }

    private void callbackEvents() {

    }


    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Zeroth.isStreaming = true;
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onOpen(webSocket, response);
            }
        }
        ExLog.i("ZerothWebSocket", "connectSocket");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onMessage(webSocket, text);
            }
        }
        Transcript transcript = (Transcript) GsonManager.fromJSon(Transcript.class, text);
        if(transcript != null) {
            onZerothResult.onMessage(transcript, text);
        }
        ExLog.i("ZerothWebSocket", "onMessage" + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onMessage(webSocket, bytes);
            }
        }
        Log.i("(onMessage)",String.valueOf(bytes));
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onClosing(webSocket, code, reason);
            }
        }
        Zeroth.isStreaming = false;
        ExLog.i("ZerothWebSocket", "onClosing" + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onClosed(webSocket, code, reason);
            }
        }
        Zeroth.isStreaming = false;
        ExLog.i("ZerothWebSocket", "onClosed" + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        if(listeners != null) {
            for(OnWebSocketListener l : listeners) {
                l.onFailure(webSocket, t, response);
            }
        }
        Zeroth.isStreaming = false;
        ExLog.i("ZerothWebSocket", "onFailure" + t.getMessage());
    }
}
