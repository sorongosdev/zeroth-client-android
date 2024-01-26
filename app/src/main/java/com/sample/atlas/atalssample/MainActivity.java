package com.sample.atlas.atalssample;

import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.atlas.zerothandroid.ExLog;
import com.atlas.zerothandroid.OnWebSocketListener;
import com.atlas.zerothandroid.OnZerothResult;
import com.atlas.zerothandroid.VisualizerView;
import com.atlas.zerothandroid.Zeroth;
import com.atlas.zerothandroid.ZerothDefine;
import com.atlas.zerothandroid.ZerothMic;
import com.atlas.zerothandroid.ZerothParam;
import com.atlas.zerothandroid.network.ErrorModel;
import com.atlas.zerothandroid.network.GsonManager;
import com.atlas.zerothandroid.network.Transcript;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;


public class MainActivity extends AppCompatActivity {

    Toolbar myToolbar;
    public static InputMethodManager imm;
    ConstraintLayout mainlayout;

    VisualizerView mVisualizerView2;
    TextView explainText;

    ImageButton micbtn;

    public ZerothMic mZerothMic;
    public ZerothParam mParam;
    public ListView listview;
    public TranscriptAdapter mAdapter;
    public ZerothDefine.ZerothStatus mStatus = ZerothDefine.ZerothStatus.IDLE;

    //앱 실행시 텍스트 사이즈 비율(퍼센트)
    public int textSizeper = 100;
    public Button addTextSizeBt;
    public Button minusTextSizeBt;

    //텍스트 사이즈 조절 사이즈(퍼센트, 사이즈)
    //퍼센트 -> 사용자 입장에서 보이는 비율, 사이즈 -> 개발자가 퍼센트에 맞게 조절하는 실제 크기(sp단위)
    //사용자 입장에서는 퍼센트로 보여주고 개발자는 사이즈를 이용하기 때문에 퍼센트와 사이즈를 매핑
    //사이즈 조절 과정에서 순서대로 현재 사이즈에 대한 검사가 들어가기 때문에 HashMap이 아닌 LinkedHashMap 사용
    public LinkedHashMap<Integer, Float> sizeMap
            = new LinkedHashMap<Integer, Float>(){
            {
                put(75, (float) 13);
                put(100, (float) 16);
                put(150, (float) 19);
                put(200, (float) 22);
            }
        };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //가상 키보드 컨트롤을 위한 객체
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mainlayout = (ConstraintLayout) findViewById(R.id.mainlayout);

        //메뉴를 위한 툴바 세팅
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        setvisualizer();
        Zeroth.initialize(getApplicationContext());
        mAdapter = new TranscriptAdapter();
        listview = findViewById(R.id.listview);
        listview.setAdapter(mAdapter);
        explainText = findViewById(R.id.explainText);
        explainText.setClickable(false);

        ParamSetting();
        micbtn = findViewById(R.id.micbtn);
        setMic(mParam);

