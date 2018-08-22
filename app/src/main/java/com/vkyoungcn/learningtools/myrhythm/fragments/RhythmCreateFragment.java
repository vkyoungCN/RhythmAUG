package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompounds;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.RHYTHM_CREATE_EDITED;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class RhythmCreateFragment extends RhythmBaseEditFragment {

    private int valueOfSection = 64;

    private int rhythmType;

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

            switch (rhythmBasedCompounds.getRhythmType()) {
                case RHYTHM_TYPE_24:
                    //此时beat值==16无需修改
                    valueOfSection =32;
                    sectionSize = 2;
                    break;
                case RHYTHM_TYPE_34:
                    valueOfSection =48;
                    sectionSize = 3;
                    break;
                case RHYTHM_TYPE_44:
                    break;
                case RHYTHM_TYPE_38:
                    valueOfSection =24;
                    valueOfBeat = 8;
                    sectionSize = 3;
                    break;
                case RHYTHM_TYPE_68:
                    valueOfSection =48;
                    valueOfBeat = 8;
                    sectionSize = 6;
                    break;
                default:
                    valueOfBeat = 16;
                    sectionSize = 4;
                    valueOfSection =64;

            }

            //初始化初始数据源
            ArrayList<Byte> firstSection = new ArrayList<>();
            for (int i=0;i<sectionSize;i++) {
                firstSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
            }
            codesInSections.add(firstSection);

            rhythmBasedCompounds = new RhythmBasedCompounds();
            //暂时只需要节奏欧的两项数据。(其中序列数据等最后编辑完成后再添加，而向RHV传递的编码按列表传递即可)
            rhythmBasedCompounds.setRhythmType(rhythmType);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);

        //设监听【其中确定键的监听在基类中未设置】
        tv_allConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在新建状态下，结束后直接前往下一页（但是请求要发回activity进行，以使startActivity及onResult正确）

                //准备数据
                for (ArrayList<Byte> codes_section:codesInSections) {
                    codes.addAll(codes_section);
                }
                rhythmBasedCompounds.setCodeSerialByte(codes);

                Bundle data = new Bundle();
                data.putParcelable("COMPOUND_RHYTHM", rhythmBasedCompounds);

                mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_EDITED,data);



            }
        });

        rh_editor_ER.setRhythmViewData(codesInSections,rhythmType,null,null,18,22,22);

        return rootView;
    }

}
