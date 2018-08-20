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
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.AddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.04
 * */
public class MainActivity extends AppCompatActivity implements OnGeneralDfgInteraction,MyRhythmConstants {
//* 功能：主页面，程序启动后经欢迎（banner页）页面后到达的第一个稳定页面；

    private static final String TAG = "MainActivity";

    public static final int MESSAGE_PRE_DB_FETCHED = 5011;
    public static final int MESSAGE_RE_FETCHED = 5012;

    MyRhythmDbHelper rhythmDbHelper;
    long timeThreshold;
    /* 控件*/
    private RelativeLayout rlt_fabPanel;
    private FrameLayout flt_mask;
    private RecyclerView mRv;
    private TextView tv_rhythmAmount;

    /* 业务逻辑变量*/
    private boolean isFabPanelExtracted = false;//FAB面板组默认处于回缩状态。

    ArrayList<CompoundRhythm> compoundRhythms;
//    ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections;
//    ArrayList<Integer> rhythmTypes;
    int rhythmsAllAmount;
//    ArrayList<Lyric> primaryLyrics;
//    ArrayList<Lyric> secondLyrics;

    private RhythmRvAdapter adapter;

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

        timeThreshold = System.currentTimeMillis()-1000*60*60*24*7;
        rhythmDbHelper = MyRhythmDbHelper.getInstance(getApplicationContext());

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

        new Thread(new PrepareCompoundRhythmsAndAmountRunnable()).start();

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
                if(flt_mask.getVisibility() == View.VISIBLE) {
                    flt_mask.setVisibility(View.GONE);
                }

                //初始化Rv构造器，令UI加载Rv控件……
                adapter = new RhythmRvAdapter(compoundRhythms,this) ;
                mRv.setLayoutManager(new LinearLayoutManager(this));
                mRv.setAdapter(adapter);

                break;

            case MESSAGE_RE_FETCHED:
                //Tv更新数据
                tv_rhythmAmount.setText(String.format(getResources().getString(R.string.psh_totalRhythmAmount),rhythmsAllAmount));

                //取消上方遮罩
                if(flt_mask.getVisibility() == View.VISIBLE) {
                    flt_mask.setVisibility(View.GONE);
                }

                //Rv数据修改
                adapter.notifyDataSetChanged();
                break;


        }

    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case CREATE_RHYTHM:
                //准备进入第二步（新增、编辑Rh）
                Intent intentToRhStep_2 = new Intent(this,CreateRhythmActivity.class);
                intentToRhStep_2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intentToRhStep_2.putExtra("BUNDLE",data);
                this.startActivityForResult(intentToRhStep_2,REQUEST_CODE_RH_CREATE);
                break;
            case DELETE_RHYTHM:
                //真正的删除任务在这里实现

                rhythmDbHelper.deleteRhythmById(data.getInt("RHYTHM_ID"));

                //删完要刷新
                new Thread(new ReFetchCompoundRhythmsRunnable()).start();

                break;

        }
    }


    class FetchDataRunnable_Bass implements Runnable{
        @Override
        public void run() {
            //获取这些Rhythm对应的主要歌词记录【暂定在此rv中只显示节奏+词；而旋律只在点进详情后再显示】

            //暂时只获取一周内修改过的节奏记录(以及标有置顶标记的)
            //一周内修改过的所有节奏记录（带词序字串）
            compoundRhythms = rhythmDbHelper.getTopKeepCompoundRhythmsOrModifiedLaterThan(timeThreshold) ;

            //对返回的节奏进行排序（置顶的在最上，其余按时间，越近越先）
            shaftRhythms(compoundRhythms);

            //获取总量数字
            rhythmsAllAmount = rhythmDbHelper.getAmountOfRhythms();//有一个控件需要使用节奏总数量

        }
    }



    public class PrepareCompoundRhythmsAndAmountRunnable extends FetchDataRunnable_Bass {
        @Override
        public void run() {
            super.run();

            //然后封转消息
            Message message = new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);
        }
    }


    public class ReFetchCompoundRhythmsRunnable extends FetchDataRunnable_Bass {
        @Override
        public void run() {
            super.run();

            Message message = new Message();
            message.what = MESSAGE_RE_FETCHED;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //更新当前列表的显示，重新获取“置顶和新的”节奏数据

        switch (resultCode){
            case RESULT_CODE_RH_CREATE_DONE:
                new Thread(new ReFetchCompoundRhythmsRunnable()).start();
                break;
            case RESULT_CODE_RH_CREATE_FAILURE:
            default:
                Toast.makeText(this, "添加失败。", Toast.LENGTH_SHORT).show();
                //什么都不做，暂时只有一个提示。
                break;
        }
    }

    private void shaftRhythms(ArrayList<CompoundRhythm> compoundRhythms){
        //暂定让置顶在最前（且均按时间先后排序）

        //拆分两列
        ArrayList<CompoundRhythm> tempCodes_keepTop = new ArrayList<>();
        ArrayList<CompoundRhythm> tempCodes_UnKeepTop = new ArrayList<>();
        for (CompoundRhythm compoundRhythm :compoundRhythms) {
            if(compoundRhythm.isKeepTop()){tempCodes_keepTop.add(compoundRhythm);}
            else {tempCodes_UnKeepTop.add(compoundRhythm);}
        }

        //分别按时间排序
        Collections.sort(tempCodes_UnKeepTop,new SortByModifyTime());
        Collections.sort(tempCodes_keepTop,new SortByModifyTime());
        //然后合并
        compoundRhythms.clear();
        compoundRhythms.addAll(tempCodes_keepTop);
        compoundRhythms.addAll(tempCodes_UnKeepTop);
    }

    class SortByModifyTime implements Comparator {
        public int compare(Object o1, Object o2) {
            CompoundRhythm s1 = (CompoundRhythm) o1;
            CompoundRhythm s2 = (CompoundRhythm) o2;
            return -Long.compare(s1.getLastModifyTime(), s2.getLastModifyTime());
            //降序
        }
    }


    public void toAllRhythms(){
        Intent intentToAllRhs = new Intent(this,AllRhythmsActivity.class);
        this.startActivity(intentToAllRhs);


    }

}
