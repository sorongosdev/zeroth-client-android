package com.sample.atlas.atalssample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.atlas.zerothandroid.ExLog;
import com.atlas.zerothandroid.OnGetTokenListener;
import com.atlas.zerothandroid.OnWebSocketListener;
import com.atlas.zerothandroid.OnZerothResult;
import com.atlas.zerothandroid.Zeroth;
import com.atlas.zerothandroid.ZerothDefine;
import com.atlas.zerothandroid.ZerothMic;
import com.atlas.zerothandroid.ZerothParam;
import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.GsonManager;
import com.atlas.zerothandroid.network.OAuthToken;
import com.atlas.zerothandroid.network.Transcript;

import java.util.ArrayList;

import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    public TextView sampleRate16000, sampleRate44100;
    public TextView lblWWSStart;

    public ZerothMic mZerothMic;
    public ZerothParam mParam;
    public ListView listview;
    public TranscriptAdapter mAdapter;
    public ZerothDefine.ZerothStatus mStatus = ZerothDefine.ZerothStatus.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new TranscriptAdapter();
        listview = findViewById(R.id.listview);
        listview.setAdapter(mAdapter);

        //request Permission
        Zeroth.requestActivityPermission(MainActivity.this);

        sampleRate16000 = findViewById(R.id.sampleRate16000);
        sampleRate16000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParam = new ZerothParam();
                mParam.language = ZerothDefine.ZEROTH_LANG_KOR;
                mParam.channels = ZerothDefine.ZEROTH_MONO;
                mParam.audioRate = ZerothDefine.ZEROTH_RATE_16;
                mParam.isFinal = false;
                startZeroth(mParam);

            }
        });

        sampleRate44100 = findViewById(R.id.sampleRate44100);
        sampleRate44100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParam = new ZerothParam();
                mParam.language = ZerothDefine.ZEROTH_LANG_KOR;
                mParam.channels = ZerothDefine.ZEROTH_MONO;
                mParam.audioRate = ZerothDefine.ZEROTH_RATE_44;
                mParam.isFinal = false;
                startZeroth(mParam);
            }
        });

    }

    public void startZeroth(final ZerothParam param) {
        if(mStatus == ZerothDefine.ZerothStatus.RUNNING) {
            mZerothMic.stopListener();
            mZerothMic.shutdown();
            mAdapter.init();
        }
        Zeroth.getToken(new OnGetTokenListener() {
            @Override
            public void onGetToken(OAuthToken oAuthToken) {
                param.accessToken = oAuthToken.access_token;
                mZerothMic = Zeroth.createZerothMic(param, new OnZerothResult() {

                    @Override
                    public void onProgressStatus(ZerothDefine.ZerothStatus status) {
                        mStatus = status;
                        invalidateOptionsMenu();
                        ExLog.i("ZerothResult","onProgressStatus=" + status.name());
                    }

                    @Override
                    public void onMessage(final Transcript transcript, String lowData) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.add(transcript);
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorModel errorModel) {
                        ExLog.i("ZerothResult","fail...=" + GsonManager.toJson(errorModel));
                    }

                });

                mZerothMic.setWebSocketListener(new OnWebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {

                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {

                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {

                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {

                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {

                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {

                    }
                });
            }

            @Override
            public void onFailed(ErrorModel error) {

            }
        });
    }

    public class TranscriptAdapter extends BaseAdapter {

        public ArrayList<Transcript> items;
        private int focusIndex = 0;

        public TranscriptAdapter() {
            init();
        }

        public void init() {
            this.items = new ArrayList<>();
            this.items.add(new Transcript());
            focusIndex = 0;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Transcript getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void add(Transcript item) {
            if(item.finalText) {
                //true 일 경우 개행하여 새로운 문장 생성
                if(items.size() <= focusIndex) {
                    items.add(item);
                } else {
                    items.set(focusIndex++,item);
                }

            } else {
                //false 일 경우 현재 위치에 문장 업데이트
                if(items.size() <= focusIndex) {
                    items.add(item);
                } else {
                    items.set(focusIndex, item);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.item_transcript, viewGroup, false);
                view.setTag(R.id.lblText, view.findViewById(R.id.lblText));
            }

            Transcript transcript = getItem(i);
            if(transcript != null && transcript.transcript != null) {
                TextView textView = (TextView) view.getTag(R.id.lblText);
                textView.setText(transcript.transcript);
            }

            return view;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menuConnect);
        if(menuItem != null) {
            menuItem.setTitle(mStatus == ZerothDefine.ZerothStatus.RUNNING ? "DISCONNECT" : "CONNECT");
        }
        menuItem = menu.findItem(R.id.menuStatus);
        if(menuItem != null) {
            menuItem.setTitle(mStatus.name());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apply, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menuConnect) {
            if(mZerothMic != null) {
                if (mStatus == ZerothDefine.ZerothStatus.RUNNING) {
                    mZerothMic.stopListener();
                    mZerothMic.shutdown();
                } else {
                    mZerothMic.startListener();
                }
            } else {
                showToast("please first call to startZeroth() or Zeroth.createZerothMic() method");
            }
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ZerothDefine.REQUEST_PERMISSIONS_RECORD_AUDIO) {

        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
