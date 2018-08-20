package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_OVERALL_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class RhythmDetailActivity extends AppCompatActivity {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* */

    CompoundRhythm compoundRhythm = new CompoundRhythm();

    /* 多线程*/
//    private Handler handler = new RhythmDetailActivityHandler(this);//涉及弱引用，通过其发送消息。
    public static final int MESSAGE_PRE_DB_FETCHED = 5021;

//    ArrayList<PitchSequence> pitchSequences = new ArrayList<>();
//    ArrayList<Lyric> lyrics = new ArrayList<>();


    private TextView tv_id;
    private TextView tv_title;
    private TextView tv_lastModifyTime;
    private TextView tv_stars;
    private TextView tv_descriptions;
    private EditText edt_descriptions;
//    private TextView tv_mask_1;
//    private TextView tv_mask_2;

    private CheckBox ckb_selfDesign;
    private CheckBox ckb_keepTop;

//    private RecyclerView mRv_pitches;
//    private RecyclerView mRv_lyrics;
    private RhythmView rhythmView;

//    private PitchesRvAdapter adapter_p;
//    private LyricsRvAdapter adapter_v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_rhythm);

        if(savedInstanceState!=null){
            compoundRhythm = savedInstanceState.getParcelable("COMPOUND_RHYTHM");
        }else {
            compoundRhythm = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
        }

        tv_id = findViewById(R.id.tv_rhId_RDA);
        tv_title = findViewById(R.id.tv_title_RDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_RDA);
        tv_stars = findViewById(R.id.tv_starts_RDA);
        tv_descriptions = findViewById(R.id.tv_description_RDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_RDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_RDA);

//        mRv_pitches = findViewById(R.id.rv_linkingPitches_RDA);
//        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_RDA);
        rhythmView = findViewById(R.id.rhView_singleLine_RDA);


        initUiData();


    }



    public void goEditRhythm(View view){
        //需要跳转到专用的页面进行修改【注意，这个是直接跳到对节奏编码修改的页面】
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM",compoundRhythm);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }

    public void toEditActivity(View view){
        //这个是跳到全修改页面（但是其中编码仍是展示而非修改，因为编码的修改必须要开启专用页面）

        Intent intentToOverallEditor = new Intent(this,RhythmOverallEditActivity.class);
        intentToOverallEditor.putExtra("COMPOUND_RHYTHM",compoundRhythm);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_RH_OVERALL_EDIT);


    }

  /*  public void editDescription(View view){
        if(edt_descriptions.getVisibility()==View.GONE){
            //尽在edt尚未打卡时起作用
            tv_descriptions.setVisibility(View.GONE);
            edt_descriptions.setVisibility(View.VISIBLE);
            edt_descriptions.setText(compoundRhythm.getDescription());
        }

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        switch (resultCode){
            case RESULT_CODE_RH_OVERALL_EDIT_DONE:
                //这个数据是传递回来的，因为本页在进入伊始就没有涉及DB
                compoundRhythm = data.getParcelableExtra("COMPOUND_RHYTHM");
                initUiData();//重新设置UI数据。

                case RESULT_CODE_RH_PURE_EDIT_DONE:
                rhythmView.setRhythmViewData(compoundRhythm);//只重新设置rhv的，重新设置后会随即刷新。

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("COMPOUND_RHYTHM",compoundRhythm);

    }


    private void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), compoundRhythm.getId()));
        tv_title.setText(compoundRhythm.getTitle());
        tv_lastModifyTime.setText(compoundRhythm.getLastModifyTimeStr());

        tv_descriptions.setText(compoundRhythm.getDescription());
        tv_stars.setText(String.format(getResources().getString(R.string.plh_stars_num),compoundRhythm.getStars()));
        ckb_selfDesign.setChecked(compoundRhythm.isSelfDesign());
        ckb_keepTop.setChecked(compoundRhythm.isKeepTop());

        rhythmView.setRhythmViewData(compoundRhythm,22,24,24);//比默认的尺寸（18/20/20）稍大
    }

}
