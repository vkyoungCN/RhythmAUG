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
public class RhythmEditFragment extends Fragment implements View.OnClickListener {

    /* 逻辑*/
    private int valueOfBeat = 16;
    private int valueOfSection = 64;
    private int sectionSize = 4;

//    private CompoundRhythm compoundRhythm;【新建不需传递cr但需要rhythmType；编辑则需要comRh，然后直接交给editor】
//    private int rhythmType;
    private ArrayList<Byte> codes = new ArrayList<>();//都需要对comRh的编码序列进行编辑
    private ArrayList<ArrayList<Byte>> codesInSections = new ArrayList<>();//都要使用这个进行处理
    //【RhEditor只是负责显示，逻辑部分其实需要由本fg负责】

    private int currentSectionIndex = 0;
    private int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

//    Rhythm rhythm ;

    /* 自定义控件*/
    private RhythmSingleLineEditor rh_editor_ARA;

    /* 35个控件，其中33个（非edt的）有点击事件*/
    private TextView tv_x0;
    private TextView tv_xb1;
    private TextView tv_xb2;
    private TextView tv_xb3;

    private TextView tv_xp;
    private TextView tv_xpb1;
    private TextView tv_xpb2;

    private TextView tv_xl1;
    private TextView tv_xl2;
    private TextView tv_xl3;

    private TextView tv_xm;
    private TextView tv_xm1;
    private TextView tv_xm2;

    private TextView tv_empty;

    private ImageView imv_x0;
    private ImageView imv_xb1;
    private ImageView imv_xb2;
    private ImageView imv_xb3;

    private ImageView imv_xp;
    private ImageView imv_xpb1;
    private ImageView imv_xpb2;

    private ImageView imv_xl1;
    private ImageView imv_xl2;
    private ImageView imv_xl3;

    private ImageView imv_xm;
    private ImageView imv_xm1;
    private ImageView imv_xm2;

    private EditText edt_xmNum;
    private EditText edt_longCurveNum;

    private ImageView imv_longCurve;

    private TextView tv_longCurve_remove;
    private TextView tv_allConfirm;
    private TextView tv_addSection;

    private TextView tv_lastSection;
    private TextView tv_nextSection;
    private TextView tv_lastUnit;
    private TextView tv_nextUnit;



    public RhythmEditFragment() {
        // Required empty public constructor
    }

