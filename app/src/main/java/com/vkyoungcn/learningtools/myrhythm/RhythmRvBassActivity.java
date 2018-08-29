package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.Collections;
import java.util.Comparator;

public class RhythmRvBassActivity extends ThreadRvBassActivity implements OnGeneralDfgInteraction {
    private static final String TAG = "RhythmRvBassActivity";
    /* 基类的部分字段实现为具体类型的字段*/
    Handler handler = new RvBassActivityHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这是一个用于扩展的基础类，不加载具体布局

    }


    void fetchAndSort(){
        //获取节奏数据
        dataFetched = rhythmDbHelper.getAllCompoundRhythms() ;
        //对返回的节奏进行排序（按修改时间降序？）
        Collections.sort(dataFetched,new SortByModifyTime());
    }


    class SortByModifyTime implements Comparator {
        public int compare(Object o1, Object o2) {
            RhythmBasedCompound s1 = (RhythmBasedCompound) o1;
            RhythmBasedCompound s2 = (RhythmBasedCompound) o2;
            return -Long.compare(s1.getLastModifyTime(), s2.getLastModifyTime());
            //降序
        }
    }


    @Override
    void loadAdapter() {
        //初始化Rv构造器，令UI加载Rv控件……
        adapter = new RhythmRvAdapter(dataFetched,this) ;
//                Log.i(TAG, "handleMessage:dataFetched="+dataFetched.toString());
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);
    }




}
