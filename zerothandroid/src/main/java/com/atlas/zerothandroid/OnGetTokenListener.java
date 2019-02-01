package com.atlas.zerothandroid;

import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.OAuthToken;

public interface OnGetTokenListener {

    void onGetToken(OAuthToken oAuthToken);
    void onFailed(ErrorModel error);
}
