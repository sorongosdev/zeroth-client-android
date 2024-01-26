package com.atlas.zerothandroid;

public class ZerothDefine {
    /*
    마이크, 웹소켓 연결에 필요한 정보들과 앱의 상태를 전역으로 미리 정의해놓은 클래스
     */

    //웹소켓 연결 주소
    public static final String MY_URL_test = "ws://119.207.210.70:16007/client/ws/speech?content-type=%s";
    public static final String MY_URL_DATA_test = "audio/x-raw, layout=(string)interleaved, rate=(int)16000, format=(string)S16LE, channels=(int)1";

    public static final String DATE_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";

    public static final String ZEROTH_LANG_KOR = "kor";
    public static final String ZEROTH_LANG_ENG = "eng";

    //웹소켓, 마이크 음성 등급
    public static final int ZEROTH_RATE_16 = 16000;
    public static final int ZEROTH_RATE_44 = 44100;

    //웹소켓 채널 개수 관련
    public static final int ZEROTH_MONO     = 1;
    public static final int ZEROTH_STEREO   = 2;

    //웹소켓 연결에 실패했을 때 에러코드
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
