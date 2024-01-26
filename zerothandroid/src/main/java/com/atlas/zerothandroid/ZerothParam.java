package com.atlas.zerothandroid;

public class ZerothParam {
    /*
    마이크, 웹소켓 생성시 필요한 정보들을 담는 데이터 클래스
     */

    /**
     * {@link ZerothDefine#ZEROTH_RATE_16}
     * {@link ZerothDefine#ZEROTH_RATE_44}
     */
    public int audioRate;

    /**
     * {@link ZerothDefine#ZEROTH_MONO}
     * {@link ZerothDefine#ZEROTH_MONO}
     */
    public int channels;

    /**
     * supported language {@link ZerothDefine#ZEROTH_LANG_KOR, {@link ZerothDefine#ZEROTH_LANG_ENG}}
     * default kor
     */
    public String language;

    public boolean isFinal;

}
