package com.atlas.zerothandroid;

import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.Transcript;

public interface OnZerothResult {

    void onProgressStatus(ZerothDefine.ZerothStatus status);
    void onMessage(Transcript transcript, String lowData);
    void onFailed(ErrorModel errorModel);
}
