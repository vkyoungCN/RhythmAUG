package com.vkyoungcn.learningtools.myrhythm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.RhythmCreateFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.RhythmEditFragment;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;

public class CreateRhythmActivity extends AppCompatActivity {

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



}
