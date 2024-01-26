package com.atlas.zerothandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.atlas.zerothandroid.network.ErrorModel;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import okhttp3.WebSocket;
import okio.ByteString;

public class Zeroth{
    /*
    마이크, 소켓 등을 생산 관리하는 클래스
    마이크에 들어가는 정보들도 관리
     */

    private static Context mContext;


    private static ZerothMicImpl mZerothMicImp;
    private static ZerothAudioRecordRunnable mZerothAudioRecordRunnable;
    private static OnZerothResult onZerothResult;

    public static Queue<File> wavs = new LinkedList<>();
    public static File file = null;

    //초기화 여부
    private static boolean mIsInit = false;

    //ZerothDefine에 미리 절정되어 있는 값.
    //마이크, 소켓 생성에 필요한 값.
    private static int audioRate = ZerothDefine.ZEROTH_RATE_16;
    private static int channels = ZerothDefine.ZEROTH_MONO;

    //일정 녹음 입력크기 이상일 때만 작동하게 하는 기능을 킬 것인가
    private static boolean use_vad = true;


    private static boolean final_only   = false;
    //마이크의 상태를 관리하는데 이용
    //이 상태들을 이용하여 녹음을 중지시키고 시작하는 것을 관리
    //ZerothAudioRecordRunnable에서 이용
    public static Boolean isStreaming   = false;
    public static Boolean isFinishing   = false;
    public static Boolean isSaying = false;


    @RequiresPermission("android.permission.INTERNET")
    public static void initialize( @NonNull Context context) {
        /*
        ZerothMicImpl객체 생성 및 mainactivity의 Context 저장
         */
        mContext = context;
        mZerothMicImp = new ZerothMicImpl();
        mIsInit = true;
        isStreaming = false;
        ExLog.i("Zeroth", "initialize");
    }


    private static boolean checkInitialize() {
        if(!mIsInit) {
            throw new RuntimeException("first need to Zeroth.Initialize()");
        }
        return mIsInit;
    }

