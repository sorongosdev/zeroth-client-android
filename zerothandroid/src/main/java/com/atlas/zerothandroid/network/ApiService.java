package com.atlas.zerothandroid.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;

public interface ApiService {

    @GET("/token")
    Call<OAuthToken> getToken(@HeaderMap Map<String, String> headers);

}
