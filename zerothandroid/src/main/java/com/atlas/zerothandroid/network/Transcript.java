package com.atlas.zerothandroid.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transcript {
    /*
    번역된 텍스트를 마지막 문장인지 알 수 있는 finalText라는 bool값과 함께 저장하기 위한 클래스
    웹소켓에서 결과를 받을 때 Transcript를 이용하여 객체를 생성하고 어댑터가 객체의 정보들을 이용해 리스트뷰에 적용
     */

    @Expose public String transcript;

    @SerializedName("final")
    @Expose public Boolean finalText;
}
