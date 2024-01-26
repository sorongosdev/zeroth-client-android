package com.atlas.zerothandroid.network;

public class ErrorModel {
    /*
    웹소켓에서 에러를 발생시키면 에러모델과 GsonManager를 이용해 어러 메세지, 에러 코드 등을 저장하기 위한 클래스
     */

    public int code;
    public String message;
    public String reason;

    public ErrorModel(int code, String message, String reason) {
        this.code = code;
        this.message = message;
        this.reason = reason;
    }
}
