package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineView;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LYPH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_OVERALL_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class RhythmDetailActivity extends BaseModelDetailActivity {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* */
private static final String TAG = "RhythmDetailActivity";
    //rhythm下的特别控件
    private RhythmSingleLineView rhythmView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initUiData();
    }



    public void goEditRhythm(View view){
        //节奏特有方法（在点击rh特别控件右下角图标时触发，只修改rh编码）
        //需要跳转到专用的页面进行修改【注意，这个是直接跳到对节奏编码修改的页面】
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }

    public void goEditLyricA(View view){
        Intent intentToLyEditor = new Intent(this,LyricPhrasesEditActivity.class);
        intentToLyEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToLyEditor.putExtra("IS_PRIMARY",true);
        intentToLyEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToLyEditor,REQUEST_CODE_LYPH_EDIT);
    }

    public void goEditLyricB(View view){
        Intent intentToLyEditor = new Intent(this,LyricPhrasesEditActivity.class);
        intentToLyEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToLyEditor.putExtra("IS_PRIMARY",false);
        intentToLyEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToLyEditor,REQUEST_CODE_LYPH_EDIT);
    }
    @Override
    public void toEditActivity(View view){
        //这个是跳到全修改页面（但是其中编码仍是展示而非修改，因为编码的修改必须要开启专用页面）

        Intent intentToOverallEditor = new Intent(this,RhythmOverallEditActivity.class);
        intentToOverallEditor.putExtra("MODEL", model);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_RH_OVERALL_EDIT);


    }

/*
    @Override
    void loadData(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            model = savedInstanceState.getParcelable("MODEL");
        }else {
//            Log.i(TAG, "onCreate: from Intent");
            model = getIntent().getParcelableExtra("MODEL");
        }
    }*/

    @Override
    void loadComponents(){
        setContentView(R.layout.activity_detail_rhythm);

        /* 加载各控件（基类共用）*/
        tv_id = findViewById(R.id.tv_rhId_RDA);
        tv_title = findViewById(R.id.tv_title_RDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_RDA);
        tv_stars = findViewById(R.id.tv_starts_RDA);
        tv_descriptions = findViewById(R.id.tv_description_RDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_RDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_RDA);

        //加载本类特有控件
        rhythmView = findViewById(R.id.rhView_singleLine_RDA);

    }


    @Override
    void initUiData(){
        super.initUiData();
//        Log.i(TAG, "initUiData: model="+model.toString());
        rhythmView.setRhythmViewData((RhythmBasedCompound) model);//比默认的尺寸（18/20/20）稍大
        Log.i(TAG, "initUiData: rh.cs="+((RhythmBasedCompound) model).getCodeSerialByte().toString());
    }

}
