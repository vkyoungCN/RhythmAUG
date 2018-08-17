package com.vkyoungcn.learningtools.myrhythm;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.PitchSequence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
        setContentView(R.layout.activity_rhythm_detail);

        tv_id = findViewById(R.id.tv_rhId_RDA);
        tv_title = findViewById(R.id.tv_title_RDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_RDA);
        tv_stars = findViewById(R.id.tv_starts_RDA);
        tv_descriptions = findViewById(R.id.tv_description_RDA);
//        tv_mask_1 = findViewById(R.id.tv_mask1_RDA);
//        tv_mask_2 = findViewById(R.id.tv_mask2_RDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_RDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_RDA);

//        mRv_pitches = findViewById(R.id.rv_linkingPitches_RDA);
//        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_RDA);
        rhythmView = findViewById(R.id.rhView_singleLine_RDA);


        compoundRhythm = getIntent().getParcelableExtra("RHYTHM");

        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), compoundRhythm.getId()));
        tv_title.setText(compoundRhythm.getTitle());
        tv_lastModifyTime.setText(compoundRhythm.getLastModifyTimeStr());

        tv_descriptions.setText(compoundRhythm.getDescription());
        tv_stars.setText(String.format(getResources().getString(R.string.plh_stars_num),compoundRhythm.getStars()));
        ckb_selfDesign.setChecked(compoundRhythm.isSelfDesign());
        ckb_keepTop.setChecked(compoundRhythm.isKeepTop());

        rhythmView.setRhythmViewData(compoundRhythm,22,24,24);//比默认的尺寸（18/20/20）稍大




    }

   /* final static class RhythmDetailActivityHandler extends Handler {
        private final WeakReference<RhythmDetailActivity> activityWeakReference;

        private RhythmDetailActivityHandler(RhythmDetailActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RhythmDetailActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

 void handleMessage(Message message) {
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED:
                //取消Rv区域的遮罩
                tv_mask_1.setVisibility(View.GONE);

                //初始化Rv构造器，令UI加载Rv控件……
                adapter_p = new PitchesRvAdapter(this) ;
                adapter_v = new LyricsRvAdapter(this) ;
                mRv_pitches.setLayoutManager(new LinearLayoutManager(this));
                mRv_pitches.setAdapter(adapter_p);

                mRv_lyrics.setLayoutManager(new LinearLayoutManager(this));
                mRv_lyrics.setAdapter(adapter_v);

                break;

        }

    }*/

}
