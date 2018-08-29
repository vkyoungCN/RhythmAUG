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
public class RemoveModelFromGroupDiaFragment extends DialogFragment
        implements View.OnClickListener {
    private static final String TAG = "DeleteModelDiaFragment";
    private int model_id = 0;
    private int removeType = 0;
    private int groupId = 0;

    private OnGeneralDfgInteraction mListener;

    public RemoveModelFromGroupDiaFragment() {
        // Required empty public constructor
    }


    public static RemoveModelFromGroupDiaFragment newInstance(int mId, int removeModelType,int groupId) {
        RemoveModelFromGroupDiaFragment fragment = new RemoveModelFromGroupDiaFragment();
        Bundle args = new Bundle();
        args.putInt("MODEL_ID",mId);
        args.putInt("REMOVE_TYPE",removeModelType);
        args.putInt("GROUP_ID",groupId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model_id = getArguments().getInt("MODEL_ID");
            removeType = getArguments().getInt("REMOVE_TYPE");
            groupId = getArguments().getInt("GROUP_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_remove_from_group_model, container, false);

        TextView cancel = (TextView) rootView.findViewById(R.id.btn_cancel_removeFromGroupDfg);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_removeFromGroupDfg);

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
            case R.id.btn_ok_removeFromGroupDfg://删除分组，将位置发回给activity，由调用方负责去DB实际删除,并更新列表显示。
                Bundle data = new Bundle();
                data.putInt("MODEL_ID", model_id);
                data.putInt("GROUP_ID", groupId);
                mListener.onButtonClickingDfgInteraction(removeType,data);
                this.dismiss();
                break;


            case R.id.btn_cancel_removeFromGroupDfg:
                this.dismiss();
                break;

        }

    }
}
