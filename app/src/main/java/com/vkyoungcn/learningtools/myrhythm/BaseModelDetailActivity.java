package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class BaseModelDetailActivity extends AppCompatActivity {
    private static final String TAG = "BaseModelDetailActivity";
    BaseModel model;

    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* 七个通用控件，一个特殊控件（子类实现）*/
    TextView tv_id;
    TextView tv_title;
    TextView tv_lastModifyTime;
    TextView tv_stars;
    TextView tv_descriptions;

    CheckBox ckb_selfDesign;
    CheckBox ckb_keepTop;

//    TextView tv_LyricString;；特殊控件


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 基类负责加载数据*/
        if(savedInstanceState!=null){
            model = savedInstanceState.getParcelable("MODEL");
        }else {
//            Log.i(TAG, "onCreate: from Intent");
            model = getIntent().getParcelableExtra("MODEL");
        }
//        Log.i(TAG, "onCreate: model="+model.toString());
//        loadData(savedInstanceState);

        /*子类负责显示（任务1，加载布局、控件）*/
        loadComponents();
        if(model==null){
//            Log.i(TAG, "onCreate: null");
            Toast.makeText(this, "传递的Model是空指针。", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        /* 子类的显示任务2*/
        initUiData();

    }



/* 此方法只有rhythm下有，对应于rhythm控件的独立修改
   public void goEditRhythm(View view){
        //需要跳转到专用的页面进行修改【注意，这个是直接跳到对节奏编码修改的页面】
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);


    }*/

    public void toEditActivity(View view){
        //跳到修改页面（子类实现）
/*
        Intent intentToOverallEditor = new Intent(this,LyricEditActivity.class);
        intentToOverallEditor.putExtra("LYRIC", model);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_LY_EDIT);*/
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示

        switch (resultCode){
            case RESULT_CODE_LY_EDIT_DONE:
            case RESULT_CODE_GP_EDIT_DONE://（本条实际没有意义，因为GDA实际是继承自ThreadRvActivity.）
            case RESULT_CODE_RH_OVERALL_EDIT_DONE:
            case RESULT_CODE_RH_PURE_EDIT_DONE://即使是仅仅编辑的rh编码，也需要全部ui重设（毕竟是从新的act返回。）
                //（问，可是这样难道不是相设置了两次嘛？）
                model = data.getParcelableExtra("MODEL");
                initUiData();//重新设置UI数据。
                break;



        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("MODEL", model);

    }


    /*void loadData(Bundle savedInstanceState){
        //子类实现
    }*/

    void loadComponents(){
        //子类实现
    }

    void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), model.getId()));
        tv_title.setText(model.getTitle());

        Date lastMT = new Date(model.getLastModifyTime());
        tv_lastModifyTime.setText(sdFormat.format(lastMT));

        tv_descriptions.setText(model.getDescription());
        tv_stars.setText(String.format(getResources().getString(R.string.plh_stars_num), model.getStars()));
        ckb_selfDesign.setChecked(model.isSelfDesign());
        ckb_keepTop.setChecked(model.isKeepTop());

        /* 特别控件在此*/
//        tv_LyricString.setText(model.getCodeSerialString());
    }

}
