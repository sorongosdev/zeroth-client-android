package com.atlas.zerothandroid;

public class ZerothDefine {

    public static final String API_OAUTH_URL = "https://zeroth.goodatlas.com:2053";
    public static final String API_WWS_URL = "wss://zeroth.goodatlas.com:2087/client/ws/speech?access-token=%s&language=%s&final-only=%s&content-type=%s";

    public static final String OPT_16_KHZ = "audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)%d";
    public static final String OPT_44_KHZ = "audio/x-raw,+layout=(string)interleaved,+rate=(int)44100,+format=(string)S16LE,+channels=(int)%d";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";

    public static final String ZEROTH_LANG_KOR = "kor";
    public static final String ZEROTH_LANG_ENG = "eng";

    public static final int ZEROTH_RATE_16 = 16000;
    public static final int ZEROTH_RATE_44 = 44100;

    public static final int ZEROTH_MONO     = 1;
    public static final int ZEROTH_STEREO   = 2;

    public static final int ERROR_GET_TOKEN_FAIL    = 1000;
    public static final int ERROR_SOCKET_FAIL       = 1001;

    public static final int REQUEST_PERMISSIONS_RECORD_AUDIO = 1;

    public enum ZerothStatus {
        IDLE,
        INIT,
        RUNNING,
        STOPED,
        SHUTDOWN
    }
}
