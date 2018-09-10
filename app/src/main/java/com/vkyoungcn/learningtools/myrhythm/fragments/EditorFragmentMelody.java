package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_PURE_EDIT_DONE;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class EditorFragmentMelody extends BaseMelodyEditFragment {
    private static final String TAG = "EditorFragmentMelody";

    public EditorFragmentMelody() {
        // Required empty public constructor
    }

    public static EditorFragmentMelody newInstance(RhythmBasedCompound rhythmBasedCompound) {
        EditorFragmentMelody fragment = new EditorFragmentMelody();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM", rhythmBasedCompound);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            rhythmBasedCompound = getArguments().getParcelable("RHYTHM");
        }
//        Log.i(TAG, "onCreate: MEfg, rbBc="+rhythmBasedCompound.toString());
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        rh_editor_EM.setRhythmViewData(rhythmBasedCompound);
        return rootView;
    }
/*
* getActivity().setResult(701, intentForResult);
                getActivity().finish();
                */


    @Override
    public void checkNotEmptyAndCommit() {
        super.checkNotEmptyAndCommit();
        if (listIsEmpty){
            return;
        }
        mListener.onButtonClickingDfgInteraction(RHYTHM_PURE_EDIT_DONE,bundleForSendBack);

    }
}
