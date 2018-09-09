package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_CREATE_FREE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_CREATED;

public class LyricCreateEntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_main);

    }


    public void lyricAddFree(View view){
        //传统新增页
        Intent intent = new Intent(this,LyricCreateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("MODEL",new Lyric());
        this.startActivityForResult(intent,REQUEST_CODE_LY_CREATE_FREE);
    }

    public void lyricAddHardLink(View view){
        //①选定旋律，
        // ②然后进入创作页
    }

    public void lyricAddWeakLink(View view){
        //①选定参考旋律，
        // ②然后进入创作页
        //·创作过程中，参考旋律可更换。（没有实质限制）
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case RESULT_CODE_LY_CREATED:
                //成功，随即返回调用方
                // （另一种情形是未成功，返回到本页后无后续动作）
                setResult(RESULT_CODE_LY_CREATED);
                this.finish();
                break;

        }


    }
}
