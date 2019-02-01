package com.atlas.zerothandroid.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class DateDeserializer implements JsonDeserializer<Date> {

    public static final String DATE_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Date dateDate = null;
        String date = json.getAsString();
        if (date != null && date.length() > 0) {
            //Log.d("DATE", "dserialize, date:" + date);
            int dotIndex = date.indexOf('.');
            if (dotIndex > 0) {
                date = date.substring(0, dotIndex);
            }
            //Log.d("DATE", "dserialize, date:" + date);
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_UTC_FORMAT);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                dateDate = formatter.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dateDate;
    }
}