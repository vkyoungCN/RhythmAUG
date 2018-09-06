package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.helper.CodeSerial_Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import java.util.ArrayList;
import java.util.List;

import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.DELETE_MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_FINAL_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.helper.CodeSerial_Rhythm.mergeArea;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class MelodyBaseEditFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "RhythmBaseEditFragment";

    public static final int BOX_TYPE_BLUE = 8001;
    public static final int BOX_TYPE_GREEN_START = 8002;
    public static final int BOX_TYPE_GREEN_END = 8003;

    public static final int MOVE_NEXT_UNIT = 2901;
    public static final int MOVE_NEXT_SECTION = 2902;
    public static final int MOVE_LAST_UNIT = 2903;
    public static final int MOVE_LAST_SECTION = 2904;
    public static final int MOVE_FINAL_SECTION = 2905;
    public static final int DELETE_MOVE_LAST_SECTION = 2906;



    /* 逻辑*/
    CodeSerial_Rhythm csRhythmHelper;

    /* 当前选中区域的两端坐标，单code模式下，sI==eI（暂定需要这样判断实际选择区域）*/
    int currentUnitIndex = 0;
    int selectStartIndex = 0;
    int selectEndIndex = 0;
    int realIndex = 0;//临时使用。
    //注意，由于界限索引需要同UI控件交互，需要指示到可绘制的code上（所以126、127、112+都是不能指向的）

    boolean dualForward = true;//选定两个拍子时，存在朝向问题；选定其一为正另一为反。暂定向右为正，默认方向。

    boolean moveAreaStart = false;
    boolean moveAreaEnd = false;

    boolean oneBeatModeOn = false;//转切分、转附点在单、双拍选区下起作用；(或则单点恰=vb)
    boolean dualBeatModeOn = false;//转前后十六尽在单拍模式下起作用。
    //切换到单点（单符）模式、或者移动后置否；进入到选定单拍、双拍后置真。

    int generalCurrentIndex;//不区分哪个指针（仅用在方法中的特殊场景）
    int fakeResultIndex;
    int indexAfterMove;

    int valueOfBeat = 16;
//    int valueOfSection = 64;
//    int sectionSize = 4;
//    int availableValue = 0;
//    int span = 1;
//    int beatEndCursor;//在均分多连音设置前导之检测节拍内可用时值时，生成的副产品（拍内空字符最末索引）；
    // 跨方法使用且返回值已被其他字段占据，暂时采用全局处理。

    //交互发回Activity进行，简化复杂问题。
    OnGeneralDfgInteraction mListener;

    RhythmBasedCompound rhythmBasedCompound;
    //【说明】编辑：传递既有comRh。新建：生成一个空Rh穿进来。
//    int rhythmType;

    /*
    * 说明：
    * 本FG负责编码选框的移动（以及选定所在的一个拍子，选择邻近的两个拍子）；
    * UI控件负责显示
    * 编码类负责处理“修改编码”时的逻辑正确
    * */
    ArrayList<Byte> codes = new ArrayList<>();//都需要对comRh的编码序列进行编辑
//    ArrayList<ArrayList<Byte>> codesInSections = new ArrayList<>();//都要使用这个进行处理
    //【RhEditor只是负责显示，逻辑部分其实需要由本fg负责】

//    int currentSectionIndex = 0;
//    int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

