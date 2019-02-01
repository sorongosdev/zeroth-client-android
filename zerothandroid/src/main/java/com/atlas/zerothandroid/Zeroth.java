package com.atlas.zerothandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import com.atlas.zerothandroid.network.ApiManager;
import com.atlas.zerothandroid.network.ApiService;
import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.OAuthToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.WebSocket;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Zeroth {

    private static Context mContext;
    private static String appId;
    private static String appSecret;

    private static ZerothMicImpl mZerothMicImp;
    private static ZerothAudioRecordRunnable mZerothAudioRecordRunnable;
    private static OnZerothResult onZerothResult;

    /**
     * AccessToken 을 얻기 위한 retrofit Service
     */
    private static ApiService mApiService;

    /**
     * 초기화 여부
     */
    private static boolean mIsInit = false;

    /**
     * {@link Zeroth#getToken()}
     */
    private static String accessToken;

    /**
     * 16000, 44100
     */
    private static int audioRate = ZerothDefine.ZEROTH_RATE_16;

    /**
     * {@link ZerothDefine#OPT_16_KHZ}
     * {@link ZerothDefine#OPT_44_KHZ}
     */
    private static String channelConfig;

    private static int channels = ZerothDefine.ZEROTH_MONO;

    /**
     * supported language {eng, kor} default kor
     */
    private static String language = ZerothDefine.ZEROTH_LANG_KOR;

    private static boolean final_only   = false;
    public static Boolean isStreaming   = false;
    public static Boolean isFinishing   = false;


    @RequiresPermission("android.permission.INTERNET")
    public static void initialize( @NonNull Context context,
                            @NonNull String pAppId,
                            @NonNull String pAppSecret) {
        if(pAppId.length() == 0 || pAppSecret.length() == 0) {
            throw new RuntimeException("AppId or AppSecret is null");
        }

        //Api init
        mContext = context;
        appId = pAppId;
        appSecret = pAppSecret;
        mZerothMicImp = new ZerothMicImpl();

        /**
         * retrofit 초기화
         */
        mApiService = ApiManager.createServer();

        mIsInit = true;
        isStreaming = false;
        ExLog.i("Zeroth", "initialize");
    }

    public static Context get() {
        if(!mIsInit) {
            try {
                throw new IllegalAccessException("first need to Zeroth.Initialize()");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mContext;
    }


    /**
     * Asynchronously send the request
     */
    public static void getToken(final OnGetTokenListener onGetTokenListener) {
        checkInitialize();

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization",appId+":"+appSecret);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept-Language", Locale.getDefault().getLanguage());
        mApiService.getToken(headerMap).enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                if(response.isSuccessful()) {
                    onGetTokenListener.onGetToken(response.body());
                } else {
                    onGetTokenListener.onFailed(new ErrorModel(
                            response.code(),
                            response.message(),
                            ""));
                }
                ExLog.i("Zeroth", "getToken=" + response.body().access_token);
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                onGetTokenListener.onFailed(new ErrorModel(
                        ZerothDefine.ERROR_GET_TOKEN_FAIL,
                        t.getMessage(),
                        t.getLocalizedMessage()));
                ExLog.i("Zeroth", "getToken fail");
            }
        });
    }

    /**
     * Synchronously send the request and return its response.
     */
    public static OAuthToken getToken() {
        checkInitialize();

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("",appId+":"+appSecret);
        try {
            return mApiService.getToken(headerMap).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static boolean checkInitialize() {
        if(!mIsInit) {
            throw new RuntimeException("first need to Zeroth.Initialize()");
        }
        return mIsInit;
    }

    public static ZerothMic createZerothMic(@NonNull ZerothParam param,
                                            @NonNull OnZerothResult onZerothResultListener) {
        checkInitialize();
        isFinishing = false;

        accessToken     = param.accessToken;
        audioRate       = param.audioRate;
        language        = param.language;
        channels        = param.channels;
        onZerothResult  = onZerothResultListener;
        channelConfig   = String.format(param.audioRate == ZerothDefine.ZEROTH_RATE_16 ?
                        ZerothDefine.OPT_16_KHZ : ZerothDefine.OPT_44_KHZ, param.channels);
        mZerothAudioRecordRunnable = new ZerothAudioRecordRunnable(
                audioRate,
                channels,
                mZerothMicImp);
        onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.INIT);

        return mZerothMicImp;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void requestActivityPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                ZerothDefine.REQUEST_PERMISSIONS_RECORD_AUDIO);
    }

    public static boolean checkPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }


    private static class ZerothMicImpl implements ZerothMic, OnAudioStreamListener, OnWebSocketListener{

        private Thread mMicThread;
        private OnWebSocketListener listener;

        @Override
        public void startListener() {

            /**
             * start socket
             */
            ZerothWebSocket.getInstance().init(accessToken, language, final_only, channelConfig, onZerothResult);
            ZerothWebSocket.getInstance().setWebSocketListener(this);

            mMicThread = new Thread(mZerothAudioRecordRunnable);
            mMicThread.start();

            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.RUNNING);
            ExLog.i("ZerothMic", "startListener");
        }

        @Override
        public void stopListener() {
            isStreaming = false;
            if(mMicThread.isAlive()) {
                mMicThread.interrupt();
            }

            ZerothWebSocket.getInstance().stop();
            mZerothAudioRecordRunnable.stopRecording();
            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.STOPED);
        }

        @Override
        public boolean shutdown() {
            isStreaming = false;
            isFinishing = true;
            if(mMicThread.isAlive()) {
                mMicThread.interrupt();
                mMicThread = null;
            }
            ZerothWebSocket.getInstance().shutdown();
            mZerothAudioRecordRunnable.shutdown();
            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.SHUTDOWN);
            return false;
        }

        @Override
        public void setWebSocketListener(OnWebSocketListener l) {
            listener = l;
        }

        @Override
        public void getAudioStreamingData(byte[] bytes) {
            ZerothWebSocket.getInstance().send(ByteString.of(bytes));
        }

        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            if(listener != null) {
                listener.onOpen(webSocket,response);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if(listener != null) {
                listener.onMessage(webSocket,text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if(listener != null) {
                listener.onMessage(webSocket,bytes);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            onZerothResult.onFailed(new ErrorModel(code, "", reason));
            if(listener != null) {
                listener.onClosing(webSocket, code,reason);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            onZerothResult.onFailed(new ErrorModel(code, "", reason));
            if(listener != null) {
                listener.onClosed(webSocket, code,reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable okhttp3.Response response) {
            int code = response != null ? response.code() : ZerothDefine.ERROR_SOCKET_FAIL;
            onZerothResult.onFailed(new ErrorModel(code, t.getMessage(), t.getLocalizedMessage()));
            if(listener != null) {
                listener.onFailure(webSocket,t,response);
            }
        }
    }

}
