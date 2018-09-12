package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.adapter.FileNameRvAdapter;

import java.util.ArrayList;

public class SelectFileNameDiaFragment extends DialogFragment {
    private static final String TAG = "ChooseRhythmDiaFragment";
//    private Context mContext;
    private OnGeneralDfgInteraction mListener;
    private TextView tv_confirm;
    private TextView tv_choseName;

    private RecyclerView rv_forChoose;
    private FileNameRvAdapter adp_forChoose;
    private String fileName="";

    private ArrayList<String> chooseList = new ArrayList<>();

    public SelectFileNameDiaFragment() {
        // Required empty public constructor
    }

    public static SelectFileNameDiaFragment newInstance(ArrayList<String> dataForChoose) {
        //注意，有的情况下，已选列表在传递进来时就是有数据的！！
        SelectFileNameDiaFragment fragment = new SelectFileNameDiaFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("FOR_CHOOSE",dataForChoose);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chooseList= getArguments().getStringArrayList("FOR_CHOOSE");
            adp_forChoose = new FileNameRvAdapter(this,chooseList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_file_select, container, false);
        //所有需要用到的8个控件，获取引用
        //【虽然IDE说redundant，但是不转型后面setText时亲测崩溃】
        tv_choseName = (TextView) rootView.findViewById(R.id.tv_choseGpAmount_FS);
        rv_forChoose = (RecyclerView) rootView.findViewById(R.id.rv_forChose_FS);
        rv_forChoose.setHasFixedSize(true);
        rv_forChoose.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_forChoose.setAdapter(adp_forChoose);

        tv_confirm = (TextView)rootView.findViewById(R.id.tv_confirmChose_FS);
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString("FILE_NAME",fileName);
                mListener.onButtonClickingDfgInteraction(OnGeneralDfgInteraction.SELECT_FILE,data);
                dismiss();
            }
        });

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

    /* 回调方法。由APD调用给本Dfg传数据*/
    public void choseClicking(String choseName) {
        fileName = choseName;
        tv_choseName.setText(String.format(getResources().getString(R.string.plh_fileChose),choseName));
    }
}
