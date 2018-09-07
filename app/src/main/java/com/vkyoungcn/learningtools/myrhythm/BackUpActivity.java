package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class BackUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_main);

    }


    public void fileSelectBkLocal(View view){

    }

    public void fileSelectBkRemote(View view){
        Intent intent = new Intent(this,SelectAndProcessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivity(intent);//下页含有一个fg(两种：本地、网络选择)选好后直接在该act中对本地DB进行操作，
        // 然后显示结果，然后回到Main页。
    }

}
