package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.AddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;
import java.util.Collections;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.04
 * */
public class MainActivity extends RhythmRvBassActivity implements OnGeneralDfgInteraction,MyRhythmConstants {
//* 功能：主页面，程序启动后经欢迎（banner页）页面后到达的第一个稳定页面；

    private static final String TAG = "MainActivity";

    long timeThreshold;
    /* 控件*/
    //本实现类的新字段
    private RelativeLayout rlt_fabPanel;
    private TextView tv_rhythmAmount;

    /* 业务逻辑变量*/
    private boolean isFabPanelExtracted = false;//FAB面板组默认处于回缩状态。
    int rhythmsAllAmount;
    boolean changeToTrueInOnCreate = false;//onPause置否


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "onCreate: b");
        setContentView(R.layout.activity_main);

        changeToTrueInOnCreate = true;

        rlt_fabPanel = findViewById(R.id.rlt_fabPanel_MA);
        maskView = findViewById(R.id.flt_mask_MA);
        mRv = findViewById(R.id.rv_some_rhythm_MA);
        tv_rhythmAmount = findViewById(R.id.tv_rhythmAmount_MA);

        timeThreshold = System.currentTimeMillis()-1000*60*60*24*7;

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

        new Thread(new FetchDataRunnable()).start();//使用基类中的实现
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!changeToTrueInOnCreate){
            //如果是从其他页面返回，通常没有经过onCreate，数据还是旧的需要再获取一遍。
            new Thread(new ReFetchDataRunnable()).start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        changeToTrueInOnCreate = false;
    }

    void fetchAndSort() {
//        Log.i(TAG, "fetchAndSort: main reFetch");
        //获取数据
        dataFetched = rhythmDbHelper.getTopKeepCompoundRhythmsOrModifiedLaterThan(timeThreshold) ;
//        Log.i(TAG, "fetchAndSort: main reFetched.Size="+dataFetched.size());
        //对返回的节奏进行排序（置顶的在最上，其余按时间，越近越先）
        if(dataFetched!=null) {
            shaftRhythms(dataFetched);
            //获取总量数字【这也是需要再对RRBA再重写一遍的原因，多了这个字段的需求。】
            rhythmsAllAmount = dataFetched.size();//有一个控件需要使用节奏总数量
        }
    }

    void reFetchAndSort(){
        //获取节奏数据
//        dataReFetched =rhythmDbHelper.getAllCompoundRhythms();
        super.reFetchAndSort();

        //对返回的节奏进行排序（按修改时间降序？）
        if(dataFetched!=null) {
            Collections.sort(dataFetched, new SortByModifyTime());
            rhythmsAllAmount = dataFetched.size();//有一个控件需要使用节奏总数量
        }
    }


    void nullDataOperation(){
        //为空时，基类在调用loadAdapter前被截止。补充一个设置方法。
        tv_rhythmAmount.setText(String.format(getResources().getString(R.string.plh_groupAmount),0));
    }
        /*
     * 当Fab按键系统的主按钮点击时调用
     *
     * ①根据标志变量判定Fab组是否处于展开状态（未展开则展开）
     * ②展开：变量置反；组Rlt取消隐藏；（加载动画）
     * 只需负责展开
     * */
    public void fabMainClick(View view) {
        extractFabPanel();
        /* else {
            isFabPanelExtracted = false;
            rlt_fabPanel.setVisibility(View.GONE);
        }*/
    }

    private void extractFabPanel() {
        if (!isFabPanelExtracted) {//未展开，要做展开操作
            //标志变量取反
            isFabPanelExtracted = true;
            //展开（取消隐藏）
            rlt_fabPanel.setVisibility(View.VISIBLE);
        }
    }

    private void collapseFabPanel() {
        if (isFabPanelExtracted) {//未展开，要做展开操作
            //标志变量取反
            isFabPanelExtracted = false;
            //展开（取消隐藏）
            rlt_fabPanel.setVisibility(View.GONE);
        }
    }



    public void createRhythm(View view){
//        点击后记得把面板收回。
        collapseFabPanel();
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
        Intent intentToLyricCreate = new Intent(this,LyricCreateEntranceActivity.class);
//        intentToLyricCreate.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivityForResult(intentToLyricCreate,REQUEST_CODE_LY_CREATE);
    }

    public void toAllRhythms(View view){
        Intent intentToAllRhs = new Intent(this,AllRhythmsActivity.class);
        this.startActivity(intentToAllRhs);
    }

    public void toGroups(View view){
        Intent intentToAllGroups = new Intent(this,AllGroupsActivity.class);
        this.startActivity(intentToAllGroups);
    }


    public void toAllModels(View view){
        Intent intentToAllModels = new Intent(this,AllModelsActivity.class);
        this.startActivity(intentToAllModels);
    }

    public void toBackUpActivity(View view){
        Intent intentToBk = new Intent(this,BackUpActivity.class);
        this.startActivity(intentToBk);
    }


    void handleMessage(Message message) {
//        Log.i(TAG, "handleMessage: MA handle. amount="+rhythmsAllAmount);

        //上方还有一个Tv没有设置数据(不区分sw_case)
        tv_rhythmAmount.setText(String.format(getResources().getString(R.string.psh_totalRhythmAmount),rhythmsAllAmount));
        super.handleMessage(message);

    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        super.onButtonClickingDfgInteraction(dfgType,data);
        switch (dfgType){
            case CREATE_RHYTHM:
                //准备进入第二步（新增、编辑Rh）
                int rhythmTypeChose = data.getInt("RHYTHM_TYPE");

                Intent intentToRhStep_2 = new Intent(this,RhythmCreateActivity.class);
//                intentToRhStep_2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);【如果有这个就没法传回来结果码，暂注释，待改】
                intentToRhStep_2.putExtra("RHYTHM_TYPE",rhythmTypeChose);
                this.startActivityForResult(intentToRhStep_2,REQUEST_CODE_RH_CREATE);
                break;
            case DELETE_RHYTHM:
                //基类中执行 DB删除和刷新，再执行改总数
//                tv_rhythmAmount.setText(String.format(getResources().getString(R.string.psh_totalRhythmAmount),rhythmsAllAmount));
                break;

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
//        Log.i(TAG, "onActivityResult: resultCode="+resultCode);
        switch (resultCode){
            case RESULT_CODE_RH_CREATE_DONE:
//                Log.i(TAG, "onActivityResult: CREATED DONE.");
//                new Thread(new ReFetchDataRunnable()).start();【在onResume中有重新获取的操作了】
                break;
            case RESULT_CODE_RH_CREATE_FAILURE:
            case DELIVER_ERROR:
                Toast.makeText(this, "添加失败。", Toast.LENGTH_SHORT).show();
                //什么都不做，暂时只有一个提示。
                break;

            case RESULT_CODE_GP_CREATED:
            case RESULT_CODE_LY_CREATED:
                //不做任何事。因为主页不存在gp、ly的显示内容。
                break;
        }
    }

    private void shaftRhythms(ArrayList<RhythmBasedCompound> compounds){
        //暂定让置顶在最前（且均按时间先后排序）

        //拆分两列
        ArrayList<RhythmBasedCompound> tempCodes_keepTop = new ArrayList<>();
        ArrayList<RhythmBasedCompound> tempCodes_UnKeepTop = new ArrayList<>();
        for (RhythmBasedCompound rhythmBasedCompound : compounds) {
            if(rhythmBasedCompound.isKeepTop()){tempCodes_keepTop.add(rhythmBasedCompound);}
            else {tempCodes_UnKeepTop.add(rhythmBasedCompound);}
        }

        //分别按时间排序
        Collections.sort(tempCodes_UnKeepTop,new SortByModifyTime());
        Collections.sort(tempCodes_keepTop,new SortByModifyTime());
        //然后合并
        compounds.clear();
        compounds.addAll(tempCodes_keepTop);
        compounds.addAll(tempCodes_UnKeepTop);
    }

    public void refresh(View view){
        new Thread(new ReFetchDataRunnable()).start();
    }

}
