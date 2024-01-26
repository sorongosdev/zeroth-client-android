package com.atlas.zerothandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import com.atlas.zerothandroid.network.Transcript;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ZerothWebSocket extends WebSocketListener {
    /*
        웹소켓을 연결하고 메세지가 왔을 때 등의 상황에 대한 조작이 구현되어 있는 클래스
     */

    private ArrayList<OnWebSocketListener> listeners;
    private OkHttpClient client;
    private WebSocket mWebSocket;
    private OnZerothResult onZerothResult;
    public static ZerothWebSocket instance;

    public static ZerothWebSocket getInstance() {
        /*
        ZerothWebSocket 객체를 반환
        없다면 생성,저장 후 반환
         */
        if(instance == null) {
            synchronized (ZerothWebSocket.class) {
                if(instance == null) {
                    instance = new ZerothWebSocket();
                }
            }
        }
        return instance;
    }

    public void init(@NonNull OnZerothResult listener) {
        /*
        웹소켓 서버에 연결하는 함수
        okhttp3 라이브러리를 사용
         */
        //웹소켓 클라이언트 객체 생성
        client = new OkHttpClient();
        listeners = new ArrayList<>();
        onZerothResult = listener;
        try {
            //서버에서 요구하는 형식으로 url 생성 후 연결 요청
            //ZerothDefine에 정의되어 있음
            String url = String.format(ZerothDefine.MY_URL_test,URLEncoder.encode(ZerothDefine.MY_URL_DATA_test, "UTF-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            mWebSocket = client.newWebSocket(request, this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ExLog.i("ZerothWebSocket", "WebSocket init");
    }

    public void shutdown() {
        /*
        웹소켓 연결을 종료
         */
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

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        /*
        연결되었을 때 실행
         */
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
        try {
            /*
            서버에서 jsonString으로 보내준 결과를 jsonObect로 변환하여 사용
            서버에서 받은 번역된 String과 마지막 문장인지를 알려주는 isfinal 정보를 transcript객체에 담아 onZerothResult 에서 처리
            onZerothResult에서는 커스텀 어댑터를 이용해 화면 하단의 리스트뷰에 적용
             */

            JSONObject x = new JSONObject(text);
            if(x != null) {
                Transcript transcript = new Transcript();
                transcript.finalText = x.getJSONObject("result").getBoolean("final");
                transcript.transcript = x.getJSONObject("result").getJSONArray("hypotheses").getJSONObject(0).getString("transcript");
                onZerothResult.onMessage(transcript, text);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ExLog.i("ZerothWebSocket", "onMessage(String)");
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        /*
        서버측에서결과를 byte형식으로 보내줄 경우 사용되는 함수
        현재 사용되지 않음
         */
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
        /*
        웹소켓이 닫힐 때 실행되는 함수
         */
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
        /*
        닫힌 후에 실행되는 함수
         */
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
        /*
        웹소켓 연결에 실패했을 때 실행되는 함수
         */
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
