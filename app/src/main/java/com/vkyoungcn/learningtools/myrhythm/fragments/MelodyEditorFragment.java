package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_PURE_EDIT_DONE;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class MelodyEditorFragment extends MelodyBaseEditFragment {
    private static final String TAG = "MelodyEditorFragment";

    public MelodyEditorFragment() {
        // Required empty public constructor
    }

    public static MelodyEditorFragment newInstance(RhythmBasedCompound rhythmBasedCompound) {
        MelodyEditorFragment fragment = new MelodyEditorFragment();
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
//            Log.i(TAG, "onCreate: comRh="+rhythmBasedCompound.toString());
            codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmBasedCompound.getRhythmType());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        rh_editor_ER.setRhythmViewData(rhythmBasedCompound);

        //设监听(只有确定是特别的)
        tv_allConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //编辑模式下，确定后，将修改完毕的ComRh发回

                Bundle data = new Bundle();
                data.putParcelable("COMPOUND_RHYTHM", rhythmBasedCompound);

                mListener.onButtonClickingDfgInteraction(RHYTHM_PURE_EDIT_DONE,data);

                Intent intentForResult = new Intent();
                intentForResult.putExtra("COMPOUND_RHYTHM_RESULT", rhythmBasedCompound);
                getActivity().setResult(701, intentForResult);
                getActivity().finish();
            }
        });

        return rootView;
    }
}
