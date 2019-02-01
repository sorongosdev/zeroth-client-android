package com.atlas.zerothandroid.network;

import com.google.gson.annotations.Expose;

/**
 * {"access_token":"abcd","token_type":"Bearer","expires_in":600}
 */
public class OAuthToken {

    @Expose public String access_token;
    @Expose public String token_type;
    @Expose public Integer expires_in;
}
