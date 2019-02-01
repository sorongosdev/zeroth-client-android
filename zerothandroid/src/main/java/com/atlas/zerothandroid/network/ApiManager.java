package com.atlas.zerothandroid.network;

import com.atlas.zerothandroid.ZerothDefine;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static Retrofit builder;

    public static ApiService createServer() {
        builder = new Retrofit.Builder()
                .baseUrl(ZerothDefine.API_OAUTH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient())
                .build();
        return builder.create(ApiService.class);
    }

    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getHttpLogginInterceptor())
                .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .build();
    }

    private static Interceptor getHttpLogginInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

}
