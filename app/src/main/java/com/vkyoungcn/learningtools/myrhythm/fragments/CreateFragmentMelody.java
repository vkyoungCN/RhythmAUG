package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_CREATE_EDITED;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class CreateFragmentMelody extends BaseMelodyEditFragment {
    private static final String TAG = "CreateFragmentMelody";
    private int valueOfSection = 64;


    public CreateFragmentMelody() {
        // Required empty public constructor
    }

    public static CreateFragmentMelody newInstance(RhythmBasedCompound rhythmBasedCompound) {
        //调用方负责传入空的新节奏
        CreateFragmentMelody fragment = new CreateFragmentMelody();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM", rhythmBasedCompound);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rhythmBasedCompound = getArguments().getParcelable("RHYTHM");
//            Log.i(TAG, "onCreate: rhBC got");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);

        rh_editor_EM.setRhythmViewData(rhythmBasedCompound);

        return rootView;
    }


    @Override
    public void checkNotEmptyAndCommit() {
        super.checkNotEmptyAndCommit();

        if(listIsEmpty){
            return;
        }

        mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_EDITED, bundleForSendBack);


    }
}
