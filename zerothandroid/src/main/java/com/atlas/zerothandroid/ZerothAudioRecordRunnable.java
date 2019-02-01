package com.atlas.zerothandroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

public class ZerothAudioRecordRunnable implements Runnable {

    public AudioRecord audioRecord;
    public int minBufferSize;
    public OnAudioStreamListener listener;

    public ZerothAudioRecordRunnable(@NonNull int sampleRateInHZ,
                                     @NonNull int audioFormat,
                                     @NonNull OnAudioStreamListener listener) {

        this.listener = listener;
        this.minBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioFormat);
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRateInHZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioFormat,
                minBufferSize);

        minBufferSize = 8000;
        ExLog.i("ZerothAudioRecordRunnable", "ZerothAudioRecordRunnable init"
                + "sampleRateInHZ=" + sampleRateInHZ
                + "audioFormat=" + audioFormat);
    }

    @Override
    public void run() {
        try {
            final byte[] buffer = new byte[minBufferSize];
            audioRecord.startRecording();
            while(!Zeroth.isFinishing) {
                while (Zeroth.isStreaming) {
                    audioRecord.read(buffer, 0, buffer.length);
                    if (listener != null) {
                        listener.getAudioStreamingData(buffer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        Zeroth.isStreaming = false;
        audioRecord.stop();
    }

    public void shutdown() {
        audioRecord.release();
    }

}