//    Rhythm rhythm ;

    /* Rh控件*/
    RhythmSingleLineEditor rh_editor_EM;

    /* 节奏部分24个tv控件，2个edt控件*/
    TextView tv_merge;

    TextView tv_selectionAreaStart;
    TextView tv_selectionAreaEnd;
    TextView tv_selectionSingleCode;

    TextView tv_selectionBeat;
    TextView tv_selectionDualBeat;

    TextView tv_lastSection;
    TextView tv_nextSection;
    TextView tv_lastUnit;
    TextView tv_nextUnit;

    TextView tv_toZero;

    TextView tv_over2;
    TextView tv_over3;
    TextView tv_toX;
    TextView tv_to0;

    TextView tv_toDvd;
    TextView tv_toHavePoint;
    TextView tv_fwd16;
    TextView tv_rwd16;

    TextView tv_curve;
    TextView tv_copy;

    TextView tv_toBar;
    TextView tv_addPreFix;
    TextView tv_addSection;
    TextView tv_deleteSection;

    EditText edt_topInfo;
    EditText edt_bottomInfo;


    /* 音高输入组件*/
    TextView tv_pitch_1;
    TextView tv_pitch_2;
    TextView tv_pitch_3;
    TextView tv_pitch_4;
    TextView tv_pitch_5;
    TextView tv_pitch_6;
    TextView tv_pitch_7;

    TextView tv_pitch_1s;
    TextView tv_pitch_2s;
    TextView tv_pitch_4s;
    TextView tv_pitch_5s;
    TextView tv_pitch_6s;

    TextView tv_adPoint_1plus;
    TextView tv_adPoint_2plus;
    TextView tv_adPoint_0;
    TextView tv_adPoint_1Neg;
    TextView tv_adPoint_2Neg;


    public MelodyBaseEditFragment() {
        // Required empty public constructor
    }

    public static MelodyBaseEditFragment newInstance(RhythmBasedCompound rhythmBasedCompound) {
        MelodyBaseEditFragment fragment = new MelodyBaseEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM", rhythmBasedCompound);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rhythmBasedCompound = getArguments().getParcelable("RHYTHM");
        this.csRhythmHelper = CodeSerial_Rhythm.getInstance(rhythmBasedCompound.getCodeSerialByte(),rhythmBasedCompound.getRhythmType(), RhythmHelper.calculateValueBeat(rhythmBasedCompound.getRhythmType()));
        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmBasedCompound.getRhythmType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_melody, container, false);

        rh_editor_EM = rootView.findViewById(R.id.rh_editor_EM);

        tv_selectionBeat = rootView.findViewById(R.id.tv_selectBeat) ;
        tv_selectionDualBeat = rootView.findViewById(R.id.tv_selectDualBeat) ;
        tv_selectionSingleCode = rootView.findViewById(R.id.tv_selectSingleCode) ;

        tv_merge = rootView.findViewById(R.id.tv_merge) ;
        tv_selectionAreaStart = rootView.findViewById(R.id.tv_selectAreaStart) ;
        tv_selectionAreaEnd = rootView.findViewById(R.id.tv_selectAreaEnd) ;

        tv_over2 = rootView.findViewById(R.id.tv_over_2) ;
        tv_over3 = rootView.findViewById(R.id.tv_over_3) ;
        tv_toX = rootView.findViewById(R.id.tv_toX) ;

        tv_toZero = rootView.findViewById(R.id.tv_toZero);

        tv_toDvd = rootView.findViewById(R.id.tv_toDv);
        tv_toHavePoint = rootView.findViewById(R.id.tv_toHaveSpot);
        tv_rwd16 = rootView.findViewById(R.id.tv_rwd16);
        tv_fwd16 = rootView.findViewById(R.id.tv_fwd16);

        tv_lastSection = rootView.findViewById(R.id.tv_lastSection_EM);
        tv_nextSection = rootView.findViewById(R.id.tv_nextSection_EM);
        tv_lastUnit=rootView.findViewById(R.id.tv_lastUnit_EM);
        tv_nextUnit = rootView.findViewById(R.id.tv_nextUnit_EM);


        tv_curve =rootView.findViewById(R.id.tv_curve);
        tv_copy =rootView.findViewById(R.id.tv_copy);

        tv_toBar =rootView.findViewById(R.id.tv_bar);
        tv_addPreFix =rootView.findViewById(R.id.tv_prefix);
        tv_addSection =rootView.findViewById(R.id.tv_sectionAdd);
        tv_deleteSection =rootView.findViewById(R.id.tv_sectionMinus);

        edt_topInfo =rootView.findViewById(R.id.tv_infoTop_EM);
        edt_bottomInfo case(R.id.tv_infoBottom_EM);


        //设监听
        tv_merge.setOnClickListener(this);

        tv_selectionAreaStart.setOnClickListener(this);
        tv_selectionAreaEnd.setOnClickListener(this);
        tv_selectionSingleCode.setOnClickListener(this);

        tv_selectionBeat.setOnClickListener(this);
        tv_selectionDualBeat.setOnClickListener(this);

        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);

        tv_over2.setOnClickListener(this);
        tv_over3.setOnClickListener(this);
        tv_toX.setOnClickListener(this);
        tv_toZero.setOnClickListener(this);

        tv_toDvd.setOnClickListener(this);
        tv_toHavePoint.setOnClickListener(this);
        tv_fwd16.setOnClickListener(this);
        tv_rwd16.setOnClickListener(this);

        tv_curve.setOnClickListener(this);
        tv_copy.setOnClickListener(this);

        tv_toBar.setOnClickListener(this);
        tv_addPreFix.setOnClickListener(this);
        tv_addSection.setOnClickListener(this);
        tv_deleteSection.setOnClickListener(this);

