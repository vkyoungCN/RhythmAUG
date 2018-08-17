package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.AddRhythmFinalActivity;
import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.DELETE_MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_FINAL_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class RhythmCreateFragment extends RhythmEditBaseFragment {

    /* 逻辑*/
//    private int valueOfBeat = 16;
    private int valueOfSection = 64;
//    private int sectionSize = 4;

//    private CompoundRhythm compoundRhythm;【新建不需传递cr但需要rhythmType；编辑则需要comRh，然后直接交给editor】
    private int rhythmType;
//    private ArrayList<Byte> codes = new ArrayList<>();//都需要对comRh的编码序列进行编辑
//    private ArrayList<ArrayList<Byte>> codesInSections = new ArrayList<>();//都要使用这个进行处理
    //【RhEditor只是负责显示，逻辑部分其实需要由本fg负责】

//    private int currentSectionIndex = 0;
//    private int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

//    Rhythm rhythm ;

    /* 自定义控件*/
//    private RhythmSingleLineEditor rh_editor_ARA;

    /* 35个控件，其中33个（非edt的）有点击事件*/
    private TextView tv_allConfirm;


    public RhythmCreateFragment() {
        // Required empty public constructor
    }

    public static RhythmCreateFragment newInstance(int rhythmType) {
        RhythmCreateFragment fragment = new RhythmCreateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("RHYTHM_TYPE",rhythmType);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rhythmType = getArguments().getInt("RHYTHM_TYPE",44);

            switch (compoundRhythm.getRhythmType()) {
                case RHYTHM_TYPE_24:
//                valueOfSection = 32;
                    //此时beat值==16无需修改
                    sectionSize = 2;
                    break;
                case RHYTHM_TYPE_34:
//                valueOfSection = 48;
                    sectionSize = 3;
                    break;
                case RHYTHM_TYPE_44:
//                valueOfSection = 64;
                    break;
                case RHYTHM_TYPE_38:
//                valueOfSection = 24;
                    valueOfBeat = 8;
                    sectionSize = 3;
                    break;
                case RHYTHM_TYPE_68:
//                valueOfSection = 48;
                    valueOfBeat = 8;
                    sectionSize = 6;
                    break;
                default:
                    valueOfBeat = 16;
                    sectionSize = 4;
            }

            //初始化初始数据源
            ArrayList<Byte> firstSection = new ArrayList<>();
            for (int i=0;i<sectionSize;i++) {
                firstSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
            }
            codesInSections.add(firstSection);

            compoundRhythm = new CompoundRhythm();
            //暂时只需要节奏欧的两项数据。(其中序列数据等最后编辑完成后再添加，而向RHV传递的编码按列表传递即可)
            compoundRhythm.setRhythmType(rhythmType);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);

        //设监听【其中确定键的监听在基类中未设置】
        tv_allConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在新建状态下，结束后直接前往下一页
                Intent intentToStep_3 = new Intent(getContext(),AddRhythmFinalActivity.class);
                //将修改完成的code编码设置给rhythm类。
                for (ArrayList<Byte> codes_section:codesInSections) {
                    codes.addAll(codes_section);
                }
                compoundRhythm.setRhythmCodeSerial(codes);
                intentToStep_3.putExtra("RHYTHM",compoundRhythm);
                getActivity().startActivity(intentToStep_3);

            }
        });

        rh_editor_ARA.setRhythm(codesInSections,rhythmType,20,22);

        return rootView;
    }

}
