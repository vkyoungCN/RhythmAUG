package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.LyricFreeRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_GP_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_OVERALL_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_OVERALL_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class GroupDetailActivity extends TwoResAllRvBaseActivity{
//由于本页面需要从Db为Rv加载大量数据，因而不继承自BaseDetail而继承自RvActivity.
private static final String TAG = "GroupDetailActivity";
    Group group = new Group();

    /*本类特有（毕竟除了Rv之外，这还是个完整的资源详情页）*/
    private TextView tv_id;
    private TextView tv_title;
    private TextView tv_descriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);


        if(savedInstanceState!=null){
            group = savedInstanceState.getParcelable("MODEL");
        }else {
            group = getIntent().getParcelableExtra("MODEL");
        }

        tv_id = findViewById(R.id.tv_rhId_GDA);
        tv_title = findViewById(R.id.tv_title_GDA);
        tv_descriptions = findViewById(R.id.tv_description_GDA);

        mRv = findViewById(R.id.rv_linkingRhythms_GDA);
//        mRv_pitches = findViewById(R.id.rv_linkingPitches_GDA);
        mRv_lyrics = findViewById(R.id.rv_linkingLyrics_GDA);
        maskView = findViewById(R.id.tv_mask1_GDA);
        maskView_lyric = findViewById(R.id.tv_mask2_GDA);
//        maskView_pitch = findViewById(R.id.tv_mask3_GDA);

        initUiData();
        new Thread(new FetchDataRunnable()).start();//使用基类中的实现

    }


    void fetchAndSort(){
        dataFetched = rhythmDbHelper.getRhythmBasedCompoundsByGid(group.getId());
        dataList_lyric = rhythmDbHelper.getFreeLyricsByGid(group.getId());
    }

    void reFetchAndSort(){
        //获取节奏数据
        dataReFetched = rhythmDbHelper.getRhythmBasedCompoundsByGid(group.getId());
//        Log.i(TAG, "reFetchAndSort: daRFd="+dataReFetched.toString());
        dataReList_lyric = rhythmDbHelper.getFreeLyricsByGid(group.getId());
        super.reFetchAndSort();

    }

    void reFetchRhAndSort(){
        //获取节奏数据
        dataReFetched = rhythmDbHelper.getRhythmBasedCompoundsByGid(group.getId());
//        Log.i(TAG, "reFetchAndSort: daRFd="+dataReFetched.toString());
//        dataReList_lyric = rhythmDbHelper.getFreeLyricsByGid(group.getId());
        super.reFetchRhAndSort();
    }

    void reFetchLyAndSort(){
        //获取节奏数据
//        dataReFetched = rhythmDbHelper.getRhythmBasedCompoundsByGid(group.getId());
//        Log.i(TAG, "reFetchAndSort: daRFd="+dataReFetched.toString());
        dataReList_lyric = rhythmDbHelper.getFreeLyricsByGid(group.getId());
        super.reFetchLyAndSort();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回之后，就是编码数据有所改变，从新加载rhV的显示
        switch (resultCode){
            case RESULT_CODE_GP_EDIT_DONE:
                //这个数据是传递回来的，因为本页在进入伊始就没有涉及DB
                group = data.getParcelableExtra("MODEL");
                initUiData();//重新设置UI数据。

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("MODEL", group);

    }


    private void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), group.getId()));
        tv_title.setText(group.getTitle());
        tv_descriptions.setText(group.getDescription());

    }

    public void toEditGroupActivity(View view){
        Intent intentToOverallEditor = new Intent(this,GroupEditActivity.class);
        intentToOverallEditor.putExtra("MODEL", group);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_GP_EDIT);


    }

    public void refreshRh(View view){
        new Thread(new ReFetchRhDataRunnable()).start();
    }

    public void refreshLy(View view){
        new Thread(new ReFetchLyDataRunnable()).start();
    }

    public void addRhForGroup(View view){

    }

    public void addLyForGroup(View view){

    }
}
