package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LY_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_OVERALL_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LY_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class LyricDetailActivity extends AppCompatActivity {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* */

    Lyric lyric = new Lyric();

    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TextView tv_id;
    private TextView tv_title;
    private TextView tv_lastModifyTime;
    private TextView tv_stars;
    private TextView tv_descriptions;
//    private EditText edt_descriptions;

    private CheckBox ckb_selfDesign;
    private CheckBox ckb_keepTop;

    private TextView tv_LyricString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lyric);

        if(savedInstanceState!=null){
            lyric = savedInstanceState.getParcelable("COMPOUND_RHYTHM");
        }else {
            lyric = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
        }

        tv_id = findViewById(R.id.tv_rhId_LDA);
        tv_title = findViewById(R.id.tv_title_LDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_LDA);
        tv_stars = findViewById(R.id.tv_starts_LDA);
        tv_descriptions = findViewById(R.id.tv_description_LDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_LDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_LDA);
        tv_LyricString = findViewById(R.id.tv_lyricString_LDA);

        initUiData();

    }



    public void goEditRhythm(View view){
        //需要跳转到专用的页面进行修改【注意，这个是直接跳到对节奏编码修改的页面】
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", lyric);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }

    public void toEditActivity(View view){
        //这个是跳到全修改页面（但是其中编码仍是展示而非修改，因为编码的修改必须要开启专用页面）

        Intent intentToOverallEditor = new Intent(this,LyricEditActivity.class);
        intentToOverallEditor.putExtra("LYRIC", lyric);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_LY_EDIT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        switch (resultCode){
            case REQUEST_CODE_LY_EDIT:
                //这个数据是传递回来的，因为本页在进入伊始就没有涉及DB
                lyric = data.getParcelableExtra("LYRIC");
                initUiData();//重新设置UI数据。
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("COMPOUND_RHYTHM", lyric);

    }


    private void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), lyric.getId()));
        tv_title.setText(lyric.getTitle());

        Date lastMT = new Date(lyric.getLastModifyTime());
        tv_lastModifyTime.setText(sdFormat.format(lastMT));

        tv_descriptions.setText(lyric.getDescription());
        tv_stars.setText(String.format(getResources().getString(R.string.plh_stars_num), lyric.getStars()));
        ckb_selfDesign.setChecked(lyric.isSelfDesign());
        ckb_keepTop.setChecked(lyric.isKeepTop());

        tv_LyricString.setText(lyric.getCodeSerialString());
    }

}
