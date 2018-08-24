package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

public class AllGroupsActivity extends RvBassActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_rv_general);
        mRv = findViewById(R.id.rv_all_rhythms_RVGL);
        maskView = findViewById(R.id.tv_mask_RVGL);

        new Thread(new FetchDataRunnable()).start();
    }


    void fetchAndSort(){
        //获取节奏数据
        dataFetched = rhythmDbHelper.getAllGroups();
        //暂不排序
    }

    @Override
    void loadAdapter() {
        //初始化Rv构造器，令UI加载Rv控件……
        adapter = new GroupRvAdapter(dataFetched,this) ;
//                Log.i(TAG, "handleMessage:dataFetched="+dataFetched.toString());
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);

    }
}
