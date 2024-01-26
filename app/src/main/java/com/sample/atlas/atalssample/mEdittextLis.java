package com.sample.atlas.atalssample;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.atlas.zerothandroid.network.Transcript;
import static com.sample.atlas.atalssample.MainActivity.imm;

public class mEdittextLis implements TextView.OnEditorActionListener, View.OnKeyListener, TextWatcher {
    /*
    하단 번역된 텍스트를 나타내는 리스트뷰의 리스너를 위한 클래스
    onEditorAction 리스너는 텍스트의 변경을 감지하여 실시간으로 적용시키고
    키보드의 Done 입력에 반응하여 가상 키보드를 내리고 커서를 제거
    //apply()함수에 대한 설명은 하단 있음
    키 입력에 반응하는 onKey 리스너가 있으며 엔터 입력에 반응하여 가상 키보드를 내리고 커서를 제거
     */

    int itemindex;
    MainActivity.TranscriptAdapter mAdapter;
    Context mContext;
    ConstraintLayout mainlayout;
    EditText et;

    mEdittextLis(int ItemIndex, MainActivity.TranscriptAdapter mAdapter,ConstraintLayout mainlayout,Context mContext,EditText etinstance){
        this.itemindex = ItemIndex;
        this.mAdapter = mAdapter;
        this.mContext = mContext;
        this.mainlayout = mainlayout;
        this.et = etinstance;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        //키보드의 엔터 입력을 감지
        //스마트폰의 가상 키보드의 완료 버튼을 누르지 않고
        //빈 공간을 터치하는 등 다른 방법을 이용해 저장할 때 엔터 입력을 보냄
        if(i == KeyEvent.KEYCODE_ENTER){
            removefocus();
            return true;
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        //스마트폰 가상 키보드의 완료 버튼을 감지
        if(actionId == EditorInfo.IME_ACTION_DONE)
        {
            removefocus();
            return true;
        }
        return  false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        /*
        사용하지 않음
        start 지점에서 시작되는 count 갯수만큼의 글자들이 after 길이만큼의 글자로 대치되려고 할 때 호출
         */
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        /*
        사용하지 않음
        start 지점에서 시작되는 count 갯수만큼의 글자들이 after 길이만큼의 글자로 대치되려고 할 때 호출
         */
    }

    @Override
    public void afterTextChanged(Editable editable) {
        /*
        화면 하단의 텍스트뷰의 변경을 감지하는 리스너
        변경이 감지되면 그때마다 mAdapter를 이용해 변경을 적용
         */
        apply();
    }

    void apply(){
        /*
        리스너 객체 생성시 인자로 넘겨받은 mAdapter(mainactivity 하단에 있는 커스텀 어댑터)를 이용하여 수정한 텍스트를 적용
        수정 이전의 상태로 되돌리고 edittext에서 focus를 저거하여 커서를 제거
        가상 키보드를 내림
         */
        Transcript t = new Transcript();
        t.transcript = ((TextView)et).getText().toString();
        t.finalText = false;
        mAdapter.setItem(t,itemindex);
    }
    void notapply(){
        /*
        현재 실질적으로 실행되지는 않음
        수정 이전의 상태로 되돌리고 edittext에서 focus를 저거하여 커서를 제거
        가상 키보드를 내림
         */
        mAdapter.notifyDataSetChanged();
        Toast.makeText(mContext,"수정 취소",Toast.LENGTH_SHORT).show();
        removefocus();
    }


    void removefocus(){
        /*
        포커스를 제거하기 전 변경사항을 감지해 적용
        인자로 넘겨받은 view 객체에서 focus를 제거함
        edittext에서 focus를 제거하면 커서가 없어지는 효과가 있음
         */
        mAdapter.notifyDataSetChanged();
        et.clearFocus();
        et.setFocusable(false);
        et.setFocusableInTouchMode(true);
        et.setFocusable(true);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}
