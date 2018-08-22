package com.vkyoungcn.learningtools.myrhythm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.fragments.RhythmCreateFragment;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_CREATE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_FAILURE;

public class CreateRhythmActivity extends AppCompatActivity implements OnGeneralDfgInteraction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_rhythm);


        int rhythmType = getIntent().getIntExtra("RHYTHM_TYPE",44);

        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("CREATE_RH"));

        if (prev != null) {
            Toast.makeText(this, "Old Dfg still there, removing...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        Fragment editFragment = RhythmCreateFragment.newInstance(rhythmType);
        transaction.add(R.id.flt_fgContainer_CRA,editFragment,"CREATE_RH").commit();

    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case RHYTHM_CREATE_EDITED:
                Intent intentToStep_3 = new Intent(this,AddRhythmFinalActivity.class);
                intentToStep_3.putExtra("COMPOUND_RHYTHM_BUNDLE",data);
                this.startActivityForResult(intentToStep_3,REQUEST_CODE_RH_CREATE);
        break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode){
            case RESULT_CODE_RH_CREATE_DONE:
                setResult(RESULT_CODE_RH_CREATE_DONE,data);//这个data应该是null
                this.finish();
                break;
            case RESULT_CODE_RH_CREATE_FAILURE:
            default:
                setResult(RESULT_CODE_RH_CREATE_FAILURE,data);//这个data应该是null
                this.finish();
                break;
        }
    }
}
