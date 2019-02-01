package com.atlas.zerothandroid;

public class ZerothParam {

    /**
     * accessToken for access to Zeroth Cloud
     */
    public String accessToken;

    /**
     * {@link ZerothDefine#ZEROTH_RATE_16}
     * {@link ZerothDefine#ZEROTH_RATE_44}
     */
    public int audioRate;

    /**
     * {@link ZerothDefine#OPT_16_KHZ}
     * {@link ZerothDefine#OPT_44_KHZ}}
     */
    public String channelConfig;

    /**
     * {@link ZerothDefine#ZEROTH_MONO}
     * {@link ZerothDefine#ZEROTH_STEREO}
     */
    public int channels;

    /**
     * supported language {@link ZerothDefine#ZEROTH_LANG_KOR, {@link ZerothDefine#ZEROTH_LANG_ENG}}
     * default kor
     */
    public String language;

    public boolean isFinal;

}
