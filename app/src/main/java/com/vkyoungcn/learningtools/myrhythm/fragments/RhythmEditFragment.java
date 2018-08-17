package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.AddRhythmFinalActivity;
import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.DELETE_MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_FINAL_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class RhythmEditFragment extends BaseRhythmEditFragment {


    public RhythmEditFragment() {
        // Required empty public constructor
    }

    public static RhythmEditFragment newInstance(CompoundRhythm compoundRhythm) {
        RhythmEditFragment fragment = new RhythmEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM",compoundRhythm);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            compoundRhythm = getArguments().getParcelable("RHYTHM");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        rh_editor_ARA.setRhythmViewData(compoundRhythm);

        //设监听(只有确定是特别的)
        tv_allConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //编辑模式下，确定后，将修改完毕的ComRh发回
                Intent intentForResult = new Intent();
                intentForResult.putExtra("COMPOUND_RHYTHM_RESULT", compoundRhythm);
                getActivity().setResult(701, intentForResult);
                getActivity().finish();
            }
        });

        return rootView;
    }
}
