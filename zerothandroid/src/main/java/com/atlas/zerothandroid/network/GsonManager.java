package com.atlas.zerothandroid.network;

import android.net.Uri;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GsonManager {
    /*
    java 객체를 json객체로 혹은 반대로 변환하기 위한 메소드들이 모여있는 클래스
    Gson 라이브러리를 사용한다
     */

    public String toJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .registerTypeAdapter(Date.class, new DateSerializer())
                .create();
        return gson.toJson(this);
    }

    public static String toJson(Object T)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .registerTypeAdapter(Date.class, new DateSerializer())
                .create();
        return gson.toJson(T);
    }

    public static String toArrayJson(ArrayList<?> T)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .registerTypeAdapter(Date.class, new DateSerializer())
                .create();
        return gson.toJson(T);
    }

    public static Object fromArrayJson(Type type, String json)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .create();

        return gson.fromJson(json, type);
    }

    public static Object fromJSon(Class T, String json)
    {
        try {
            Gson gson = new GsonBuilder().serializeNulls()
                    .registerTypeAdapter(Uri.class, new UriDeserializer())
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();
            return gson.fromJson(json, T);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object fromJSon(Type T, String json)
    {
        try {
            Gson gson = new GsonBuilder().serializeNulls()
                    .registerTypeAdapter(Uri.class, new UriDeserializer())
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();
            return gson.fromJson(json, T);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bundle toBundle(String json)
    {
        Bundle savedBundle = new Bundle();
        savedBundle.putString("toBundle",json);
        return savedBundle;
    }

    public static Map<String, Object> toMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if(obj == null)
            return map;
        try {
            for(Field f: obj.getClass().getDeclaredFields()) {
                if(f.isAnnotationPresent(Expose.class)) {
                    f.setAccessible(true);
                    if (f.get(obj) != null)
                        map.put(f.getName(), f.get(obj));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> T objectFromHashMap(Object hashMap, Class<T> tClass) {
        String jsonString = toJson(hashMap);
        return (T) fromJSon(tClass, jsonString);
    }

}
