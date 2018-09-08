package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_CREATE_DONE;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
@SuppressWarnings("all")
public class FinalAddRhythmDiaFragment extends DialogFragment
        implements View.OnClickListener {

    private static final String TAG = "FinalAddRhythmDiaFragment";

    boolean isSuccessed = false;

    private OnGeneralDfgInteraction mListener;
    private RadioGroup rgp_rhythmType;

    public FinalAddRhythmDiaFragment() {
        // Required empty public constructor
    }

    public static FinalAddRhythmDiaFragment newInstance(boolean isSuccessed) {
        FinalAddRhythmDiaFragment fragment = new FinalAddRhythmDiaFragment();
        Bundle args = new Bundle();
        args.putBoolean("SUCCESS",isSuccessed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSuccessed = getArguments().getBoolean("SUCCESS",false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_report_added_rhythm, container, false);
        TextView tv_result = (TextView) rootView.findViewById(R.id.tv_result_FarDfg);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_finaladdRhDfg);

        if(!isSuccessed){
            //未成功时，文本要设置为失败。
            tv_result.setText(getResources().getString(R.string.add_rhythm_finish_false));
        }

        //部分需要添加事件监听
        confirm.setOnClickListener(this);

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
            case R.id.btn_ok_finaladdRhDfg:
                //确定了直接返回就好了
//                Log.i(TAG, "onClick: dft for back="+RHYTHM_CREATE_DONE);
//                Log.i(TAG, "onClick: mListener="+mListener.toString());
                mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_DONE,null);
                this.dismiss();
                break;
        }
    }
}