    public static RhythmEditFragment newInstance(CompoundRhythm compoundRhythm) {
        RhythmEditFragment fragment = new RhythmEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM",compoundRhythm);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            compoundRhythm = getArguments().getParcelable("RHYTHM");
            if(compoundRhythm == null){
                //传递空数据，代表是新建

            }else {
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
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_rhythm, container, false);

        rh_editor_ARA = rootView.findViewById(R.id.rh_editor_ARA);

        tv_x0 = rootView.findViewById(R.id.tv_x0_ARA);
        tv_xb1 = rootView.findViewById(R.id.tv_xb1_ARA);
        tv_xb2 = rootView.findViewById(R.id.tv_xb2_ARA) ;
        tv_xb3 = rootView.findViewById(R.id.tv_xb3_ARA) ;

        tv_xp = rootView.findViewById(R.id.tv_xp_ARA) ;
        tv_xpb1 = rootView.findViewById(R.id.tv_xpb1_ARA) ;
        tv_xpb2 = rootView.findViewById(R.id.tv_xpb2_ARA) ;

        tv_xl1 = rootView.findViewById(R.id.tv_xl1_ARA) ;
        tv_xl2 = rootView.findViewById(R.id.tv_xl2_ARA) ;
        tv_xl3 = rootView.findViewById(R.id.tv_xl3_ARA) ;

        tv_xm = rootView.findViewById(R.id.tv_xm_ARA) ;
        tv_xm1 = rootView.findViewById(R.id.tv_xm1_ARA) ;
        tv_xm2 = rootView.findViewById(R.id.tv_xm2_ARA) ;

        tv_empty = rootView.findViewById(R.id.tv_empty_ARA);

        imv_x0 = rootView.findViewById(R.id.imv_x0_ARA);
        imv_xb1 = rootView.findViewById(R.id.imv_xb1_ARA) ;
        imv_xb2 = rootView.findViewById(R.id.imv_xb2_ARA) ;
        imv_xb3 = rootView.findViewById(R.id.imv_xb3_ARA) ;

        imv_xp = rootView.findViewById(R.id.imv_xp_ARA) ;
        imv_xpb1 = rootView.findViewById(R.id.imv_xpb1_ARA) ;
        imv_xpb2 = rootView.findViewById(R.id.imv_xpb2_ARA) ;

        imv_xl1 = rootView.findViewById(R.id.imv_xl1_ARA) ;
        imv_xl2 = rootView.findViewById(R.id.imv_xl2_ARA) ;
        imv_xl3 = rootView.findViewById(R.id.imv_xl3_ARA) ;

        imv_xm = rootView.findViewById(R.id.imv_xm_ARA) ;
        imv_xm1 = rootView.findViewById(R.id.imv_xm1_ARA) ;
        imv_xm2 = rootView.findViewById(R.id.imv_xm2_ARA) ;


        edt_xmNum = rootView.findViewById(R.id.edt_xmNum_ARA);
        edt_longCurveNum = rootView.findViewById(R.id.edt_longCurveSpan_ARA);

        imv_longCurve = rootView.findViewById(R.id.imv_longCurveEnd_ARA);

        tv_longCurve_remove =rootView.findViewById(R.id.tv_longCurveRemove_ARA) ;
        tv_allConfirm = rootView.findViewById(R.id.tv_confirmAddRhythm_ARA);
        tv_addSection = rootView.findViewById(R.id.tv_addEmptySection_ARA);

        tv_lastSection = rootView.findViewById(R.id.tv_lastSection_ARA);
        tv_nextSection = rootView.findViewById(R.id.tv_nextSection_ARA);
        tv_lastUnit=rootView.findViewById(R.id.tv_lastUnit_ARA);
        tv_nextUnit = rootView.findViewById(R.id.tv_nextUnit_ARA);


        //设监听
        imv_x0.setOnClickListener(this);
        imv_xb1.setOnClickListener(this);
        imv_xb2.setOnClickListener(this);
        imv_xb3.setOnClickListener(this);

        imv_xp.setOnClickListener(this) ;
        imv_xpb1.setOnClickListener(this);
        imv_xpb2.setOnClickListener(this);

        imv_xl1.setOnClickListener(this);
        imv_xl2.setOnClickListener(this);
        imv_xl3.setOnClickListener(this);

        imv_xm.setOnClickListener(this);
        imv_xm1.setOnClickListener(this);
        imv_xm2.setOnClickListener(this);

//        edt_xmNum.setOnClickListener(this);
//        edt_longCurveNum.setOnClickListener(this);

        imv_longCurve.setOnClickListener(this);

        tv_longCurve_remove.setOnClickListener(this);
        tv_allConfirm.setOnClickListener(this);
        tv_addSection.setOnClickListener(this);

        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);

        tv_empty.setOnClickListener(this);



        //给下方的tv区设置值（对应时值的值的说明区域）
        tv_x0.setText(String.valueOf(valueOfBeat));
        tv_xb1.setText(String.valueOf(valueOfBeat/2));
        tv_xb2.setText(String.valueOf(valueOfBeat/4));
        tv_xb3.setText(String.valueOf(valueOfBeat/8));

        tv_xp.setText(String.valueOf(valueOfBeat+valueOfBeat/2));
        tv_xpb1.setText(String.valueOf(valueOfBeat/2+valueOfBeat/4));
        tv_xpb2.setText(String.valueOf(valueOfBeat/4+valueOfBeat/8));

        tv_xl1.setText(String.valueOf(valueOfBeat*2));
        tv_xl2.setText(String.valueOf(valueOfBeat*3));
        tv_xl3.setText(String.valueOf(valueOfBeat*4));

        tv_xm.setText(String.valueOf(valueOfBeat));
        tv_xm1.setText(String.valueOf(valueOfBeat/2));
        tv_xm2.setText(String.valueOf(valueOfBeat/4));

        //初始化初始数据源
        ArrayList<Byte> firstSection = new ArrayList<>();
        for (int i=0;i<sectionSize;i++) {
            firstSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
        }
        codesInSections.add(firstSection);
//        codes.addAll(firstSection);

        rhythm = new Rhythm();
        //暂时只对节奏数据类设置两项即可。
//        rhythm.setRhythmCodeSerial(codes);
        rhythm.setRhythmType(rhythmType);

