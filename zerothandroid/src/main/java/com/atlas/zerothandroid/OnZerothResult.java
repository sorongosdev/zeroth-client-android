package com.atlas.zerothandroid;

import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.Transcript;

public interface OnZerothResult {
    /*
    웹소켓에서 받은 정보를 처리하기 위한 인터페이스
    mainactivity에서 구현
     */

    void onProgressStatus(ZerothDefine.ZerothStatus status);
    void onMessage(Transcript transcript, String lowData);
    void onFailed(ErrorModel errorModel);

    void doMopen();
    void doMclose();
}
