package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_CREATED;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_CANCEL;

public class LyricCreateActivity extends LyricEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_lyric);
        initUiData();
    }

    @Override
    public void confirmAndBack(View view) {
        super.confirmAndBack(view);
        //直接使用基类即可，会转而调用本类的分支方法实现
        // （由于布局文件中需要相应的方法，所以这里必须写一遍）
    }

    /* 由于布局中需要，所以即使基类有此方法也要重写一遍【是否有其他解决？】*/
    public void cancel(View view){
        this.finish();

    }


    @Override
    void updateModel() {
        rhythmDbHelper.createLyric((Lyric) model);
    }

    @Override
    void backToDetail() {
        Intent intentBack = new Intent();
        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_LY_CREATED,intentBack);
        this.finish();

    }

    void initUiData(){
      //新建模式下，传递来的是一个空model，手动设置初始的值。
        tv_id.setText("--");
        edt_title.setText("");

        edt_descriptions.setText("");
        spinner.setSelection(1);
        ckb_selfDesign.setChecked(true);
        ckb_keepTop.setChecked(false);

        edt_LyricString.setText("");
    }

}
