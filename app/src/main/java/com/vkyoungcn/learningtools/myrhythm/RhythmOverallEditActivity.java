package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompounds;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_CANCEL;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;

public class RhythmOverallEditActivity extends AppCompatActivity {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* 【后期考虑增加分组（节奏和旋律是一起的，词也是主要以挂载在节奏下啊的方式存在），然后有关联的节奏、旋律等可以分在一个组内管理】
* */
    RhythmBasedCompounds rhythmBasedCompounds = new RhythmBasedCompounds();

    /* 多线程*/
//    private Handler handler = new RhythmDetailActivityHandler(this);//涉及弱引用，通过其发送消息。
    public static final int MESSAGE_PRE_DB_FETCHED = 5021;

//    ArrayList<PitchSequence> pitchSequences = new ArrayList<>();
//    ArrayList<Lyric> lyrics = new ArrayList<>();


    private TextView tv_id;
    private TextView edt_title;
    private Spinner spinner;
    private EditText edt_descriptions;
//    private TextView edt_mask_1;
//    private TextView edt_mask_2;

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
        setContentView(R.layout.activity_edit_overall_rhythm);


        if(savedInstanceState!=null){
            rhythmBasedCompounds = savedInstanceState.getParcelable("COMPOUND_RHYTHM");
        }else {
            rhythmBasedCompounds = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
        }
        
        tv_id = findViewById(R.id.tv_rhId_RODA);
        edt_title = findViewById(R.id.edt_title_RODA);
        spinner = findViewById(R.id.spinner_RODA);
        edt_descriptions = findViewById(R.id.edt_description_RODA);

/*
        edt_descriptions.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId()==R.id.edt_description_RODA||!hasFocus){
                    //失去焦点
                    rhythmBasedCompounds.setDescription(edt_descriptions.getText().toString());
                    edt_descriptions.setVisibility(View.GONE);
                    edt_descriptions.setVisibility(View.VISIBLE);
                    edt_descriptions.setText(rhythmBasedCompounds.getDescription());
                }
            }
        });
*/
//        edt_mask_1 = findViewById(R.id.edt_mask1_RODA);
//        edt_mask_2 = findViewById(R.id.edt_mask2_RODA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_RODA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_RODA);

//        mRv_pitches = findViewById(R.id.rv_linkingPitches_RODA);
//        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_RODA);
        rhythmView = findViewById(R.id.rhView_singleLine_RODA);


        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), rhythmBasedCompounds.getId()));
        edt_title.setText(rhythmBasedCompounds.getTitle());

        edt_descriptions.setText(rhythmBasedCompounds.getDescription());
        spinner.setSelection(rhythmBasedCompounds.getStars()-1);//数据数组的值和索引间只相差1.（简便设置方式）【要求cRh内的值在合理范围】
        ckb_selfDesign.setChecked(rhythmBasedCompounds.isSelfDesign());
        ckb_keepTop.setChecked(rhythmBasedCompounds.isKeepTop());

        rhythmView.setRhythmViewData(rhythmBasedCompounds,22,24,24);//比默认的尺寸（18/20/20）稍大

    }



    //修改节奏编码
    public void goEditRhythm(View view){
        //需要跳转到专用的页面进行修改
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", rhythmBasedCompounds);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }

    /*public void editDescription(View view){
        if(edt_descriptions.getVisibility()==View.GONE){
            //尽在edt尚未打卡时起作用
            edt_descriptions.setVisibility(View.GONE);
            edt_descriptions.setVisibility(View.VISIBLE);
            edt_descriptions.setText(rhythmBasedCompounds.getDescription());
        }

    }*/


    /* 以下一个方法只可能是从rh的编码专用修改页返回*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        if(resultCode == RESULT_CODE_RH_PURE_EDIT_DONE){
            rhythmBasedCompounds = data.getParcelableExtra("COMPOUND_RHYTHM");
            rhythmView.setRhythmViewData(rhythmBasedCompounds);//重新设置后会随即刷新。

        }
        //其他控件应当不需要改变。
        //【考虑在返回前已被回收时，可能需重建act，从而onCreate可能从null中获取intent，是否会出错？】
    }



    public void editFinish(View view){
        //点击后，①保存到DB；②返回到前一页（应该是详情页）
        MyRhythmDbHelper myRhythmDbHelper = MyRhythmDbHelper.getInstance(this);
        int affectedRows = myRhythmDbHelper.updateRhythm(rhythmBasedCompounds);
//【如果未做出实质修改，如何避免向DB写入数据】
        Intent intentBack = new Intent();
        intentBack.putExtra("COMPOUND_RHYTHM", rhythmBasedCompounds);
        setResult(RESULT_CODE_RH_OVERALL_EDIT_DONE,intentBack);
        this.finish();

    }


    public void cancel(View view){
        setResult(RESULT_CODE_RH_OVERALL_EDIT_CANCEL);
        this.finish();

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("COMPOUND_RHYTHM", rhythmBasedCompounds);

    }
    
    
}
