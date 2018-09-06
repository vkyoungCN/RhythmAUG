package com.vkyoungcn.learningtools.myrhythm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.fragments.MelodyCreateFragment;
import com.vkyoungcn.learningtools.myrhythm.helper.CodeSerial_Rhythm;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.DELIVER_ERROR;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_RH_CREATE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_FAILURE;

public class CreateRhythmActivity extends AppCompatActivity implements OnGeneralDfgInteraction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_rhythm);

        int rhythmType = getIntent().getIntExtra("RHYTHM_TYPE",-1);
        if(rhythmType == -1){
            //数据传递出错，节奏类型：-1
            Toast.makeText(this, "数据传递出错，节奏类型：-1", Toast.LENGTH_SHORT).show();
            setResult(DELIVER_ERROR);
            this.finish();
//            return;
        }

        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("CREATE_RH"));

        if (prev != null) {
            Toast.makeText(this, "Old Dfg still there, removing...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }

        //根据上一步所选定的节拍类型，生成带有一个空小节的节奏编码；
        //目前rh类中仅仅持有编码数据一个字段。
        ArrayList<Byte> sectionForAdd = RhythmHelper.getStandardEmptySection(rhythmType);
        RhythmBasedCompound rhythmBasedCompound = new RhythmBasedCompound();
        rhythmBasedCompound.setCodeSerialByte(sectionForAdd);

        Fragment editFragment = MelodyCreateFragment.newInstance(rhythmBasedCompound);
        transaction.add(R.id.flt_fgContainer_CRA,editFragment,"CREATE_RH").commit();
    }


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case RHYTHM_CREATE_EDITED:

                Intent intentToStep_3 = new Intent(this,AddRhythmFinalActivity.class);
                intentToStep_3.putExtra("COMPOUND_RHYTHM",data.getParcelable("COMPOUND_RHYTHM"));
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
            case DELIVER_ERROR:
                setResult(RESULT_CODE_RH_CREATE_FAILURE,data);//这个data应该是null
                this.finish();
                break;
        }
    }
}
