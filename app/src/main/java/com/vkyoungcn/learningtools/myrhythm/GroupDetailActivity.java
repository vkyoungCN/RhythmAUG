package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.ChooseRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmLiteForGpX;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_GP_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class GroupDetailActivity<T extends BaseModel> extends TwoResAllRvBaseActivity{
//由于本页面需要从Db为Rv加载大量数据，因而不继承自BaseDetail而继承自RvActivity.
private static final String TAG = "GroupDetailActivity";
    Group group = new Group();

    /*本类特有（毕竟除了Rv之外，这还是个完整的资源详情页）*/
    private TextView tv_id;
    private TextView tv_title;
    private TextView tv_descriptions;

    //用于选择RH（使用了轻量新类型；因为两个旧列表实际传递两种旧类型，无法共存，干脆直接设计个轻量新类型）
    ArrayList<RhythmLiteForGpX> originRhythmsLite = new ArrayList<>();
    ArrayList<RhythmLiteForGpX> rhythmsLiteForChoose = new ArrayList<>();

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
     private ArrayList<Rhythm> fetchAllRhythms(){
         return rhythmDbHelper.getAllRhythms();
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
    //弹出DFG用于选定要添加的旋律，可以是一组，结果发回本Activity，在OnButtonGClick方法中向DB提交
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("CHOOSE_RHYTHM");

        if (prev != null) {
            Toast.makeText(this, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        prepareDataForChoose();
        Log.i(TAG, "addRhForGroup: data to send="+originRhythmsLite.toString());
        DialogFragment dfg = ChooseRhythmDiaFragment.newInstance(rhythmsLiteForChoose,originRhythmsLite);
        dfg.show(transaction, "CHOOSE_RHYTHM");
    }

    private void prepareDataForChoose(){
        rhythmsLiteForChoose.clear();//如果不清空，第二次调用时items重复（数量加倍（大概因为每次从DB取数据实际生成了新Rh，每次是地址不同的新元素））
        originRhythmsLite.clear();//这个可以不清空，（实测不会重复，大概因为地址一致自动过滤了吧）
        for(int i=0;i<dataFetched.size();i++){
            RhythmLiteForGpX liteForGpX = new RhythmLiteForGpX((RhythmBasedCompound) (dataFetched.get(i)));
            originRhythmsLite.add(liteForGpX);
        }

        ArrayList<Rhythm> allRhythms = rhythmDbHelper.getAllRhythms();
        for (int j = 0; j < allRhythms.size(); j++) {
            RhythmLiteForGpX liteForGpX = new RhythmLiteForGpX(allRhythms.get(j));
//            Log.i(TAG, "prepareDataForChoose: lite Single="+liteForGpX.toString());
            rhythmsLiteForChoose.add(liteForGpX);
        }
    }

    public void addLyForGroup(View view){

    }

    public void confirmChose(View view){

    }

    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        //接收从dfg（选择rh）传回的列表，提交到DB
//        int modelId = data.getInt("MODEL_ID");
        switch (dfgType){
            case CHOOSE_RHYTHM_FOR_GROUP:
                ArrayList<RhythmLiteForGpX> rhythmsAddForGP = data.getParcelableArrayList("RHYTHMS");
                if(rhythmsAddForGP==null||rhythmsAddForGP.isEmpty()){
                    Toast.makeText(this, "选定了0个项目。", Toast.LENGTH_SHORT).show();
                    return;
                }
//                rhythmsAddForGP.removeAll(originRhythmsLite);//从待添加的列表中移除原本已有的。
//                【不能如上这种操作，虽然转了一大圈，但两个List实际是同一地址的同一列表。（这也说明data传递、parcel传递是传地址的（？））
// 同时也解释了使用foreach循环一个list对另一个list移除会产生同步改写错误的原因（本来就是同一个表）。（已实测）】
                for (int i = 0; i < dataFetched.size(); i++) {
                    int innerJ = -1;
                    for (int j = 0; j < rhythmsAddForGP.size(); j++) {
                        if(((RhythmBasedCompound)(dataFetched.get(i))).getId()==rhythmsAddForGP.get(j).getId()){
                            innerJ = j;
                            break;
                        }
                    }
                    if(innerJ!=-1) {
                        rhythmsAddForGP.remove(innerJ);
                    }
                }//大概只能这样删除吧。

                Toast.makeText(this, "新选定项目数量："+rhythmsAddForGP.size(), Toast.LENGTH_SHORT).show();

                int l = rhythmDbHelper.createRhythmCrossGroup(group.getId(),rhythmsAddForGP);
                if(l!=0){
                    //更新有效，刷新显示
                    new Thread(new ReFetchRhDataRunnable()).start();

                }else {
                    Toast.makeText(this, "影响的行数为0。", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }


}
