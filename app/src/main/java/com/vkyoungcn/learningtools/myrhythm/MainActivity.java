package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmPrimaryRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.fragments.AddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.04
 * */
public class MainActivity extends AppCompatActivity implements OnGeneralDfgInteraction {
//* 功能：主页面，程序启动后经欢迎（banner页）页面后到达的第一个稳定页面；

    private static final String TAG = "MainActivity";

    public static final int MESSAGE_PRE_DB_FETCHED = 5011;

    /* 控件*/
    private RelativeLayout rlt_fabPanel;
    private FrameLayout flt_mask;
    private RecyclerView mRv;
    private TextView tv_rhythmAmount;

    /* 业务逻辑变量*/
    private boolean isFabPanelExtracted = false;//FAB面板组默认处于回缩状态。

    ArrayList<Rhythm> rhythms;
    ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections;
    ArrayList<Integer> rhythmTypes;
    int rhythmsAllAmount;
    ArrayList<Lyric> primaryLyrics;
    ArrayList<Lyric> secondLyrics;

    private RhythmPrimaryRvAdapter adapter;

    /* 多线程*/
    private Handler handler = new MainActivityHandler(this);//涉及弱引用，通过其发送消息。


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rlt_fabPanel = findViewById(R.id.rlt_fabPanel_MA);
        flt_mask = findViewById(R.id.flt_mask_MA);
        mRv = findViewById(R.id.rv_some_rhythm_MA);
        tv_rhythmAmount = findViewById(R.id.tv_rhythmAmount_MA);

        rlt_fabPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在板子展开后，点击板子需要能缩回（隐藏）
                if(isFabPanelExtracted){
                    rlt_fabPanel.setVisibility(View.GONE);
                    isFabPanelExtracted = false;
                }//只负责缩回就好了。
            }
        });

        new Thread(new PrepareCompoundRhythmsRunnable()).start();


    }

    /*
     * 当Fab按键系统的主按钮点击时调用
     *
     * ①根据标志变量判定Fab组是否处于展开状态（未展开则展开）
     * ②展开：变量置反；组Rlt取消隐藏；（加载动画）
     * 只需负责展开
     * */
    public void fabMainClick(View view) {
        if (!isFabPanelExtracted) {//未展开，要做展开操作

            //标志变量取反
            isFabPanelExtracted = true;
            //展开（取消隐藏）
            rlt_fabPanel.setVisibility(View.VISIBLE);

        }/* else {
            isFabPanelExtracted = false;
            rlt_fabPanel.setVisibility(View.GONE);
        }*/
    }


    public void addRhythm(View view){
//        点击后记得把面板收回。

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("ADD_RHYTHM");

        if (prev != null) {
            Toast.makeText(this, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = AddRhythmDiaFragment.newInstance();
        dfg.show(transaction, "ADD_RHYTHM");

    }

    public void createPitchSerial(View view){

    }

    public void createWords(View view){

    }


    final static class MainActivityHandler extends Handler {
        private final WeakReference<MainActivity> activityWeakReference;

        private MainActivityHandler(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

    void handleMessage(Message message) {
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED:
                //上方还有一个Tv没有设置数据
                tv_rhythmAmount.setText(String.format(getResources().getString(R.string.psh_totalRhythmAmount),rhythmsAllAmount));

                //取消上方遮罩
                flt_mask.setVisibility(View.GONE);

                //初始化Rv构造器，令UI加载Rv控件……
                adapter = new RhythmPrimaryRvAdapter(rhythmCodesInSections,primaryLyrics,secondLyrics, rhythmTypes,this) ;
                mRv.setLayoutManager(new LinearLayoutManager(this));
                mRv.setHasFixedSize(true);
                mRv.setAdapter(adapter);

                break;

        }

    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case ADD_RHYTHM:
                //准备进入第二步（新增、编辑Rh）
                Intent intentToRhStep_2 = new Intent(this,AddRhythmActivity.class);
                intentToRhStep_2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intentToRhStep_2.putExtra("BUNDLE",data);
                this.startActivity(intentToRhStep_2);
                break;



        }
    }



    public class PrepareCompoundRhythmsRunnable implements Runnable{
        @Override
        public void run() {
            //数据库获取标记有（置顶）的，以及修改时间在一周内的Rhythms；
            //获取这些Rhythm对应的主要歌词记录【暂定在此rv中只显示节奏+词；而旋律只在点进详情后再显示】
            MyRhythmDbHelper rhythmDbHelper = MyRhythmDbHelper.getInstance(getApplicationContext());

            rhythms = rhythmDbHelper.getAllRhythms() ;
            rhythmsAllAmount = rhythmDbHelper.getAmountOfRhythms();//有一个控件需要使用节奏总数量

            rhythmCodesInSections = new ArrayList<>();
            rhythmTypes = new ArrayList<>();
            for (Rhythm rh :rhythms) {
                Log.i(TAG, "run: rhythm-"+rh.getId()+"rh.codes="+rh.getRhythmCodeSerial().toString());
                rhythmCodesInSections.add(RhythmHelper.codeParseIntoSections(rh.getRhythmCodeSerial(),rh.getRhythmType()));
//                primaryLyrics.add(rh.getPrimaryLyricId());
                rhythmTypes.add(rh.getRhythmType());

            }



            Message message = new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);

        }
    }




    /*
     * 阻止返回到Logo页
     * back将直接退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
