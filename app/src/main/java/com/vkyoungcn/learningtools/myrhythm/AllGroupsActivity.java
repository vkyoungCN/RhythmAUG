package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.GroupRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.Collections;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_GP_CREATE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_CREATED;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class AllGroupsActivity extends ThreadRvBassActivity {
    private static final String TAG = "AllGroupsActivity";
    //本实现类的新字段
    private TextView tv_amount;
    private int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_rv_group);
        mRv = findViewById(R.id.rv_all_rhythms_RVGP);
        maskView = findViewById(R.id.tv_mask_RVGP);
        tv_amount = findViewById(R.id.tv_gpAmount_RVFP);

        new Thread(new FetchDataRunnable()).start();
    }


    void fetchAndSort(){
        //获取节奏数据
        dataFetched = rhythmDbHelper.getAllGroups();
        amount = dataFetched.size();
    }
    void reFetchAndSort(){
        //获取节奏数据
        dataReFetched = rhythmDbHelper.getAllGroups();
        amount = dataFetched.size();
        super.reFetchAndSort();//基类负责对原数据、新数据进行非空检测，然后情况--添加。
    }

   /* void nullDataOperation(){
        //为空时，基类在调用loadAdapter前被截止。补充一个设置方法。
        tv_amount.setText(String.format(getResources().getString(R.string.plh_groupAmount),amount));
    }*/


    @Override
    void loadAdapter() {
        /*if(dataFetched==null){
            tv_amount.setText(String.format(getResources().getString(R.string.plh_groupAmount),0));
            return;
        }*/
        //初始化Rv构造器，令UI加载Rv控件……
        adapter = new GroupRvAdapter(dataFetched,this) ;
//                Log.i(TAG, "handleMessage:dataFetched="+dataFetched.toString());
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);
        tv_amount.setText(String.format(getResources().getString(R.string.plh_groupAmount),amount));

    }


    public void createGroup(View view){
        Intent intentCreateGp = new Intent(this,GroupCreateActivity.class);
        intentCreateGp.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intentCreateGp.putExtra("MODEL",new Group());
        this.startActivityForResult(intentCreateGp,REQUEST_CODE_GP_CREATE);
    }

    /*基本上是从“新建分组”接收返回*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        switch (resultCode){
            case RESULT_CODE_GP_CREATED:
                //这个数据是传递回来的，因为本页在进入伊始就没有涉及DB
                new Thread(new ReFetchDataRunnable()).start();
                break;

        }
    }


    public void refresh(View view){
        new Thread(new ReFetchDataRunnable()).start();
    }
}
