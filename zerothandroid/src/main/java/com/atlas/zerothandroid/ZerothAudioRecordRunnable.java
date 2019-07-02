package com.atlas.zerothandroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

public class ZerothAudioRecordRunnable implements Runnable {

    public AudioRecord audioRecord;
    public int minBufferSize;
    public OnAudioStreamListener listener;
    public boolean use_vad;

    public ZerothAudioRecordRunnable(@NonNull int sampleRateInHZ,
                                     @NonNull int channels,
                                     @NonNull boolean use_vad,
                                     @NonNull OnAudioStreamListener listener) {

        this.listener = listener;
        this.minBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHZ,
                channels == ZerothDefine.ZEROTH_MONO ?
                         AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
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
        this.use_vad = use_vad;
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
                        if(this.use_vad)
                            listener.getAudioStreamingData_VAD(buffer);
                        else
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

    /**
     * bitrate = bitsperSample * samplePerSecond * channel;
     */
    public int getBufferSize(int sampleRateHZ, int channels) {
        return (int)(((16 * sampleRateHZ * channels) / 8) * 0.25f);
    }

}