        // 앱 가운데 마이크 버튼을 실행했을 때를 위한 클릭 리스너
        micbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mZerothMic != null) {
                    if (mStatus == ZerothDefine.ZerothStatus.RUNNING) {
                        //음성 녹음이 실행중일 때
                        //하단 mclose함수에 설명
                        mclose();
                    } else {
                        //음성 녹음이 실행중이지 않을 때
                        //하단 mopen 함수에 설명
                        mopen();
                    }
                } else {
                    showToast("please first call to setMic() or Zeroth.createZerothMic() method");
                }
            }
        });

        addTextSizeBt = (Button)findViewById(R.id.addTextSizeBt);
        minusTextSizeBt = (Button)findViewById(R.id.minusTextSizeBt);
        //오른쪽 하단의 '크게' 버튼을 클릭했을 때를 위한 리스너
        addTextSizeBt.setOnClickListener(new View.OnClickListener() {
            /*
                    LinkedHashMap으로 미리 정의된 비율에 의해 텍스트의 크기를 키움

                    edittext에서 수정중인 내용은 저장이 안되어있는 상태이기 때문에
                    크기를 변환하면 수정중인 내용이 사라지고 기존의 내용이 적용되어 텍스트의 크기가 변경됨.
                    이를 방지하기 위해 edittext에 focus가 있을 때(현재 텍스트를 수정중인 상황일 때)는
                    '엔터' 입력을 발생시켜 저장
                 */
            @Override
            public void onClick(View view) {
                //현재 focus를 가져옴
                final View cView = getCurrentFocus();
                //현재 focus가 edittext라면 실행
                if (cView instanceof EditText) {
                    new Thread(new Runnable() {
                        public void run() {
                            //'엔터' 입력을 발생.(mEditorLis 클래스의 리스너가 반응해 변경중인 텍스트를 적용)
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                        }
                    }).start();
                }
                boolean x = false;
                //LinkedHashMap에 정의된 크기에 의해 최대크기가 아니라면 사이즈를 키움.(Toast메세지를 통해 현재 사이즈를 알림)
                //최대크기라면 최대크기임을 Toast 메세지를 통해 알림
                for(int key : sizeMap.keySet()){
                    if(x){
                        textSizeper = key;
                        mAdapter.applydatasetChanged();
                        Toast.makeText(getApplicationContext(),textSizeper+"%",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(key == textSizeper && key != 200){
                        x = true;
                    }
                }
                if(!x) Toast.makeText(getApplicationContext(),"최대 크기입니다.",Toast.LENGTH_SHORT).show();
            }
        });
        //오른쪽 하단의 '작게' 버튼을 클릭했을 때를 위한 리스너
        minusTextSizeBt.setOnClickListener(new View.OnClickListener() {
            /*
                    위의 addTextBt 리스너와 같은 구조로, 텍스트의 크기를 축소하는 기능
            */
            @Override
            public void onClick(View view) {
                final View cView = getCurrentFocus();
                if (cView instanceof EditText) {
                    new Thread(new Runnable() {
                        public void run() {
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                        }
                    }).start();
                }
                    boolean x = false;
                    int pre = 75;
                    for (int key : sizeMap.keySet()) {
                        if (key == textSizeper && key != 75) {
                            x = true;
                            break;
                        }
                        pre = key;
                    }
                    if (x) {
                        textSizeper = pre;
                        mAdapter.applydatasetChanged();
                        Toast.makeText(getApplicationContext(), textSizeper + "%", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "최저 크기입니다.", Toast.LENGTH_SHORT).show();
                }
        });
        mainlayout.setOnClickListener(new View.OnClickListener() {
            /*
            메인 화면의 가장 밑부분인 Constraintlayout의 클릭 리스너
            사용자가 완료버튼을 누르지 않고 빈 곳을 눌렀을 경우을 대비하여 만듦.

            현재 focus가 edittext에 있어 가상 키보드가 올라와있다면 '엔터' 입력을 보내 저장 후 키보드를 내림

            mainlayout인 constraintlayout 위에 clickable한 view 객체가 있다면 적용되지 않음
            키보드가 올라와 있는 상태에서 상단의 설명 텍스트가 대부분이기 때문에 설명 텍스트인 explaintext의 clickable 속성을
            false로 변경하여 뒤에있는 mainlayout이 클릭되어 리스너가 반응하도록 함
            */
            @Override
            public void onClick(View view) {
                final View cView = getCurrentFocus();
                if (cView instanceof EditText) {
                    new Thread(new Runnable() {
                        public void run() {
                            //'엔터'입력 발생
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                        }
                    }).start();
                }
            }
        });

        //앱에 필요한 권한 요청
        Zeroth.requestActivityPermission(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
            오른쪽 상단 메뉴버튼을 생성
         */
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
            오른쪽 상단 메뉴 버튼을 눌렀을 때의 행동을 설정
            메뉴목록(id값)은 app 프로젝트의 res/menu/menu.xml에서 수정,삭제,생성
         */
        switch (item.getItemId()) {
            case R.id.save:
                /*
                '저장' 버튼을 누르면 현재 화면하단의 text들을 "yyyyMMdd_HHmmss" 형식의 현재 시간을 제목으로 저장

                listview의 item별로 jsonObject에 저장 후 String으로 변환하여 "yyyyMMdd_HHmmss" 형식의 제목으로 저장
                저장경로 얻기 -> File Context.getFilesDir() (data/data/appname/files)
                 */
                JSONObject jsonObject = new JSONObject();
                String title_time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                try {
                    ArrayList<Transcript> items = mAdapter.getItems();
                    for(int i=0;i<items.size();i++){
                        jsonObject.put(Integer.toString(i),mAdapter.getItem(i).transcript);
                    }
                    FileOutputStream os = openFileOutput(title_time,MODE_PRIVATE);
                    os.write(jsonObject.toString().getBytes());
                    showToast("저장되었습니다.");
                    os.close();
                }catch (JSONException | IOException e){
                    e.printStackTrace();
                }
                return  true;

            case R.id.restart:
                /*
                하단의 listview를 초기화하여 현재 화면의 text들을 삭제
                 */
                //listview adapter를 초기화(adapter의 item리스트를 다시 생성)
                mAdapter.init();
                //변경 사항을 적용
                mAdapter.notifyDataSetChanged();
                return true;

            case R.id.load:
                /*
                앱 내부 저장소에 있는 파일들의 목록을 불러와서 파일의 제목 리스트를 dialog에 띄워줌

                항목을 터치하면 해당파일의 String들을 JsonObject로 변환 후 listview에 적용(mAdapter이용)
                StringBuffer를 이용해 파일의 내용을 읽어들인다.

                '닫기'를 누른다면 dialog만 없어짐
                 */
                final String list[] = fileList();
                Arrays.sort(list);
                new AlertDialog.Builder(this).setTitle("선택").setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            FileInputStream is = openFileInput(list[which]);
                            StringBuffer data = new StringBuffer();
                            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
                            String str = buffer.readLine(); // 파일에서 한줄을 읽어옴
                            while (str != null) {
                                data.append(str + "\n");
                                str = buffer.readLine();
                            }
                            buffer.close();
                            JSONObject jso = new JSONObject(String.valueOf(data));
                            mAdapter.init();
                            for(int i = 0; i < jso.length();i++){
                                Transcript item = new Transcript();
                                item.finalText = false;
                                item.transcript = jso.getString(Integer.toString(i));
                                mAdapter.add(item);
                                mAdapter.setnextlist();
                            }
                            mAdapter.notifyDataSetChanged();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNeutralButton("닫기", null).show();
                return true;

            case R.id.send:
                /*
                상단 메뉴의 '공유' 버튼을 누르면 현재 화면의 text들로 공유할 String을 생성
                String을 생성할 때 item별로 구분을 주기 위해 아이템들 사이에 '\n'을 넣는다.
                shareSheet를 실행하여 공유할 앱을 결정
                 */
                int arrSize = mAdapter.getCount();
                String sendtext = "";
                //현재 화면의 text들로 공유할 String 생성
                for(int i = 0; i < arrSize; i++){
                    sendtext +=  "\n\n" + mAdapter.getItem(i).transcript;
                }
                //공유할 앱을 선택할 shareSheet 생성
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, sendtext.substring(2));
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void mclose(){
        Log.e("mclose","sdfsdf");
        /*
        녹음중 마이크 버튼을 터치했을 때 실행
        하나의 녹음이 끝났음으로 리스트뷰의 항목을 다음으로 넘겨 녹음마다 다른 아이템에 저장
        음성 visualizer를 안보이게 함
        ZerothMic를 종료함(마이크, 소켓서버가 종료됨)
        설명 텍스트를 변경
         */
        mAdapter.setnextlist();
        mVisualizerView2.setVisibility(View.GONE);
        mVisualizerView2.receive(0);
        mZerothMic.stopListener();
        mZerothMic.shutdown();
        //마이크,소캣 다시 세팅
        setMic(mParam);
        explainText.setText(R.string.explain1);
    }

    public void mopen(){
        Log.e("mopen","SDfsdfsdff");
        /*
        녹음중이 아닐 때 마이크 버튼을 터치했을 때 실행
        음성 visualizer를 보이게 함
        ZerothMic를 시작(마이크, 소켓서버가 시작됨)
        설명 텍스트를 변경
         */
        explainText.setText(R.string.explain2);
        mZerothMic.startListener();
        mVisualizerView2.setVisibility(View.VISIBLE);
    }


    public void ParamSetting(){
        /*
        마이크 설정에 필요한 옵션들을 미리 지정
        ZerothDefine에 옵션들이 미리 정의되어 있다.
         */
            mParam = new ZerothParam();
            mParam.language = ZerothDefine.ZEROTH_LANG_KOR;
            mParam.channels = ZerothDefine.ZEROTH_MONO;
            mParam.audioRate = ZerothDefine.ZEROTH_RATE_16;
            mParam.isFinal = false;
    }


    public void setvisualizer() {
        /*
        녹음시 화면 중앙에 나오는 visualizer를 생성, 세팅하고 보이지 않는 상태로 만듦.
         */
        mVisualizerView2 = (VisualizerView) findViewById(R.id.visual);
        ViewTreeObserver observer = mVisualizerView2.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //위치를 지정
                mVisualizerView2.setBaseY(mVisualizerView2.getHeight() / 5);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mVisualizerView2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mVisualizerView2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        mVisualizerView2.setVisibility(View.GONE);
    }


    /*
    세팅해놓은 param의 옵션들(언어,rate,channel)로 ZerothMic객체를 생성

     */
    public void setMic(final ZerothParam param){
        /*
            웹소켓 통신으로 받아온 결과를 관리하여 적용하는 onZerothResult 객체를 인자로 넘겨준다.
            visualizer 객체를 넘겨준다
            상단의 설명 텍스트를 객체로 넘겨준다.
            */
        mZerothMic = Zeroth.createZerothMic(param,
                //onZerothResult 인터페이스의 구현
                new OnZerothResult() {
            @Override
            public void doMopen(){
                //시작은 마이크버튼 클릭으로만 하기 때문에 비워놓음
            }
            @Override
            public void doMclose(){
                //마이크 클릭 뿐 아니라 침묵 감지 등 사용자의 직접적인 조작 없이 녹음을 끝낼 때 이용
                mclose();
            }

            @Override
            public void onProgressStatus(ZerothDefine.ZerothStatus status) {
                //현재 동작 상태를 변경 -> 마이크버튼 클릭 리스너 등 동작 상태로 행동을 결정하는 객체들에게 영향
                mStatus = status;
            }

            @Override
            public void onMessage(final Transcript transcript, String lowData) {
                /*
                ZerothMicImpl 객체에 implement 되어있는 웹소켓 onMessage리스너에서 호출한다.
                받아온 메세지를 하단의 customadapter로 화면 하단의 listview에 적용
                 */
                runOnUiThread(new Runnable() {//Ui를 변경하는 작업이기 때문에 UIThread사용
                    @Override
                    public void run() {
                        mAdapter.add(transcript);
                    }
                });
            }

            @Override
            public void onFailed(ErrorModel errorModel) {
                //실패했을 때 에러 메세지 발생
                ExLog.i("ZerothResult","fail...=" + GsonManager.toJson(errorModel));
            }

        },mVisualizerView2,(TextView)findViewById(R.id.explainText));

        mZerothMic.setWebSocketListener(new OnWebSocketListener() {
            //mZerothMic 객체의 소켓 리스너에 빈 리스너 객체를 넣어둠
            //실질적인 기능을 하는 리스너는 Zeroth 클래스 안의 ZerothMicImpl클래스에 있음
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

    public class TranscriptAdapter extends BaseAdapter {
        /*
        * 안드로이드 리스트뷰 사용을 위한 custom adapter
        * */

        public ArrayList<Transcript> items;
        private int focusIndex = 0;

        //서버에서 문장의 마지막이라는 판단이 왔을 때 true로 바뀌며
        // 마지막 인것을 기억해놨다가 다음 문장이 들어오면 줄바꿈을 하고 false로 바꾸어줌
        boolean preFinal = false;

        public TranscriptAdapter() {
            init();
        }

        public void init() {
            this.items = new ArrayList<>();
            Transcript first = new Transcript();
            first.transcript = "";
            first.finalText = false;
            this.items.add(first);
            focusIndex = 0;
        }

        //아이템 리스트를 반환
        public ArrayList getItems(){
            return items;
        }

        //아이템을 원하는 인덱스에 세팅하고 변경사항을 감지하여 UI 적용
        public void setItem(Transcript transcript,int index){
            items.set(index,transcript);
        }


        @Override
        //아이템들의 갯수를 반환
        public int getCount() {
            return items.size();
        }

        @Override
        //특정 인덱스의 아이템 1개를 반환
        public Transcript getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        //줄바꿈이 아닌 listview의 항목 자체를 다음으로 넘길때 호출하는 함수
        //focusindex를 더하면 하단의 add 함수에서 focus와 아이템의 갯수를 비교하여 다음 항목으로 넘길 것인지를 결정
        public void setnextlist(){
            focusIndex++;
        }

        public void add(Transcript item) {

            //true 일 경우 이전 문장이 끝났다는 의미로 개행하여 새로운 문장 생성
            if(preFinal) {
                //focusindex가 현재 아이템 개수와 같다면 다음 목록으로 이동함
                //아이템의 내용을 더하지 않고 다음 내용으로 넘어감
                if(items.size() <= focusIndex) {
                    items.add(item);
                }
                //focusindex가 아이템의 갯수보다 작다면 현재 목록에 글을 추가하는 것으로 판단
                else {
                    item.transcript = getItem(focusIndex).transcript +"\n"+ item.transcript;
                    items.set(focusIndex,item);
                }

            } else {
                //위와 동일하나 개행하지 않는다
                if(items.size() <= focusIndex) {
                    items.add(item);
                } else {
                    item.transcript = getItem(focusIndex).transcript + item.transcript;
                    items.set(focusIndex, item);
                }
            }
            //현재 들어온 text가 다음에 줄바꿈을 요구하는지 저장
            preFinal = item.finalText;
            //변경감지, 적용
            notifyDataSetChanged();
        }

        //외부에서 아이템 리스트를 바꾸었을 경우해 외부에서 변경을 감지하고 적용하기 위해 호출
        public void applydatasetChanged(){
            notifyDataSetChanged();
        }

        /*
        view(목록 하나의 객체)가 생성되지 않은 상태라면 getLayoutInflater().inflate(R.layout.item_transcript, viewGroup, false)로 생성
        view에 태그를 달아서 태그 안에 객체를 가지고 다니며 외부에서 view로 접근하여 태그로 객체를 불러올 수 있음
        '엔터','Done' 입력을 위한 listener를 연결해줌. (listener는 mEdittextLis 클래스)
        택스트의 사이즈를 현재 설정한 사이즈로 설정
        텍스트의 내용을 현재 아이템Array의 내용으로 적용
         */
        @Override
        //태그 설정 및 리스너 연결
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.item_transcript, viewGroup, false);
                view.setTag(R.id.EditTextListview, view.findViewById(R.id.EditTextListview));
                EditText et = (EditText) view.getTag(R.id.EditTextListview);
                et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                mEdittextLis l = new mEdittextLis(i,mAdapter,mainlayout,getApplicationContext(),et);
                et.setOnKeyListener(l);
                et.setOnEditorActionListener(l);
                et.addTextChangedListener(l);
            }


            //내용 적용 및 사이즈 적용
            Transcript transcript = getItem(i);
            if(transcript != null && transcript.transcript != null) {
                EditText editText = (EditText) view.getTag(R.id.EditTextListview);
                editText.setTextSize(sizeMap.get(textSizeper));
                editText.setText(transcript.transcript);
            }

            return view;
        }
    }


    /*오디오 녹음 권한 요청 함수*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ZerothDefine.REQUEST_PERMISSIONS_RECORD_AUDIO) {

        }
    }

    //Toast메세지를 좀 더 편하게 만들기 위한 함수
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}


