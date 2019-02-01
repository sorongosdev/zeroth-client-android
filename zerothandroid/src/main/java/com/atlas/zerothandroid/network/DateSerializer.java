package com.atlas.zerothandroid.network;

import com.atlas.zerothandroid.ZerothDefine;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jay on 2015. 7. 16..
 */
public class DateSerializer implements JsonSerializer<Date> {
    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        SimpleDateFormat formatter = new SimpleDateFormat(ZerothDefine.DATE_UTC_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new JsonPrimitive(formatter.format(src));
    }
}
