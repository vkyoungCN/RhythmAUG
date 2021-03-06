package com.vkyoungcn.learningtools.myrhythm;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.BaseLyricPhrasesRhythmBasedEditorFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_LYPH_EDIT_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class LyricPhrasesEditActivity extends Activity implements OnGeneralDfgInteraction{
    RhythmBasedCompound rhythmBasedCompound;
    boolean trueIfModifyPrimary;
    int lyricPrId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_phrases_edit);

        rhythmBasedCompound = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
        if(rhythmBasedCompound==null){
            Toast.makeText(this, "空数据传递", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        trueIfModifyPrimary = getIntent().getBooleanExtra("IS_PRIMARY",true);
        if(trueIfModifyPrimary){
            lyricPrId = rhythmBasedCompound.getPrimaryLyricId();
        }else {
            lyricPrId = rhythmBasedCompound.getSecondLyricId();
        }
        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("EDIT_TEXT"));

        if (prev != null) {
            Toast.makeText(this, "Old Dfg still there, removing...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        Fragment editFragment = BaseLyricPhrasesRhythmBasedEditorFragment.newInstance(rhythmBasedCompound,trueIfModifyPrimary);
        transaction.add(R.id.flt_fgContainer_ELPA,editFragment,"EDIT_TEXT").commit();

    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case RHYTHM_PHRASE_EDIT_DONE:
                //准备返回详情页（携带最新修改的信息）。
                Intent intentBack = new Intent();

                //这里与其他资源的修改略有不同，直接在此处提交DB，然后再向调用栈返回数据
                //其他修改多是对资源整体的修改(包括多个字段)，而在此只修改Ly的pr字段。可以处理
                MyRhythmDbHelper rhythmDbHelper = MyRhythmDbHelper.getInstance(this);
                if(lyricPrId==0){
                    //该Lyric不存在，使用的默认id，需要新建
                    Lyric lyric = new Lyric();
                    lyric.setCodeSerialString(data.getString("STRING"));
                    long lastModifyTime = System.currentTimeMillis();
                    lyric.setLastModifyTime(lastModifyTime);
                    if(rhythmDbHelper.createLyric(lyric)==-1){
                        Toast.makeText(this, "DB新增Lyric失败。", Toast.LENGTH_SHORT).show();
                        return;
                    };
                    //然后将ly和rh关联
                    int lyId = rhythmDbHelper.getLyricIdByModifyTime(lastModifyTime);
                    int resultNum = 0;
                    if(trueIfModifyPrimary) {
                        resultNum = rhythmDbHelper.updateRhythmPLyIdByRid(rhythmBasedCompound.getId(), lyId);
                    }else {
                        resultNum = rhythmDbHelper.updateRhythmSLyIdByRid(rhythmBasedCompound.getId(), lyId);
                    }
                    if(resultNum == 0){
                        Toast.makeText(this, "向Rhythm计入新Lyric失败（DB）", Toast.LENGTH_SHORT).show();
                    }

                    //成功，返回。
                    backAndFinish(rhythmDbHelper,intentBack);

                }else {
                    if (rhythmDbHelper.updateLyricCodeStringById(lyricPrId, data.getString("STRING")) != 0) {
                        //成功
                        backAndFinish(rhythmDbHelper,intentBack);

                    } else {
                        //失败
                        Toast.makeText(this, "向DB存入失败。", Toast.LENGTH_SHORT).show();
                        this.finish();
                    }
                }
//                Log.i(TAG, "onButtonClickingDfgInteraction: model="+data.getParcelable("COMPOUND_RHYTHM").toString());
                break;
        }
    }

    void backAndFinish(MyRhythmDbHelper rhythmDbHelper,Intent intentBack){
        RhythmBasedCompound rbcNew = rhythmDbHelper.getCompoundRhythmById(rhythmBasedCompound.getId());//reFetch the rbc again.
        intentBack.putExtra("MODEL", rbcNew);
        setResult(RESULT_CODE_LYPH_EDIT_DONE, intentBack);
        this.finish();
    }

}
