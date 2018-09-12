package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

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
//            Toast.makeText(getContext(), "列表为空。", Toast.LENGTH_SHORT).show();基类已有提示
            return;
        }

        MyRhythmDbHelper rhythmDbHelper = MyRhythmDbHelper.getInstance(getContext());

        rhythmBasedCompound.setLastModifyTime(System.currentTimeMillis());
//        Log.i(TAG, "checkNotEmptyAndCommit: rbc.cs="+rhythmBasedCompound.getCodeSerialByte()+",csStr="+rhythmBasedCompound.getCodeSerialString());
        int resultNum = rhythmDbHelper.updateRhythmCodesByRid(rhythmBasedCompound);
//        Log.i(TAG, "checkNotEmptyAndCommit: resultNum="+resultNum);

        mListener.onButtonClickingDfgInteraction(RHYTHM_PURE_EDIT_DONE,bundleForSendBack);
        //“全修改”页面会有确认保存，“详情”页面没有保存，因而在此单独存一次cs。
    }
}
