package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
@SuppressWarnings("all")
public class DeleteRhythmDiaFragment extends DialogFragment
        implements View.OnClickListener {
    private static final String TAG = "DeleteGroupDiaFragment";
    private int position = 0;

    private OnGeneralDfgInteraction mListener;

    public DeleteRhythmDiaFragment() {
        // Required empty public constructor
    }


    public static DeleteRhythmDiaFragment newInstance(int position) {
        DeleteRhythmDiaFragment fragment = new DeleteRhythmDiaFragment();
        Bundle args = new Bundle();
        args.putInt("POSITION",position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("POSITION");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_delete_rhythm, container, false);

        TextView cancel = (TextView) rootView.findViewById(R.id.btn_cancel_deleteRhythmDfg);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_deleteRhythmDfg);

        //部分需要添加事件监听
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return rootView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralDfgInteraction) {
            mListener = (OnGeneralDfgInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGeneralDfgInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok_deleteRhythmDfg://删除分组，将位置发回给activity，由调用方负责去DB实际删除,并更新列表显示。
                Bundle data = new Bundle();
                data.putInt("POSITION",position);
                mListener.onButtonClickingDfgInteraction(OnGeneralDfgInteraction.DELETE_RHYTHM,data);
                this.dismiss();
                break;


            case R.id.btn_cancel_deleteRhythmDfg:
                this.dismiss();
                break;

        }

    }
}
