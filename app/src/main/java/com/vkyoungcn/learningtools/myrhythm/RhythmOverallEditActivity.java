package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_CANCEL;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;

public class RhythmOverallEditActivity extends BaseModelEditActivity {

    /* 特有控件*/
    private RhythmView rhythmView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_overall_rhythm);

        //加载
        tv_id = findViewById(R.id.tv_rhId_ROEA);
        edt_title = findViewById(R.id.edt_title_ROEA);

        spinner = findViewById(R.id.spinner_ROEA);
        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_ROEA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_ROEA);

        edt_descriptions = findViewById(R.id.edt_description_ROEA);

        //特有控件
        rhythmView = findViewById(R.id.rhView_singleLine_ROEA);

        initUiData();
    }



    //修改节奏编码
    public void goEditRhythm(View view){
        //需要跳转到专用的页面进行修改
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }



    /* 以下一个方法只可能是从rh的编码专用修改页返回
    * 是节奏编辑页独有方法，其他资源不需要*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        if(resultCode == RESULT_CODE_RH_PURE_EDIT_DONE){
            model = data.getParcelableExtra("MODEL");
//            由于从新的activity返回，需要对所有控件再次加载
            initUiData();
        }
    }



    //数据的setCodeSerialString在纯rh编码修改时已经做出改变并保存到rh类中【待确定实现】，
    // 不需再次处理。不必覆写saveIntoModel。


    @Override
    void updateModel() {
        rhythmDbHelper.updateRhythm((Rhythm) model);
//【如果未做出实质修改，如何避免向DB写入数据?】
    }

    @Override
    void backToDetail() {
        Intent intentBack = new Intent();
        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_RH_OVERALL_EDIT_DONE,intentBack);
        this.finish();
    }


    public void cancel(View view){
        setResult(RESULT_CODE_RH_OVERALL_EDIT_CANCEL);
        this.finish();

    }



    @Override
    void initUiData() {
        super.initUiData();

        rhythmView.setRhythmViewData((RhythmBasedCompound) model,22,24,24);//比默认的尺寸（18/20/20）稍大

    }
}
