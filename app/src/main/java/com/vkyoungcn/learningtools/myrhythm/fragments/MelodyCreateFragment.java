package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_CREATE_EDITED;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class MelodyCreateFragment extends MelodyBaseEditFragment {

    private int valueOfSection = 64;


    public MelodyCreateFragment() {
        // Required empty public constructor
    }

    public static MelodyCreateFragment newInstance(RhythmBasedCompound rhythmBasedCompound) {
        //调用方负责传入空的新节奏
        MelodyCreateFragment fragment = new MelodyCreateFragment();
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
