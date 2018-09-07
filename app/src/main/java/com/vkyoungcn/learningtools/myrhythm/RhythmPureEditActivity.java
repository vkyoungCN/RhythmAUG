package com.vkyoungcn.learningtools.myrhythm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.MelodyEditorFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_PURE_EDIT_DONE;

public class RhythmPureEditActivity extends AppCompatActivity implements OnGeneralDfgInteraction {
    private static final String TAG = "RhythmPureEditActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pure_rhythm);

        RhythmBasedCompound rhythmBasedCompound = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
//        Log.i(TAG, "RhPureEdt onCreate: rhBc="+rhythmBasedCompound.toString());
        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("EDIT_TEXT"));

        if (prev != null) {
            Toast.makeText(this, "Old Dfg still there, removing...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        Fragment editFragment = MelodyEditorFragment.newInstance(rhythmBasedCompound);
        transaction.add(R.id.flt_fgContainer_ERA,editFragment,"EDIT_TEXT").commit();

    }

    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case RHYTHM_PURE_EDIT_DONE:
                //准备返回节奏详情页（携带最新修改的rh信息）。
                Intent intentBack = new Intent();
                intentBack.putExtra("MODEL",data.getParcelable("COMPOUND_RHYTHM"));
                setResult(RESULT_CODE_RH_PURE_EDIT_DONE);
                this.finish();
                break;
        }
    }


}
