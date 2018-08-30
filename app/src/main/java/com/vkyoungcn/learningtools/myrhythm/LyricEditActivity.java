package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_CANCEL;

public class LyricEditActivity extends BaseModelEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_lyric);

        tv_id = findViewById(R.id.tv_rhId_LEA);
        edt_title = findViewById(R.id.edt_title_LEA);
        spinner = findViewById(R.id.spinner_starts_LEA);
        edt_descriptions = findViewById(R.id.edt_description_LEA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_LEA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_LEA);

        edt_LyricString = findViewById(R.id.edt_lyricString_LEA);

        initUiData();
    }

    @Override
    public void confirmAndBack(View view) {
        super.confirmAndBack(view);
        //直接使用基类即可，会转而调用本类的分支方法实现
        // （由于布局文件中需要相应的方法，所以这里必须写一遍）
    }

    public void cancel(View view){
//        setResult(RESULT_CODE_RH_OVERALL_EDIT_CANCEL);
        this.finish();

    }

    @Override
    void saveIntoModel() {
        super.saveIntoModel();

        //特有控件
        model.setCodeSerialString(edt_LyricString.getText().toString());

    }

    @Override
    void updateModel() {
        rhythmDbHelper.updateLyricById((Lyric) model);
    }

    @Override
    void backToDetail() {
        Intent intentBack = new Intent();
        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_LY_EDIT_DONE,intentBack);
        this.finish();

    }


    void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), model.getId()));
        edt_title.setText(model.getTitle());

        Date lastMT = new Date(model.getLastModifyTime());

        edt_descriptions.setText(model.getDescription());
        spinner.setSelection(model.getStars()-1);
        ckb_selfDesign.setChecked(model.isSelfDesign());
        ckb_keepTop.setChecked(model.isKeepTop());

        edt_LyricString.setText(model.getCodeSerialString());
    }
}
