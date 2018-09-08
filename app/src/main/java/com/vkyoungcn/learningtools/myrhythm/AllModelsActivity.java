package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.adapter.LyricFreeRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;
import java.util.Collections;

public class AllModelsActivity extends TwoResAllRvBaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_models);

        maskView = findViewById(R.id.tv_mask1_AMA);
        maskView_lyric = findViewById(R.id.tv_mask2_AMA);
//        maskView_pitch = findViewById(R.id.tv_mask3_GDA);

        mRv = findViewById(R.id.rv_linkingRhythms_AMA);
        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_AMA);
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
        dataReFetched = rhythmDbHelper.getAllCompoundRhythms();
        dataReList_lyric = rhythmDbHelper.getAllLyrics();
        super.reFetchAndSort();//基类负责对原数据、新数据进行非空检测，然后情况--添加。

    }

    void reFetchRhAndSort(){
        //获取节奏数据
        dataReFetched = rhythmDbHelper.getAllCompoundRhythms();
//        Log.i(TAG, "reFetchAndSort: daRFd="+dataReFetched.toString());
//        dataReList_lyric = rhythmDbHelper.getFreeLyricsByGid(group.getId());
        super.reFetchRhAndSort();
    }

    void reFetchLyAndSort(){
        //获取节奏数据
//        dataReFetched = rhythmDbHelper.getRhythmBasedCompoundsByGid(group.getId());
//        Log.i(TAG, "reFetchAndSort: daRFd="+dataReFetched.toString());
        dataReList_lyric = rhythmDbHelper.getAllLyrics();
        super.reFetchLyAndSort();
    }


}
