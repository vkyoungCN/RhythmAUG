package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmToBitmap;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.fragments.ShowBitmapDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_LYPH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_OVERALL_EDIT;

public class RhythmDetailActivity extends BaseModelDetailActivity implements RhythmToBitmap.OnRtbDataReady {
/* 由于暂时取消了多交叉复杂关系，暂不在本页面显示“相关的音序和歌词”
* 其实这种互相关联的功能已经涉及到了创作的部分，暂时先实现记录，再谋求创作。
* */
private static final String TAG = "RhythmDetailActivity";
    //rhythm下的特别控件
    private RhythmView rhythmView;
    RhythmToBitmap rhythmToBitmap;//用于转换


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initUiData();
    }



    public void goEditRhythm(View view){
        //节奏特有方法（在点击rh特别控件右下角图标时触发，只修改rh编码）
        //需要跳转到专用的页面进行修改【注意，这个是直接跳到对节奏编码修改的页面】
        Intent intentToRhEditor = new Intent(this,RhythmPureEditActivity.class);
        intentToRhEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToRhEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToRhEditor,REQUEST_CODE_RH_EDIT);

    }

    public void goEditLyricA(View view){
        Intent intentToLyEditor = new Intent(this,LyricPhrasesEditActivity.class);
        intentToLyEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToLyEditor.putExtra("IS_PRIMARY",true);
        intentToLyEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToLyEditor,REQUEST_CODE_LYPH_EDIT);
    }

    public void goEditLyricB(View view){
        Intent intentToLyEditor = new Intent(this,LyricPhrasesEditActivity.class);
        intentToLyEditor.putExtra("COMPOUND_RHYTHM", model);
        intentToLyEditor.putExtra("IS_PRIMARY",false);
        intentToLyEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToLyEditor,REQUEST_CODE_LYPH_EDIT);
    }
    @Override
    public void toEditActivity(View view){
        //这个是跳到全修改页面（但是其中编码仍是展示而非修改，因为编码的修改必须要开启专用页面）

        Intent intentToOverallEditor = new Intent(this,RhythmOverallEditActivity.class);
        intentToOverallEditor.putExtra("MODEL", model);
        intentToOverallEditor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentToOverallEditor,REQUEST_CODE_RH_OVERALL_EDIT);


    }

/*
    @Override
    void loadData(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            model = savedInstanceState.getParcelable("MODEL");
        }else {
//            Log.i(TAG, "onCreate: from Intent");
            model = getIntent().getParcelableExtra("MODEL");
        }
    }*/

    @Override
    void loadComponents(){
        setContentView(R.layout.activity_detail_rhythm);

        /* 加载各控件（基类共用）*/
        tv_id = findViewById(R.id.tv_rhId_RDA);
        tv_title = findViewById(R.id.tv_title_RDA);
        tv_lastModifyTime = findViewById(R.id.tv_lastModifyTime_RDA);
        tv_stars = findViewById(R.id.tv_starts_RDA);
        tv_descriptions = findViewById(R.id.tv_description_RDA);

        ckb_selfDesign = findViewById(R.id.ckb_isSelfDesign_RDA);
        ckb_keepTop = findViewById(R.id.ckb_isKeepTop_RDA);

        //加载本类特有控件
        rhythmView = findViewById(R.id.rhView_RDA);

    }


    @Override
    void initUiData(){
        super.initUiData();
        Log.i(TAG, "initUiData: model="+model.toString());
        rhythmView.setRhythmViewData((RhythmBasedCompound) model);//比默认的尺寸（18/20/20）稍大
//        Log.i(TAG, "initUiData: rh.cs="+((RhythmBasedCompound) model).getCodeSerialByte().toString());
    }


    public void captureTest(View view){

        //需要通过RhythmToBitmap生成Bitmap转换
        rhythmToBitmap = new RhythmToBitmap(this);
        rhythmToBitmap.setRtbReadyListener(this);
        rhythmToBitmap.setDataAndParams((RhythmBasedCompound) model,1080);
        //到此，等待Rtb计算完毕后，再继续执行后续转换（因为）
//        Bitmap bitmap = convertViewToBitmap(rhythmView);




    }



    public void captureActionStep_2(){
        Bitmap bitmap = rhythmToBitmap.makeBitmap();

        if(!saveBitmapLocalTest(bitmap)){
            return;
        }

        /*if(!saveBitmapLocalPrivateTest(bitmap)){
            return;
        }
*/
        //dfg中显示所生成的图片
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("BITMAP_SHOW");

        if (prev != null) {
            Toast.makeText(getApplicationContext(), "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = ShowBitmapDiaFragment.newInstance(bitmap);
        dfg.show(transaction, "BITMAP_SHOW");
    }

    /*public  Bitmap convertViewToBitmap(View view){
//        view.buildDrawingCache();
//        return view.getDrawingCache();

        RhythmToBitmap rhythmToBitmap = new RhythmToBitmap(this);
        rhythmToBitmap.setDataAndParams((RhythmBasedCompound) model,1080);

        Bitmap bitmap = rhythmToBitmap.makeBitmap();
//        Log.i(TAG, "convertViewToBitmap: bitmap created="+bitmap);
        return bitmap;
    }*/


    public boolean saveBitmapLocalPrivateTest(Bitmap bitmap){
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(date)+".jpg";
        String fileName = "Rhythm_"+timeSuffix;

        try {

            FileOutputStream outStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "失败。", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(this, "成功。", Toast.LENGTH_SHORT).show();
        return true;
    }


    public boolean saveBitmapLocalTest(Bitmap bitmap){
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "saveBitmapLocalTest: mounted.");
        }else {
            Toast.makeText(this, "未检测到SD卡。", Toast.LENGTH_SHORT).show();
            return false;

        }

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(date)+".jpg";
        String fileName = "Rhythm_"+model.getTitle()+"_"+timeSuffix;

        try {
            File outFile = new File(getPublicStorageDir("RhythmSep"),fileName);
            if (!outFile.createNewFile()) {
                Log.e(TAG, "File not created");
                return false;
            }

            FileOutputStream outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "失败。", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(this, "成功。", Toast.LENGTH_SHORT).show();
        return true;
    }



    public File getPublicStorageDir(String dirName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created.99");
        }
        return file;
    }

    @Override
    public void onRtbDataReady() {
        captureActionStep_2();
    }
}
