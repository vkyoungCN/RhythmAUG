package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BassModelEditActivity extends AppCompatActivity {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* */

    BaseModel model;
    MyRhythmDbHelper rhythmDbHelper = MyRhythmDbHelper.getInstance(this);
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* 七项通用控件*/
    TextView tv_id;
    EditText edt_title;
    Spinner spinner;
    EditText edt_descriptions;

    CheckBox ckb_selfDesign;
    CheckBox ckb_keepTop;

    EditText edt_LyricString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            model = savedInstanceState.getParcelable("MODEL");
        }else {
            model = getIntent().getParcelableExtra("MODEL");
        }

        //需要先调用SUPER（也就是本类）然后执行各子类的实现，ui数据的初始在子类实现中进行

//        initUiData();

    }




    public void confirmAndBack(View view){
        //这个是确认并返回

        //先把各项修改存入
        saveIntoModel();//通用

        updateModel();//子类具体实现
        //        rhythmDbHelper.updateLyricById(model);
        backToDetail();//子类具体实现
//        Intent intentBack = new Intent(this,LyricDetailActivity.class);
//        intentBack.putExtra("LYRIC", model);
//        intentBack.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

//        startActivity(intentBack,REQUEST_CODE_LY_EDIT);
    }

    void updateModel(){

    }

    void backToDetail(){
        
    }


    void saveIntoModel(){
        model.setTitle(edt_title.getText().toString());
        model.setCodeSerialString(edt_LyricString.getText().toString());
        model.setDescription(edt_descriptions.getText().toString());
        model.setKeepTop(ckb_keepTop.isChecked());
        model.setSelfDesign(ckb_selfDesign.isChecked());
        model.setStars(spinner.getSelectedItemPosition()+1);
        model.setLastModifyTime(System.currentTimeMillis());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("MODEL", model);

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
