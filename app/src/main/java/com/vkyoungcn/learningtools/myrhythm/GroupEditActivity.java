package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class GroupEditActivity extends BaseModelEditActivity {

/*
【注意group的字段与基类一致，没有“特有”控件】
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        tv_id = findViewById(R.id.tv_rhId_GEA);
        edt_title = findViewById(R.id.edt_title_GEA);
        edt_descriptions = findViewById(R.id.edt_description_GEA);

        spinner = findViewById(R.id.spinner_starts_GEA);
        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_GEA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_GEA);

        initUiData();
    }

    public void cancel(View view){
//        setResult(RESULT_CODE_RH_OVERALL_EDIT_CANCEL);
        this.finish();

    }

    public void confirmAndBack(View view){
        super.confirmAndBack(view);
    }

    @Override
    void updateModel() {
        rhythmDbHelper.updateGroupPure((Group) model);//存入DB（仅涉及group一张表，交叉信息不在此存储）

    }

    @Override
    void backToDetail() {
        Intent intentBack = new Intent();
        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_GP_EDIT_DONE,intentBack);
        this.finish();
    }
}