    public static ZerothMic createZerothMic(@NonNull ZerothParam param,
                                            @NonNull OnZerothResult onZerothResultListener,
                                            @NonNull VisualizerView mvisualizerview,
                                            @NonNull TextView extext) {
        /*
        마이크 객체를 생성하는 클래스
         */

        //초기화를 했는지 체크
        checkInitialize();
        isFinishing = false;

        //마이크 생성을 위한 정보가 들어있는 param 객체를 인자로 받아 정보를 현재 클래스에 저장
        audioRate       = param.audioRate;
        channels        = param.channels;

        //웹소켓 통신 후 받아온 결과를 컨트롤하는 객체를 인자로 받아 저장
        onZerothResult  = onZerothResultListener;

        //마이크를 이용해 녹음 시 실제로 동작하는 runnable 객체를 생성
        //ZerothAudioRecordRunnable 클래스에서 자세히 설명
        mZerothAudioRecordRunnable = new ZerothAudioRecordRunnable(
                audioRate,
                channels,
                use_vad,
                mZerothMicImp);

        //녹음시 화면 중앙에 나타나는 음성 녹음 시각화 객체를 마이크 객체에 연결
        mZerothMicImp.setvisualizer(mvisualizerview);
        //화면 상단에 있는 설명 텍스트를 마이크 객체에 연결
        //녹음상태에 따라 설명 텍스트를 바꿔주기 위함
        mZerothMicImp.setextext(extext);
        //현재 앱 상태를 init으로 변경
        onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.INIT);
        return mZerothMicImp;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void requestActivityPermission(Activity activity) {
        /*
        마이크 권한을 요청
         */
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                ZerothDefine.REQUEST_PERMISSIONS_RECORD_AUDIO);
    }


    private static class ZerothMicImpl implements ZerothMic, OnAudioStreamListener, OnWebSocketListener, ZerothAudioRecordRunnable.CalculateVolumeListener{
        /*
        마이크 객체와 웹소켓 객체를 관리
        녹음,웹소켓으로 전송하는 기능
         */
        /*
        implements
            //ZerothMic: 녹음 인터페이스
            //OnAudioStreamListener: 음성의 크기를 인지해 일정 크기 이상에서만 작동하는 getAudioStreamingData_VAD와 getAudioStreamingData
                                    //웹소켓으로 전송하는 작업이 이루어짐
            //OnWebSocketListener: 웹소켓에서 받은 정보를 처리하는 메소드들 ex)onmessage
            //ZerothAudioRecordRunnable.CalculateVolumeListener: 녹음하는 음성의 크기를 로그로 보여줌
         */
        private Thread mMicThread;
        private OnWebSocketListener listener;

        private double dynamic_energy_adjustment_damping;
        private double dynamic_energy_ratio;
        private double energy_threshold;
        private double energy;
        private double damping;
        private double target_energy;
        private int curr_state;
        private int prev_state;
        private int pause_count;
        private int pause_threshold;
        private int pause_buffer_count;
        private ByteBuffer buffer;
        private double seconds_per_buffer;
        public VisualizerView mVisualizerView2;
        public TextView explainText;

        public void setextext(TextView extext){
            /*
            화면 상단 설명 텍스트를 변경하기 위해 객체를 저장
             */
            explainText = extext;
        }

        @Override
        public void onCalculateVolume ( int volume){
            //visualizer와 mic의 기능중복으로 인한 주석처리
            //Log.d("T", String.valueOf(volume));
        }

        public void setvisualizer(VisualizerView visualizerview){
            /*
            녹음시 화면 중앙에 나오는 음성 시각화 객체 세팅
            ZerothAudioRecordRunnable 클래스에 사용됨
             */
            mVisualizerView2 = visualizerview;
            mZerothAudioRecordRunnable.setVolumeListener(this);
            mZerothAudioRecordRunnable.link(mVisualizerView2);
        }

        public void socketInit(){
            /*
            ZerothWebScocket 객체 초기화
            ZerothWebSocket 클래스에 설명
             */
            //소켓 연결 주소 변경시에는 zerothDefine class 변경
                ZerothWebSocket.getInstance().init(onZerothResult);
                ZerothWebSocket.getInstance().setWebSocketListener(this);
        }

        @Override
        public void startListener() {

            //녹음 시작할 때 변환을 위한 캐시,배열 초기화
            //지정된 바이트 단위로 짤라서 각각을 wav파일로 들을 수 있게 함
            //현재는 쓰이지 않는 기능
            file = null;
            clearCache();
            wavs.clear();
            wavs = null;
            wavs = new LinkedList<>();

            socketInit();
            dynamic_energy_adjustment_damping = 0.15;
            dynamic_energy_ratio = 1.5;
            // 민감도: 높은 값을 잡을 수록 작은 소리에는 오디오 전송을 시작하지 않음
            energy_threshold = 300;
            // 단위: 초, 말소리가 작아진 뒤 몇 초를 기다리고 끊을 것인지 결정
            pause_threshold = 4;
            curr_state  = 0;
            prev_state  = 0;
            pause_count = 0;
            seconds_per_buffer = (double)mZerothAudioRecordRunnable.minBufferSize / 2.0 / (double)audioRate; // 8000/2/16000
            buffer = ByteBuffer.allocate(mZerothAudioRecordRunnable.minBufferSize * 4);
            pause_buffer_count = (int)Math.ceil((pause_threshold / seconds_per_buffer)); //

            isStreaming = true;
            //마이크 쓰레드 생성
            mMicThread = new Thread(mZerothAudioRecordRunnable);
            mMicThread.start();
            //상태 변경
            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.RUNNING);
            ExLog.i("ZerothMic", "startListener");
        }

        @Override
        public void stopListener() {
            //전역변수 isSaying, isStreaming의 상태 변경
            isSaying = false;
            isStreaming = false;
            //마이크 쓰레드 종료
            if(mMicThread.isAlive()) {
                mMicThread.interrupt();
            }
            mZerothAudioRecordRunnable.stopRecording();
            //앱 상태 변경
            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.STOPED);
        }

        @Override
        public boolean shutdown() {
            /*
            stopListener와 같은 기능으로 웹소켓 shutdown이 추가되어 있고 마이크runnable 객체도 객체 내부에 있는 shutdown함수 호출
             */
            isStreaming = false;
            isFinishing = true;
            if(mMicThread.isAlive()) {
                mMicThread.interrupt();
                mMicThread = null;
            }
            ZerothWebSocket.getInstance().shutdown();
            mZerothAudioRecordRunnable.shutdown();
            onZerothResult.onProgressStatus(ZerothDefine.ZerothStatus.SHUTDOWN);
            return false;
        }

        @Override
        public void setWebSocketListener(OnWebSocketListener l) {
            listener = l;
        }

        public double getRMS(byte[] bytes) {
            /*
            음성 녹음 시 현재 음성의 크기를 계산하는 함수
            float형식으로 음성의 크기를 반환
             */
            // convert byte[] into short[]
            short[] shorts = new short[bytes.length/2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            // compute RMS value
            double sum = 0;
            for(int i=0; i<shorts.length; i++)
                sum += shorts[i] * shorts[i];
            sum /= (double) shorts.length;
            return Math.sqrt(sum);
        }

        @Override
        public void getAudioStreamingData(byte[] bytes) {
            /*
            음성의 크기에 상관없이 전송하려면 usevad 변수를 false로 하고 이 함수를 이용
            현재는 이용하지 않음
             */
//            ZerothWebSocket.getInstance().send(ByteString.of(bytes));
        }

        @Override
        public void getAudioStreamingData_VAD(byte[] bytes) {
            /*
            음성이갑의 크기 energy_threshold변수의 값보다 크면 웹소켓을 통해 서버로 음성 byte를 전송
             */

            // buffer에 인자로 전달받은 byte를 넣음
            buffer.put(bytes);
            //버퍼 안의 값을 남김없이 저장
            if(!buffer.hasRemaining()){
                byte[] front = new byte[bytes.length];
                buffer.flip();
                buffer.get(front);
                buffer.compact();
            }

            // 음성 입력 크기 저장
            energy = getRMS(bytes);

            // dynamic threshold control (only in initial state)
            if (curr_state == 0) {
                damping = Math.pow(dynamic_energy_adjustment_damping, seconds_per_buffer);
                target_energy = energy * dynamic_energy_ratio;
                energy_threshold = energy_threshold * damping + target_energy * (1 - damping);
            }
            ExLog.i("ZerothMic", "energy = " + energy
                    + ", threshold = " + energy_threshold
                    + ", pause_count = " + pause_count
                    + ", curr_state = " + curr_state);

            // 입력 음성의 크기가 지정해놓은 크기 이상이면 상태를 1로 바꾸고 pause_count를 0으로 설정
            if (energy > energy_threshold) {
                curr_state = 1;
                pause_count = 0;
                isSaying = true;
            } else {
                //정지되어 있는 동안 pause_count 증가
                pause_count += 1;
                isSaying = false;
            }


            if (prev_state == 0 && curr_state == 1){
                // 음성으로 인식 최초 시작
                byte[] mbyte = buffer.array();
                int num = mbyte.length/bytes.length;
                int bytessize = bytes.length;
                //최소 바이트 단위로 나눠서 전송
                for(int i = 0;i<num;i++){
                    ZerothWebSocket.getInstance().send(ByteString.of(Arrays.copyOfRange(mbyte,i*bytessize,(i+1)*bytessize-1)));
                }
                //전송했으므로 버퍼를 비움
                buffer.clear();
            } else if (curr_state == 1) {
                //웹 소켓을 이용해 전송
                ZerothWebSocket.getInstance().send(ByteString.of(bytes));
            }

            //침묵 감지
            //설정해둔 pause_buffer_count(초) 보다 오래 침묵이 감지되면 녹음을 종료
            if (pause_count > pause_buffer_count){
                ExLog.i("ZerothMic", "Silence Detected");
                isStreaming = false;
                //UI를 변경해야하는 작업이기 때문에 Handler를 통해 mianthread를 이용
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onZerothResult.doMclose();
                        Toast.makeText(mContext,"침묵이 감지되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            //이전 상태 저장
            //쉬었다가 녹음하는 상태인지 계속 녹음중인 상태인지를 구분하기 위함
            prev_state = curr_state;
        }

        /*
        pcm 데이터를 wav 파일로 변환하기 위함
        byte 형식의 데이터에 헤더파일을 추가하여 파일로 저장하는 형식
        현재는 쓰이지 않는 기능
         */
        private void rawToWave(final byte[] rawdata, final File waveFile) throws IOException {

            byte[] rawData = new byte[rawdata.length];


            DataOutputStream output = null;
            try {
                output = new DataOutputStream(new FileOutputStream(waveFile));
                // WAVE header
                // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
                writeString(output, "RIFF"); // chunk id
                writeInt(output, 36 + rawData.length); // chunk size
                writeString(output, "WAVE"); // format
                writeString(output, "fmt "); // subchunk 1 id
                writeInt(output, 16); // subchunk 1 size
                writeShort(output, (short) 1); // audio format (1 = PCM)
                writeShort(output, (short) 1); // number of channels
                writeInt(output, ZerothDefine.ZEROTH_RATE_16); // sample rate
                writeInt(output, ZerothDefine.ZEROTH_RATE_16 * 2); // byte rate
                writeShort(output, (short) 2); // block align
                writeShort(output, (short) 16); // bits per sample
                writeString(output, "data"); // subchunk 2 id
                writeInt(output, rawData.length); // subchunk 2 size
                // Audio data (conversion big endian -> little endian)
                short[] shorts = new short[rawData.length / 2];
                ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
                ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
                for (short s : shorts) {
                    bytes.putShort(s);
                }

                output.write(rawdata);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }

        private void writeInt(final DataOutputStream output, final int value) throws IOException {
            output.write(value >> 0);
            output.write(value >> 8);
            output.write(value >> 16);
            output.write(value >> 24);
        }

        private void writeShort(final DataOutputStream output, final short value) throws IOException {
            output.write(value >> 0);
            output.write(value >> 8);
        }

        private void writeString(final DataOutputStream output, final String value) throws IOException {
            for (int i = 0; i < value.length(); i++) {
                output.write(value.charAt(i));
            }
        }

        //캐시에 wav파일 임시 생성을 위한 메소드 + Queue에 삽입
        public File createFile(String name){
            File storage = mContext.getCacheDir(); //임시파일 저장 경로
            String fileName = name + ".wav";  //파일이름//순차적으로 매길 예정
            File newFile = new File(storage,fileName);
            try{
                newFile.createNewFile();  // 파일 생성
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //newFile.getAbsolutePath(); //절대경로가 필요할 시 이용가능
            return newFile;
        }

        //사용한 캐시를 다시 없애기 위한 메소드
        public void clearCache() {
            final File cacheDirFile = mContext.getCacheDir();
            if (null != cacheDirFile && cacheDirFile.isDirectory()) {
                clearSubCacheFiles(cacheDirFile);
            }
        }
        public void clearSubCacheFiles(File cacheDirFile) {
            if (null == cacheDirFile || cacheDirFile.isFile()) {
                return;
            }
            for (File cacheFile : cacheDirFile.listFiles()) {
                if (cacheFile.isFile()) {
                    if (cacheFile.exists()) {
                        cacheFile.delete();
                    }
                } else {
                    clearSubCacheFiles(cacheFile);
                }
            }
        }


        /*
        웹소켓 리스너
        ZerothWebSocket 클래스에 실질적인 내용이 구현(설명도 ZerothWebSocket에 있음)
        */
        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            if(listener != null) {
                listener.onOpen(webSocket,response);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if(listener != null) {
                listener.onMessage(webSocket, text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if(listener != null) {
                listener.onMessage(webSocket,bytes);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            onZerothResult.onFailed(new ErrorModel(code, "", reason));
            if(listener != null) {
                listener.onClosing(webSocket, code,reason);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            onZerothResult.onFailed(new ErrorModel(code, "", reason));
            if(listener != null) {
                listener.onClosed(webSocket, code,reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable okhttp3.Response response) {
            int code = response != null ? response.code() : ZerothDefine.ERROR_SOCKET_FAIL;
            onZerothResult.onFailed(new ErrorModel(code, t.getMessage(), t.getLocalizedMessage()));
            if(listener != null) {
                listener.onFailure(webSocket,t,response);
            }
        }
    }
}
