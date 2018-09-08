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

public class TwoResAllRvBaseActivity extends ThreadRvBassActivity{
//因为显示两类资源，因而有两组 mask+Rv+adapter+data。
    /* 第二组*/
    TextView maskView_lyric;
    RecyclerView mRv_lyrics;
    LyricFreeRvAdapter adapter_lfr;
    ArrayList<Lyric> dataList_lyric;
    ArrayList<Lyric> dataReList_lyric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//布局由子类加载
//        new Thread(new FetchDataRunnable()).start();//使用基类中的实现
    }

    @Override
    void fetchAndSort(){
        //两类子类略有不同（一个是全获取，一个是按gid获取）
//        rhythmDbHelper.(group.getId());

    }
    void reFetchAndSort(){
        //子类重新获取数据
        //基类清空旧数据，添加为新数据。
        dataFetched.clear();
        dataFetched.addAll(dataReFetched);
        dataList_lyric.clear();
        dataList_lyric.addAll(dataReList_lyric);

    }

    void reFetchLyAndSort(){
        //获取需要子类实现
        dataList_lyric.clear();
        dataList_lyric.addAll(dataReList_lyric);
    }

    @Override
    void loadAdapter() {
        //两子类一致，直接在本基类设计即可。
            //（节奏的mask在基类已取消显示）
        //【注意，无论数据是否empty，都要实例化adp，否则刷新时空指针。】
        adapter = new RhythmRvAdapter(dataFetched,this) ;
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);
        if(dataFetched.isEmpty()) {
            Toast.makeText(this, "无节奏数据。", Toast.LENGTH_SHORT).show();
        }

        maskView_lyric.setVisibility(View.GONE);
        adapter_lfr = new LyricFreeRvAdapter(dataList_lyric,this);
        mRv_lyrics.setLayoutManager(new LinearLayoutManager(this));
        mRv_lyrics.setAdapter(adapter_lfr);

        if(dataList_lyric.isEmpty()){
            Toast.makeText(this, "无词作数据。", Toast.LENGTH_SHORT).show();
        }
    }

    //双资源子类需要覆写付下两方法（用于处理第二adp的变动）
    void notifyAdapter(){
        super.notifyAdapter();//基类默认更新一项
        adapter_lfr.notifyDataSetChanged();//更新第二项

    }

    void notifySecondAdp(){
        //子类更新其独有的第二项ADP
        adapter_lfr.notifyDataSetChanged();//更新第二项
    }
}