        rh_editor_ARA.setRhythm(codesInSections,rhythmType,14,18);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imv_x0_ARA :
                changeCode((byte)valueOfBeat);
                break;
            case R.id.imv_xb1_ARA :
                changeCode((byte)(valueOfBeat/2));
                break;
            case R.id.imv_xb2_ARA :
                changeCode((byte)(valueOfBeat/4));
                break;
            case R.id.imv_xb3_ARA:
                changeCode((byte)(valueOfBeat/8));
                break;
            case R.id.imv_xp_ARA :
                changeCode((byte)(valueOfBeat+valueOfBeat/2));
                break;
            case R.id.imv_xpb1_ARA :
                changeCode((byte)(valueOfBeat/2+valueOfBeat/4));
                break;
            case R.id.imv_xpb2_ARA :
                changeCode((byte)(valueOfBeat/4+valueOfBeat/8));
                break;
            case R.id.imv_xl1_ARA :
                changeCode((byte)(valueOfBeat*2));
                break;
            case R.id.imv_xl2_ARA :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xl3_ARA :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xm1_ARA :
                int fraction = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(8,fraction);
                break;
            case R.id.imv_xm2_ARA :
                int fraction_2 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(9,fraction_2);
                break;
            case R.id.imv_xm_ARA :
                int fraction_3 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(7,fraction_3);
                break;
            case R.id.tv_empty_ARA :
                changeToEmpty();
                break;
            case R.id.imv_longCurveEnd_ARA:
                int spanNum = Integer.parseInt(edt_longCurveNum.getText().toString());
                if(spanNum>7||spanNum<2){
                    //不合理的跨度
                    Toast.makeText(this, "连音跨度不合理，请检查输入", Toast.LENGTH_SHORT).show();
                    break;
                }
                insertCurveEndAfterCurrent(spanNum);
                break;
            case R.id.tv_longCurveRemove_ARA:

                int returnNum = checkAndRemoveLongCurve();
                if(returnNum<0){
                    Toast.makeText(this, "不在连音弧覆盖的范围，没有删除的目标", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_lastSection_ARA:
                moveBox(MOVE_LAST_SECTION);
                break;
            case R.id.tv_lastUnit_ARA:
                moveBox(MOVE_LAST_UNIT);

                break;
            case R.id.tv_nextSection_ARA:
                moveBox(MOVE_NEXT_SECTION);
                break;
            case R.id.tv_nextUnit_ARA:
                moveBox(MOVE_NEXT_UNIT);
                break;

            case R.id.tv_confirmAddRhythm_ARA:
                //直接前往下一页
                Intent intentToStep_3 = new Intent(this,AddRhythmFinalActivity.class);
                //将修改完成的code编码设置给rhythm类。
                for (ArrayList<Byte> codes_section:codesInSections) {
                    codes.addAll(codes_section);
                }
                rhythm.setRhythmCodeSerial(codes);
                intentToStep_3.putExtra("RHYTHM",rhythm);
                this.startActivity(intentToStep_3);

                break;
            case R.id.tv_addEmptySection_ARA:
                //在最后添加一个新的小节，编辑框移动到新小节第一位置
                ArrayList<Byte> newSection = new ArrayList<>();
                for (int i=0;i<sectionSize;i++) {
                    newSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
                }
                codesInSections.add(newSection);
                moveBox(MOVE_FINAL_SECTION);

                break;

        }
    }

    private void changeCode(Byte newCode){
        //确定剩余的可用时值值
        ArrayList<Byte> currentSectionCodes = codesInSections.get(currentSectionIndex);
        int emptyValueInSection = 0;
        for(int i=currentUnitIndexInSection;i<currentSectionCodes.size();i++){
            //只计算本位置后尚余多少时值的空拍
            byte code =currentSectionCodes.get(i);
            if(code< 0 ){
                emptyValueInSection-=code;
            }
        }

        //判断新编码的时值是否符号条件
        int newValue = valueOfBeat;
        if(newCode < 73 && newCode>0){
            newValue = newCode;
        }
        if(emptyValueInSection<newValue){
            Toast.makeText(this, "小节内剩余空时值不足，请考虑删除其他已有音符", Toast.LENGTH_SHORT).show();
        }else {
            if(newCode<=(valueOfBeat+valueOfBeat/2)) {
                //只涉及一位编码
                currentSectionCodes.set(currentUnitIndexInSection, newCode);
            }else if(newCode<valueOfBeat*4) {
                //可能涉及多位编码
                int numbersNeeded = (newCode/valueOfBeat)-1;//影响到几个字符（通常是X---形式，第一个除去，剩余还需几个位置。结果应在1~3）
                //先修改当前音符
                currentSectionCodes.set(currentUnitIndexInSection,(byte)valueOfBeat);

                for(int j=1;j<=numbersNeeded;j++){
                    if(currentSectionCodes.get(currentUnitIndexInSection+j)>0){
                        Toast.makeText(this, "空间不足以安置整个目标时值，截断处理。", Toast.LENGTH_SHORT).show();
                        break;//后面遇到非空位置，退出，且提示。
                    }
                    //只有在后面是空拍或延长音时才能如是设置。
                    currentSectionCodes.set(currentUnitIndexInSection+j,(byte)0);//后面都是-，设为code=0即可.

                }
            }
        }

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }

