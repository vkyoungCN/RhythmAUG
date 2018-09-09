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
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmLiteRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmLiteForGpX;

import java.util.ArrayList;

public class ChooseRhythmDiaFragment extends DialogFragment {
    private static final String TAG = "ChooseRhythmDiaFragment";
//    private Context mContext;
    private OnGeneralDfgInteraction mListener;
    private RecyclerView rv_forChoose;
    private RecyclerView rv_choseDone;
    private TextView tv_choseGpAmount;
    private TextView tv_confirm;

    private RhythmLiteRvAdapter adp_forChoose;
    private RhythmLiteRvAdapter adp_choseDone;

    private ArrayList<RhythmLiteForGpX> dataForChoose = new ArrayList<>();
    private ArrayList<RhythmLiteForGpX> choseDoneList = new ArrayList<>();
//    private ArrayList<Integer> choseDoneIds = new ArrayList<>();
    //【关于点击增加时传递什么：方案①传id（弊端：无法转换成index，在已选列表中更新显示困难）
    // ②传index；③传rh（可以避免使用第三个列表；另既然是传地址，所以大小似乎无所谓，选用③方案）】

    public ChooseRhythmDiaFragment() {
        // Required empty public constructor
    }

    public static ChooseRhythmDiaFragment newInstance(ArrayList<RhythmLiteForGpX> dataForChoose, ArrayList<RhythmLiteForGpX> choseDoneList) {
        //注意，有的情况下，已选列表在传递进来时就是有数据的！！
        ChooseRhythmDiaFragment fragment = new ChooseRhythmDiaFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("FOR_CHOOSE",dataForChoose);
        args.putParcelableArrayList("CHOSE_DONE",choseDoneList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataForChoose= getArguments().getParcelableArrayList("FOR_CHOOSE");
            Log.i(TAG, "onCreate: dfC received="+dataForChoose.toString());
            choseDoneList = getArguments().getParcelableArrayList("CHOSE_DONE");

            adp_forChoose = new RhythmLiteRvAdapter(this,dataForChoose,true);
            adp_choseDone = new RhythmLiteRvAdapter(this,choseDoneList,true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_choose_rhythm, container, false);
        //所有需要用到的8个控件，获取引用
        //【虽然IDE说redundant，但是不转型后面setText时亲测崩溃】
        rv_forChoose = (RecyclerView) rootView.findViewById(R.id.rv_forChose_CRHD);
        rv_choseDone = (RecyclerView) rootView.findViewById(R.id.rv_choseDone_CRHD);
        rv_forChoose.setHasFixedSize(true);
        rv_forChoose.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_forChoose.setAdapter(adp_forChoose);
        rv_choseDone.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_choseDone.setAdapter(adp_choseDone);

        tv_choseGpAmount = (TextView) rootView.findViewById(R.id.tv_choseGpAmount_CRHD);
        tv_choseGpAmount.setText(String.format(getResources().getString(R.string.plh_choseRhAmount),choseDoneList.size()));

        tv_confirm = (TextView)rootView.findViewById(R.id.tv_confirmChose_CRHD);
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putParcelableArrayList("RHYTHMS",choseDoneList);
                mListener.onButtonClickingDfgInteraction(OnGeneralDfgInteraction.CHOOSE_RHYTHM_FOR_GROUP,data);
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
    public void choseClicking(RhythmLiteForGpX choseItem, boolean isClickingToAdd) {
        //收到的是被点击项目在数据集中的ID(可直接供调用方使用)
        if (isClickingToAdd) {
            //检索所选项目是否已在“已选定”集合中
            int indexForCheck = choseDoneList.indexOf(choseItem);
            Log.i(TAG, "choseClicking: cDl.siez="+choseDoneList.size());
            if(indexForCheck!=-1){
                Toast.makeText(getActivity(), "点击项目已在选中列表", Toast.LENGTH_SHORT).show();
                return;//已存在
            }
            //未选中的项，加入到选中列表
            int choseDoneListOldSize = choseDoneList.size();
            choseDoneList.add(choseItem);
            adp_choseDone.notifyItemInserted(choseDoneListOldSize);//【待】

        }else {
            //点击移除（点击的是下方已选列表）
            choseDoneList.remove(choseItem);
            adp_choseDone.notifyDataSetChanged();
        }
    }
}
