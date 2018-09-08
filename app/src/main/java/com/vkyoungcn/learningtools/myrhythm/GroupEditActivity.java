package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class GroupEditActivity extends BaseModelEditActivity {
    private static final String TAG = "GroupEditActivity";
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

//        spinner = findViewById(R.id.spinner_starts_GEA);
//        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_GEA);
//        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_GEA);

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
//        Log.i(TAG, "updateModel: model="+model.toString());
        rhythmDbHelper.updateGroupPure((Group) model);//存入DB（仅涉及group一张表，交叉信息不在此存储）

    }

    void initUiData(){
        //字段比基类要少！不能super调用。
//        Log.i(TAG, "initUiData: model="+model.toString());
//        Log.i(TAG, "initUiData: model.id="+model.getId());
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), model.getId()));
        edt_title.setText(model.getTitle());

//        Date lastMT = new Date(model.getLastModifyTime());
        edt_descriptions.setText(model.getDescription());
//        spinner.setSelection(model.getStars()-1);
//        ckb_selfDesign.setChecked(model.isSelfDesign());
//        ckb_keepTop.setChecked(model.isKeepTop());

//        edt_LyricString.setText(model.getCodeSerialString());
    }
    @Override
    void backToDetail() {
        Intent intentBack = new Intent();
        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_GP_EDIT_DONE,intentBack);
        this.finish();
    }

    void saveIntoModel(){
        model.setTitle(edt_title.getText().toString());
//        model.setCodeSerialString(edt_LyricString.getText().toString());
        model.setDescription(edt_descriptions.getText().toString());
//        model.setKeepTop(ckb_keepTop.isChecked());
//        model.setSelfDesign(ckb_selfDesign.isChecked());
//        model.setStars(spinner.getSelectedItemPosition()+1);
        model.setLastModifyTime(System.currentTimeMillis());
    }
}