//        rh_editor_EM.setRhythm(rhythmBasedCompound);rh编辑器的设置由实现类负责

        return rootView;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_selectBeat:
                //点击后，从默认的选中单个code变为选中所在的Beat
                resetSelectionAreaToTotalBeat();
                oneBeatModeOn = true;
                //通知UI（改框色、改起止范围）
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex);
                break;

            case R.id.tv_selectDualBeat:
                resetSelectionAreaToDualBeat();
                dualBeatModeOn = true;
                //通知自定义UI改用双拍框的颜色样式
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex);
                break;

            case R.id.tv_selectSingleCode:
                moveAreaStart = false;
                moveAreaEnd = false;
                oneBeatModeOn = false;
                dualBeatModeOn = false;

                selectStartIndex = selectEndIndex =currentUnitIndex;
                //通知控件
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex);
                break;

            case R.id.tv_merge:
                //调用编码辅助类的合并方法（暂定只允许对一拍、不足一拍的选区进行合并；跨拍的（含超1拍，2拍多拍的）暂不处理）
                int resultCodeMerge = csRhythmHelper.mergeArea(selectStartIndex,selectEndIndex);
                if(resultCodeMerge<3300){
                    //成功，通知自定义UI改变
                    rh_editor_EM.codeChangedReDraw();
                }else {
                    //失败，给出提示
                    Toast.makeText(getContext(),"失败代码："+resultCodeMerge,Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_selectAreaStart:
                moveAreaStart = true;
                moveAreaEnd = false;
                //然后在move方法中通过布尔情况判定移动的目标是谁。
                break;

            case R.id.tv_selectAreaEnd:
                moveAreaStart = false;
                moveAreaEnd = false;
                break;

            case R.id.tv_over_2:
                if(selectStartIndex!=selectEndIndex){//高兼容，即使不在单点模式，但是二者相等也可。
                    Toast.makeText(getContext(), "仅对单个符号进行操作。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }

                //【注意，单点模式下ss必=se，故!=必是选区模式；而反过来==并不一定是单点模式且ss==se并不一定与cI
                // 一致；逻辑上只要求ss/se不交叉跨越，未要求不跨越cI！】(另外考虑高兼容要求，设计如下逻辑选择实际意图下的索引值)
                if(moveAreaStart||moveAreaEnd){
                    realIndex = selectStartIndex;
                }else {
                    realIndex = currentUnitIndex;
                }
                if(csRhythmHelper.binaryDividingAt(realIndex)<25){
                    //拆分完成，应刷新控件
                    rh_editor_EM.codeChangedReDraw();
                }//否则无反映
                break;

            case R.id.tv_over_3:
                //改均分多连音
                //只要①不跨拍子，②选区时值符合，实际上度允许操作【暂定】
                if(!checkAreaInsideBeat(selectStartIndex,selectEndIndex)){
                    //跨拍子，不符合要求；
                    return;
                }
                //在拍子内部，还需要判断选区是否在弧下；时值是否合理，合理则替换（相关逻辑由编码类负责）
                if(csRhythmHelper.replaceAreaToMultiDivided(selectStartIndex,selectEndIndex)<25){
                    rh_editor_EM.codeChangedReDraw();
                }

                break;

            case R.id.tv_toX:
                if(selectStartIndex!=selectEndIndex){//高兼容，即使不在单点模式，但是二者相等也可。
                    Toast.makeText(getContext(), "仅对单个符号进行操作。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(moveAreaStart||moveAreaEnd){
                        realIndex = selectStartIndex;
                    }else {
                        realIndex = currentUnitIndex;
                    }
                    if(csRhythmHelper.changeCodeToXAt(realIndex)<25){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_toZero:
                if(selectStartIndex!=selectEndIndex){//高兼容，即使不在单点模式，但是二者相等也可。
                    Toast.makeText(getContext(), "仅对单个符号进行操作。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(moveAreaStart||moveAreaEnd){
                        realIndex = selectStartIndex;
                    }else {
                        realIndex = currentUnitIndex;
                    }
                    if(csRhythmHelper.changeCodeToZeroAt(realIndex)<25){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_toDv:
                //选定的拍子或区域转为切分（仅在：①选区是整拍、双拍；②单点选择恰=vb 时有效果）
                if(!oneBeatModeOn&&!dualBeatModeOn&&checkCodeValue(codes.get(currentUnitIndex))!=valueOfBeat){
                    Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(csRhythmHelper.changeAreaToDvd(selectStartIndex,selectEndIndex)<33){//允许双整拍
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_toHaveSpot:
                if(!oneBeatModeOn&&!dualBeatModeOn&&checkCodeValue(codes.get(currentUnitIndex))!=valueOfBeat){
                    Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(csRhythmHelper.changeAreaToHaveSpot(selectStartIndex,selectEndIndex)<33){//允许双整拍
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_rwd16:
                if(!oneBeatModeOn&&checkCodeValue(codes.get(currentUnitIndex))!=valueOfBeat){
                    Toast.makeText(getContext(), "需选定整拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                    return;//合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(csRhythmHelper.changeAreaToRwd16(selectStartIndex,selectEndIndex)<25){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_fwd16:
                if(!oneBeatModeOn&&checkCodeValue(codes.get(currentUnitIndex))!=valueOfBeat){
                    Toast.makeText(getContext(), "需选定整拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                    return;//合法情形均不满足，不能执行操作
                }else {
                    //选区或单点之一符合，统一暂按选区模式转变（兼容）
                    if(csRhythmHelper.changeAreaToFwd16(selectStartIndex,selectEndIndex)<25){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_lastSection_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_LAST_SECTION);
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                break;

            case R.id.tv_nextSection_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_NEXT_SECTION);
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                break;

            case R.id.tv_lastUnit_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_LAST_UNIT);
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                break;

            case R.id.tv_nextUnit_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_NEXT_UNIT);
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                break;


            case R.id.tv_curve:
                //单点模式，只能删除：（如果有）则取消当前上方的连音弧
                //选区模式（且s不等e）只能新增；如果有，不允许增加
                if(selectStartIndex!=selectEndIndex){
                    if(csRhythmHelper.checkAreaUnderCurve(selectStartIndex,selectEndIndex)){
                        Toast.makeText(getContext(), "选区模式仅支持新增。且暂不允许层叠。用单点模式删除。", Toast.LENGTH_SHORT).show();
                    }else {
                        if(csRhythmHelper.addCurveForArea(selectStartIndex,selectEndIndex)<126){
                            rh_editor_EM.codeChangedReDraw();
                        }
                    }
                }else {
                    //实质单点模式（但是此时选区的坐标和单点坐标可能不一致，判断到底是谁）
                    if(moveAreaStart||moveAreaEnd){
                        realIndex = selectStartIndex;//决定暂时允许对选区实际单点的情形下执行删除操作。
                    }else {
                        realIndex = currentUnitIndex;
                    }
                    if(csRhythmHelper.checkCurveCovering(realIndex)){
                        //删除
                        if(csRhythmHelper.removeCurveOver(realIndex)<3000){
                            rh_editor_EM.codeChangedReDraw();
                        }
                    }else {
                        Toast.makeText(getContext(), "上方无连音弧。", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.tv_copy:
                Toast.makeText(getContext(), "功能开发中……", Toast.LENGTH_SHORT).show();
            case R.id.tv_bar:
                //仅限：左侧是X或- ；区域单个整拍、单点单个整拍 【实际单点时的坐标和手动单点持有的坐标可能不一致，采用实际坐标】
                if(!oneBeatModeOn && csRhythmHelper.getAreaValue(selectStartIndex,selectEndIndex)!=valueOfBeat){
                    Toast.makeText(getContext(), "选定整拍才可执行转换。", Toast.LENGTH_SHORT).show();
                    return;//合法情形均不满足，不能执行操作
                }
                if(!csRhythmHelper.checkLeftRealIsXOrBar(selectStartIndex)){
                    //不满足，不可修改
                    Toast.makeText(getContext(), "左侧实际音符需要是X或-才可执行。", Toast.LENGTH_SHORT).show();

                }else {
                    if(csRhythmHelper.changeAreaToBar(selectStartIndex,selectEndIndex)==0){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_prefix:

            case R.id.tv_sectionAdd:
            case R.id.tv_sectionMinus:

            case R.id.tv_longCurveRemove_ER:

                int returnNum = checkAndRemoveLongCurve(true);
                if(returnNum<0){
                    Toast.makeText(getContext(), "不在连音弧覆盖的范围，没有删除的目标", Toast.LENGTH_SHORT).show();
                }
                break;

           /* case R.id.tv_confirmAddRhythm_ER:
                //子类行为不同
                // 新建：直接前往下一页
                // 编辑，返回上一页（可能还需要上一页是startAFResult的调用方式）
                【由子类负责以匿名接口方式实现】

                break;*/
            case R.id.tv_addEmptySection_ER:
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



    private boolean checkAreaInsideBeat(int startIndex, int endIndex){
        for(int i=startIndex;i<=endIndex;i++){
            if(codes.get(i)==126){
                return false;
            }
        }
        return true;
    }


    /* 返回的值是移动后的索引值（或者在移动无效时是移动前的值），但都是当前模式对应的正确的索引项的值
    * 增加一个返回值的原因是便于后续的利用（不必再判断模式）
    * */
    private int checkMoveModeAndSetResultIndex(int resultIndex) {
        if (moveAreaStart) {
            //选区起端移动模式
            if(resultIndex>selectEndIndex){
                Toast.makeText(getContext(), "不允许交叉起止端。", Toast.LENGTH_SHORT).show();
                return selectStartIndex;//选区的起端不允许越过选区末端(仍旧返回旧值)
            }
            selectStartIndex = resultIndex;
            return selectStartIndex;//返回新值
        } else if (moveAreaEnd) {
            //选区末端移动模式
            if(resultIndex<selectStartIndex){
                Toast.makeText(getContext(), "不允许交叉起止端。", Toast.LENGTH_SHORT).show();
                return selectEndIndex;//不允许交叉起止端。返回的是旧值
            }
            selectEndIndex = resultIndex;
            return selectEndIndex;
        } else {
            //单点移动模式
            selectStartIndex = selectEndIndex = currentUnitIndex = resultIndex;
            return currentUnitIndex;
        }
    }

    private int checkMoveModeAndGetCurrentIndex(){
        if (moveAreaStart) {
            //选区起端移动模式
            return selectStartIndex;
        } else if (moveAreaEnd) {
            //选区末端移动模式
            return selectEndIndex;
        } else {
            //单点移动模式
            selectStartIndex = selectEndIndex = currentUnitIndex = resultIndex;
        }

    }

    /* 选定当前光标所在的单个整拍子，将区域选定标记的起止坐标记录器设置为结果值*/
    private void resetSelectionAreaToTotalBeat(){

        selectStartIndex = findBeatStartIndex();
        selectEndIndex = findBeatEndIndex(currentUnitIndex);


    }

    /* 选定当前光标所在的两个整拍子，将区域选定标记的起止坐标记录器设置为结果值*/
    private void resetSelectionAreaToDualBeat(){
        if(!dualForward){
            //执行向右扩展
            //切换扩展方向（从当前光标所在拍开始）
            dualForward = true;

            selectStartIndex= findBeatStartIndex();//区域开头仍然是本拍拍首
            int tempAreaEndIndex = findBeatEndIndex(currentUnitIndex);//拍尾要选用下一拍（除非跨节（不允许））
            int fakeNextBeatStartIndex = isNextBeatInSameSection(tempAreaEndIndex);
            if(fakeNextBeatStartIndex == -1||fakeNextBeatStartIndex==tempAreaEndIndex){
                //会跨节或已到序列尾部，只实际选定单拍
                selectEndIndex = tempAreaEndIndex;
            }else {
                //选定下一拍的拍尾
                selectEndIndex = findBeatEndIndex(fakeNextBeatStartIndex);
            }
        }
    }

    /* 为了能利用直接获取下拍的起坐标，跨节时返回-1。其余返回下节节首坐标
    * 如果返回值与当前节的拍末坐标一致，则说明后方不足一拍*/
    int isNextBeatInSameSection(int currentBeatEndIndex){
        int nextBeatStartIndex = currentBeatEndIndex;
        for(int i=currentBeatEndIndex;i<codes.size();i++){
            if(codes.get(i)<111){
                nextBeatStartIndex = i;
                break;
            }
        }
        for(int k=nextBeatStartIndex;k>=currentBeatEndIndex;k--){
            if(codes.get(k)==127)
                return -1;
        }
        return nextBeatStartIndex;

    }


    /* 找到当前光标所在拍子的前界限*/
    private int findBeatStartIndex(){
        for(int i=currentUnitIndex;i>=0;i--){
            byte b1 = codes.get(i);
            if((b1==127)||(b1==126)){
                //（上一拍的结尾，上一节的结尾）
                return i+1;
            }
            if(i==0){
                //遍历到头（本身位于首拍）
                return i;
            }
        }
        return -1;
    }



    /* 找到当前光标所在拍子的后界限*/
    //在此“多此一举”地传入一个与全局变量同名的变量原因：方法的另一处应用场景中，传入的不是这个全局量而是另外的量，
    // 因而必须设置一个形参。
    private int findBeatEndIndex(int currentUnitIndex){
        for(int k=currentUnitIndex;k<codes.size();k++){
            byte b2 = codes.get(k);
            if(b2 == 126){
                //末尾即使在节尾也必然要存在126符号，不需考虑127
                if(codes.get(k-1)>111){
                    //剔除连音弧尾
                    return k-2;
                }
                return k-1;
                //【126/127暂定不计入当前音符范围，连音弧尾标记也不计入。】
            }
        }
        return  -1;
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

    int checkCodeValue(byte code) {
        if (code > 111) {
            //上弧连音专用符号，不记时值
            return 0;//但是由于实际上不会选中结束符，因而这种状态是错误的
        }else if(code>92){
            return 16;//三类均分多连音的时值的定值，不随内容数量改变，也与vb无关。
        }else if(code>82){
            return 8;
        }else if (code > 72) {
            //时值计算
            return 4;
        } else if (code > 0) {
            //时值计算
            return code;
        }else if(code==0){
            return valueOfBeat;
        }else {//b<0
            //时值计算：空拍带时值，时值绝对值与普通音符相同
            return -code;
        }
    }


    int checkAndRemoveLongCurve(boolean notifyUI){
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
                        if(notifyUI){
                            //需要刷新（手动点击取消连音弧按钮时调用）
                            //当由其他方法调用时，由于调用方本身通常自带刷新逻辑，因而不必刷新。
                            rh_editor_EM.codeChangedReDraw(codesInSections);
                        }
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



    private boolean checkIsLastRealUnit(int currentIndex){
        for(int i=currentIndex; i<codes.size();i++){
            if(codes.get(i)<110){
                //其后仍然 有实际音符
                return false;
            }
        }
        //循环完了都没找到则是最后一个了
        return true;
    }

    private boolean checkIsFirstRealUnit(int currentIndex){
        for(int i=currentIndex; i>0;i--){
            if(codes.get(i)<110){
                //左侧仍然 有实际音符
                return false;
            }
        }
        //循环完了都没找到
        return true;
    }


    private int getNextRealUnitIndex(int currentIndex){
        for(int i=currentIndex; i<codes.size();i++){
            if(codes.get(i)<110){
                //其后的首个实际音符
                return i;
            }
        }
        //循环完了都没找到则是最后一个了
        return -1;
    }

    private int getLastRealUnitIndex(int currentIndex){
        for(int i=currentIndex; i>0;i--){
            if(codes.get(i)<110){
                //左侧首个相邻的实际音符
                return i;
            }
        }
        //循环完了都没找到则是最后一个了
        return -1;
    }

    private int getRealUnitIndexOfNextSection(int currentIndex) {
        boolean afterSelf127 = false;//越过本节的节尾127后，置真
        for (int i = currentIndex; i < codes.size(); i++) {
            if (codes.get(i) == 127 && !afterSelf127) {
                //本节结尾
                afterSelf127 = true;
            } else if (afterSelf127&&codes.get(i)<110) {
                //已跨节，且首个实际音符。
                return i;
            }
        }

        return -1;
    }

    private boolean checkIsLastSection(int currentIndex){
        boolean afterSelf127 = false;//越过本节的节尾127后，置真
        for(int i=currentIndex; i<codes.size();i++){
            if(codes.get(i)==127&&!afterSelf127){
                //本节结尾
                afterSelf127 = true;
            }else if(codes.get(i)==127&&afterSelf127){
                //
                return false;
            }
        }
        //循环完了都没找到则是最后一个了
        return true;
/*
        int endIndexOfThisSection = -1;
        for(int i=generalCurrentIndex; i<codes.size();i++){
            if(codes.get(i)==127){
                //本节结尾
                endIndexOfThisSection = i;
                break;
            }
        }
        //本节一定有节尾127编码，否则是错误的。（是否要考虑错误处理？）
        for(int i=endIndexOfThisSection; i<codes.size();i++){
            if(codes.get(i)<110){
                //有普通音符
                return false;
            }
        }
*/



    }

    private boolean checkIsFirstSection(int currentIndex) {
        for (int i = currentIndex; i >0; i--) {
            if (codes.get(i) == 127) {
                //只要左侧还有127则表明不是首节
                return false;
            }
        }
        //循环完了都没找到则是首个节
        return true;
    }


    private int getLastRealUnitIndexOfLastSection(int currentIndex) {
        boolean passed127 = false;
//        boolean passed127By2 = false;
        for (int i = currentIndex; i>0; i--) {
            if (codes.get(i) == 127&&!passed127) {
                //左侧紧邻小节的末尾
                passed127 = true;
            } else if (passed127 && codes.get(i)<110) {
                //是左侧小节的末尾实际音符【如果要找该节的节首，比较复杂；还要考虑是否全编码首位问题等，以简化方案执行】
                return i;
//                passed127By2 = true;
            }
        }
        return -1;
    }


    public int moveBox(int currentIndex,int moveType){
        //移动完毕后，先经过检查（在调用方法中进行）再更新自定义UI；
        switch (moveType){
            case MOVE_NEXT_UNIT:
                if(checkIsLastRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最后", Toast.LENGTH_SHORT).show();
                    return 3401; //已在最后，不移动
                }else {
                    return getNextRealUnitIndex(currentIndex);

                }
            case MOVE_NEXT_SECTION:
                if(checkIsLastSection(currentIndex)){
                    //已在最后，不移动
                    Toast.makeText(getContext(), "已在最后一节", Toast.LENGTH_SHORT).show();
                    return 0 ;
                }else {
                    return getRealUnitIndexOfNextSection(currentIndex);

                }
            case MOVE_LAST_UNIT:
                if(checkIsFirstRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最前", Toast.LENGTH_SHORT).show();
                    return 3042;//已在最前，不移动

                }else{
                    return getLastRealUnitIndex(currentIndex);
                }

            case MOVE_LAST_SECTION:
                if(checkIsFirstSection(currentIndex)){
                    //已在最前节，不移动
                    Toast.makeText(getContext(), "已在第一小节", Toast.LENGTH_SHORT).show();
                    return 3043 ;
                }else{
                    return getLastRealUnitIndexOfLastSection(currentIndex);
                }
/*
            case DELETE_MOVE_LAST_SECTION:
                if(boxSectionIndex ==0){
                    //已在最前（删除的是第一小节）,小节索引不需改变，只改单元索引。
                    boxUnitIndex =0;
                    invalidate();

                    return -18;
                }else {
                    //跨节移到上节首
                    boxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size() - 1;
                    boxUnitIndex = 0;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();

                    return -19;
                }
            case MOVE_FINAL_SECTION:
                //移到最后一节的节首（用于添加一个新的小节后）
                boxSectionIndex = maxSectionIndex;//基于“引用数据源自动修改”的设想
                maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;

                boxUnitIndex = 0;

                //移动到中心（如果超出绘制区）
                checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));
                invalidate();

                return 20;

*/

        }
        return 0;
    }


}
