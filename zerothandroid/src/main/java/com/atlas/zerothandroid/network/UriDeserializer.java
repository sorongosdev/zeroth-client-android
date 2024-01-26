package com.atlas.zerothandroid.network;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UriDeserializer implements JsonDeserializer<Uri> {
    @Override
    //직렬화된 데이터를 Uri 객체로 구성하여 반환
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Uri.parse(json.getAsString());
    }
}
