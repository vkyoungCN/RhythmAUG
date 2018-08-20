package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
@SuppressWarnings("all")
public class AddRhythmDiaFragment extends DialogFragment
        implements View.OnClickListener {

    private static final String TAG = "AddRhythmDiaFragment";

    private OnGeneralDfgInteraction mListener;
    private RadioGroup rgp_rhythmType;

    public AddRhythmDiaFragment() {
        // Required empty public constructor
    }

    public static AddRhythmDiaFragment newInstance() {
        AddRhythmDiaFragment fragment = new AddRhythmDiaFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_create_rhythm, container, false);
        //所有需要用到的8个控件，获取引用
        //【虽然IDE说redundant，但是不转型后面setText时亲测出错崩溃】
        rgp_rhythmType = (RadioGroup) rootView.findViewById(R.id.rg_type_addRhDfg);
        TextView cancel = (TextView) rootView.findViewById(R.id.btn_cancel_addRhDfg);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_addRhDfg);

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
            case R.id.btn_ok_addRhDfg://创建
                Bundle data = new Bundle();

                //获取设置的节奏类型
                int rhythmType = Rhythm.RHYTHM_TYPE_44;//默认四四拍
                switch(rgp_rhythmType.getCheckedRadioButtonId()){
                    case R.id.rb_24_addRhDfg:
                        rhythmType = Rhythm.RHYTHM_TYPE_24;
                        break;
                    case R.id.rb_34_addRhDfg:
                        rhythmType = Rhythm.RHYTHM_TYPE_34;
                        break;
                    case R.id.rb_44_addRhDfg:
                        rhythmType = Rhythm.RHYTHM_TYPE_44;
                        break;
                    case R.id.rb_38_addRhDfg:
                        rhythmType = Rhythm.RHYTHM_TYPE_38;
                        break;
                    case R.id.rb_68_addRhDfg:
                        rhythmType = Rhythm.RHYTHM_TYPE_68;
                        break;
                }

                //要发回的数据
                data.putInt("RHYTHM_TYPE",rhythmType);
                mListener.onButtonClickingDfgInteraction(OnGeneralDfgInteraction.CREATE_RHYTHM,data);

                dismiss();
                break;
            case R.id.btn_cancel_addRhDfg:
                this.dismiss();
                break;

        }

    }
}
