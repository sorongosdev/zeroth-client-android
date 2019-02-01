package com.atlas.zerothandroid;

public class ZerothParam {

    public String accessToken;

    /**
     * 16000, 44100
     */
    public int audioRate;

    /**
     * {@link ZerothDefine#OPT_16_KHZ_MONO, @link {@link ZerothDefine#OPT_44_KHZ_MONO}}
     */
    public String channelConfig;

    /**
     * supported language {eng, kor} default kor
     */
    public String language;

    public boolean isFinal;

    public int bufferSize;


}
