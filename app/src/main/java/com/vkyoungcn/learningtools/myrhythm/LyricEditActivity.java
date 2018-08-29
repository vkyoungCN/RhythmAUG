package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;

public class LyricEditActivity extends BassModelEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_lyric);

        initUiData();
    }


    @Override
    void updateModel() {
        rhythmDbHelper.updateLyricById((Lyric) model);
    }

    @Override
    void backToDetail() {
        Intent intentBack = new Intent(this,LyricDetailActivity.class);
        intentBack.putExtra("LYRIC", model);
        intentBack.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        setResult(REQUEST_CODE_LY_EDIT);
        this.startActivity(intentBack);

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
