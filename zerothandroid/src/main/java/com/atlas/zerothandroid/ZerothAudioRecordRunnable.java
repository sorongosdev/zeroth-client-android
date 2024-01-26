package com.atlas.zerothandroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * This output send to {@link VisualizerView}
 */

public class ZerothAudioRecordRunnable implements Runnable {
    /*
    마이크가 쓰레드로 동작할 때 쓰레드 안에서 Runnable로서 작동하는 클래스
    녹음시 나오는 화면 중앙의 visualizer와 연결되어 있음
    녹음과 동시에 visualizer에 소리의 크기를 전송
     */

    public AudioRecord audioRecord;
    public int minBufferSize;
    public OnAudioStreamListener listener;
    public boolean use_vad;
    private ZerothAudioRecordRunnable.CalculateVolumeListener mVolumeListener;
    private VisualizerView mVisualizerView = null;

    public ZerothAudioRecordRunnable(@NonNull int sampleRateInHZ,
                                     @NonNull int channels,
                                     @NonNull boolean use_vad,
                                     @NonNull OnAudioStreamListener listener) {

        //웹소켓으로 정보를 보내는 객체가 들어있음
        this.listener = listener;

        //녹음 객체를 만들기 위해 버퍼 사이즈를 생성
        //AudioRecord의 인자로 들어감
        this.minBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHZ,
                channels == ZerothDefine.ZEROTH_MONO ?
                         AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        //녹음을 하는 객체
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRateInHZ,
                channels == ZerothDefine.ZEROTH_MONO ?
                        AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize);

        minBufferSize = getBufferSize(sampleRateInHZ, channels);
        ExLog.i("ZerothAudioRecordRunnable", "ZerothAudioRecordRunnable init"
                + "sampleRateInHZ=" + sampleRateInHZ
                + "audioFormat=" + AudioFormat.ENCODING_PCM_16BIT
                + "channels= " + channels);

        //음성의 크기에 따른 인식을 사용할 것인지
        this.use_vad = use_vad;
    }


    @Override
    public void run() {
        try {
            //최소바이트 단위로 바이트 배열을 생성
            final byte[] buffer = new byte[minBufferSize];
            //녹음을 시작
            audioRecord.startRecording();
            //전역변수인 isFinishing이 false일 동안
            while(!Zeroth.isFinishing) {
                //전역변수인 isStreaming이 true일 동안
                while (Zeroth.isStreaming) {
                    //녹음을 읽어들임
                    audioRecord.read(buffer, 0, buffer.length);
                    if (listener != null) {
                        //listener에 있는 getAudioStreamingData_VAD와 getAudioStreamingData는 Zeroth에 구현되어 있음
                        if(this.use_vad)
                            listener.getAudioStreamingData_VAD(buffer);
                        else
                            listener.getAudioStreamingData(buffer);
                    }
                    //visualizer에 넘겨줄 데시벨 게산
                    int decibel = calculateDecibel(buffer);
                    if (mVisualizerView != null) {
                        //zerothmic의 음성 인식률에 따라 visualizer를 적용
                        if(Zeroth.isSaying) mVisualizerView.receive(decibel);
                        else mVisualizerView.receive(0);
                    }
                    if (mVolumeListener != null) {
                        mVolumeListener.onCalculateVolume(decibel);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        /*
        녹음을 멈출 때 호출
         */
        //visualizer에 0을 넘겨줘 초기상태로 돌림
        if (mVisualizerView != null) {
                mVisualizerView.receive(0);
        }
        //상단의 while문을 정지시키는 역할
        Zeroth.isStreaming = false;
        //녹음을 중지
        audioRecord.stop();
    }

    public void shutdown() {
        //녹음 객체를 해제시킴
        audioRecord.release();
    }

    /**
     * bitrate = bitsperSample * samplePerSecond * channel;
     */
    public int getBufferSize(int sampleRateHZ, int channels) {
        return (int)(((16 * sampleRateHZ * channels) / 8) * 0.25f);
    }



    //visualizer 객체를 저장
    public void link(VisualizerView visualizerView) {
        mVisualizerView = visualizerView;
    }

    /**
     * setter of CalculateVolumeListener
     *
     * @param volumeListener CalculateVolumeListener
     */
    //입력 음성의 크기를 로그로 나타내는 리스너 등록
    public void setVolumeListener(ZerothAudioRecordRunnable.CalculateVolumeListener volumeListener) {
        mVolumeListener = volumeListener;
    }

    //입력된 음성의 크기를 계산하는 함수
    private int calculateDecibel(byte[] buf) {
        int sum = 0;
        for (int i = 0; i < minBufferSize; i++) {
            sum += Math.abs(buf[i]);
        }
        // avg 10-50
        return sum / minBufferSize;
    }


    public interface CalculateVolumeListener {
        void onCalculateVolume(int volume);
    }
}
