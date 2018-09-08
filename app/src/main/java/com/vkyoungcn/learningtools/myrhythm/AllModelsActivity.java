package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.LyricFreeRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;
import java.util.Collections;

public class AllModelsActivity extends ThreadRvBassActivity{

    private TextView maskView_lyric;
//    private TextView maskView_pitch;

    private RecyclerView mRv_lyrics;
//    private RecyclerView mRv_pitches;
    private LyricFreeRvAdapter adapter_lfr;
    private ArrayList<Lyric> dataList_lyric;
//    private ArrayList<String> dataList_pitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_models);

        maskView = findViewById(R.id.tv_mask1_GDA);
        maskView_lyric = findViewById(R.id.tv_mask2_GDA);
//        maskView_pitch = findViewById(R.id.tv_mask3_GDA);

        mRv = findViewById(R.id.rv_linkingRhythms_GDA);
        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_GDA);
//        mRv_pitches = findViewById(R.id.rv_linkingPitches_GDA);

        new Thread(new FetchDataRunnable()).start();//使用基类中的实现
    }

    @Override
    void fetchAndSort(){
        dataFetched = rhythmDbHelper.getAllCompoundRhythms();
        dataList_lyric = rhythmDbHelper.getAllLyrics();//不按归属
//        rhythmDbHelper.(group.getId());

    }
    void reFetchAndSort(){
        //获取节奏数据
        dataFetched.clear();
        dataFetched.addAll(rhythmDbHelper.getAllLyrics());

    }

    @Override
    void loadAdapter() {
        //取消其他两个mask（节奏的在基类已取消显示）
        maskView_lyric.setVisibility(View.GONE);
//        maskView_pitch.setVisibility(View.GONE);

        //初始化Rv构造器，令UI加载Rv控件……
        adapter = new RhythmRvAdapter(dataFetched,this) ;
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);

        adapter_lfr = new LyricFreeRvAdapter(dataList_lyric,this);
        mRv_lyrics.setLayoutManager(new LinearLayoutManager(this));
        mRv_lyrics.setAdapter(adapter_lfr);

    }

}
