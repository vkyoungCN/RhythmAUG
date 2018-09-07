package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;

public class LyricDetailActivity extends BaseModelDetailActivity {

    //在lyric下的特殊空间（展示词序列）
    private TextView tv_LyricString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUiData();
    }


    public void toEditActivity(View view){
        Intent intentToOverallEditor = new Intent(this,LyricEditActivity.class);
        intentToOverallEditor.putExtra("MODEL", model);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_LY_EDIT);
    }

    //返回接收方法使用基类的



    @Override
    void loadComponents(){
        setContentView(R.layout.activity_detail_lyric);

        //子类负责控件的加载
        tv_id = findViewById(R.id.tv_rhId_LDA);
        tv_title = findViewById(R.id.tv_title_LDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_LDA);
        tv_stars = findViewById(R.id.tv_starts_LDA);
        tv_descriptions = findViewById(R.id.tv_description_LDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_LDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_LDA);

        //包括特殊控件
        tv_LyricString = findViewById(R.id.tv_lyricString_LDA);

    }


    void initUiData(){
        super.initUiData();

        //只负责特殊控件
        tv_LyricString.setText(model.getCodeSerialString());
    }

}