    private void changeCodeToMultiDivided(int ten, int fraction){
        //确定剩余的可用时值值
        ArrayList<Byte> currentSectionCodes = codesInSections.get(currentSectionIndex);
        int emptyValueInSection = 0;
        for(int i=currentUnitIndexInSection;i<currentSectionCodes.size();i++){
            //只计算本位置后尚余多少时值的空拍
            byte code =currentSectionCodes.get(i);
            if(code< 0 ){
                emptyValueInSection-=code;
            }
        }

        //判断新编码的时值是否符号条件
        if(emptyValueInSection<valueOfBeat){//均分多连音占据一个标准节拍的时值
            Toast.makeText(this, "小节内剩余空时值不足，请考虑删除其他已有音符", Toast.LENGTH_SHORT).show();
        }else {
            byte newCode = (byte) (ten*10+fraction);
            currentSectionCodes.set(currentUnitIndexInSection,newCode);
        }

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }

    private void changeToEmpty(){
        byte b = codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection);
        //变更为同时值大小的
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        codesInThisSection.set(currentUnitIndexInSection,(byte)-b);

        boolean isAllEmpty = true;
        for (byte code:codesInThisSection) {
            if(code>0&&code<112){
                isAllEmpty = false;//只单向设置
                break;
            }
        }
        if(isAllEmpty){
            //本节没有非空的拍子了,应当整节删除
            codesInSections.remove(currentSectionIndex);
            /*
            对索引计数器的调整，由moveB方法负责。
            if(currentSectionIndex>0) {
                currentSectionIndex--;
            }
            currentUnitIndexInSection = 0;*/
            moveBox(DELETE_MOVE_LAST_SECTION);


        }

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }


    private void insertCurveEndAfterCurrent(int span){
        byte code = (byte)(110+span);//这里的跨度从2起，最小是2。
        if((codesInSections.get(currentSectionIndex).size()-1-currentUnitIndexInSection)>0){
            //后面有元素，指定索引插入
            codesInSections.get(currentSectionIndex).add(currentUnitIndexInSection+1,code);
            //据文档：是在指定索引插入元素，该位置原有及后续元素均右移（如果有的话）。

        }else {
            //后面已经没有元素，要附加
            codesInSections.get(currentSectionIndex).add(code);
        }

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }

    private int checkAndRemoveLongCurve(){
        int numForReturn = 0;
        int distanceToCurveEnd = 0;
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        for(int j=currentSectionIndex;j<codesInSections.size()-1;j++) {
            for (int i = currentUnitIndexInSection; i < codesInThisSection.size() - 1; i++) {
                distanceToCurveEnd++;
                byte b = codesInThisSection.get(i);
                if (b > 111) {
                    //是curveEnd
                    if (distanceToCurveEnd <= (b - 110)) {
                        //在有效跨度内，可以移除
                        codesInThisSection.remove(i);
                        //通知到UI改变
                        rh_editor_ARA.codeChangedReDraw();
                    } else {
                        numForReturn = -1;//代表不在弧线覆盖范围内
                    }
                    return numForReturn;
                }
            }
            currentUnitIndexInSection = 0;//本节剩余字符内没有连音弧结束标记，需要跨节寻找，重置节内索引。

        }
        //当所有剩余字符都检索完毕仍没有检索到，则
        numForReturn = -2;
        return numForReturn;

    }

    private void moveBox(int moveType){
        int result = rh_editor_ARA.moveBox(moveType);

        switch (result){
            case 1:
                currentUnitIndexInSection++;
                break;
            case 11:
                currentUnitIndexInSection =0;
                currentSectionIndex++;
                break;
            case -1:
                currentUnitIndexInSection--;
                break;
            case -11:
                currentSectionIndex--;
                currentUnitIndexInSection =(codesInSections.get(currentSectionIndex).size()-1);
                break;
            case -19:
                currentSectionIndex--;
                currentUnitIndexInSection = 0;
                break;
            case -18:
                currentSectionIndex = 0;
                currentUnitIndexInSection = 0;
                break;
            case 20:
                currentSectionIndex = codesInSections.size()-1;
                currentUnitIndexInSection = 0;
        }


    }



}
