package com.atlas.zerothandroid;

public interface OnAudioStreamListener {
    /*
    웹소켓으로 보내는 방법을 위한 인터페이스
    _VAD는 입력 음성의 크기로 말하는 중인지를 판단하여 말하는 중일때만 전송하도록 구현
    Zeroth클래스의 하단에 구현되어 있음
     */

    void getAudioStreamingData(byte[] bytes);
    void getAudioStreamingData_VAD(byte[] bytes);
}
