package com.sample.atlas.atalssample;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

public class FirstPage extends AppCompatActivity {
    /*
    로딩 페이지 구현을 위한 클래스
    values/styles.xml 파일에 로딩화면의 스타일(화면 이미지)이 설정되어 있다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //로딩화면의 유지를 위해 2초간 정지한다
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //mainactivity를 실횅
        startActivity(new Intent(this,MainActivity.class));
        //현재 activity를 종료
        finish();
    }

}
