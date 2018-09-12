package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.helper.CodeSerial_Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;

import java.util.ArrayList;

/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class BaseMelodyEditFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "BaseMelodyEditFragment";

    public static final int BOX_TYPE_BLUE = 8001;
    public static final int BOX_TYPE_GREEN_START = 8002;
    public static final int BOX_TYPE_GREEN_END = 8003;

    public static final int MOVE_NEXT_UNIT = 2901;
    public static final int MOVE_NEXT_SECTION = 2902;
    public static final int MOVE_LAST_UNIT = 2903;
    public static final int MOVE_LAST_SECTION = 2904;
    public static final int MOVE_FINAL_SECTION = 2905;
    public static final int DELETE_MOVE_LAST_SECTION = 2906;
    public static final int MOVE_ADJACENT_SECTION = 2907;

    /*
     * 说明：本FG、rh-UI、csRh编码事务类、rh辅助类四者配合完成编辑与显示任务
     * 相关职责的划分尚未完全明细【待】
     * */
    RhythmBasedCompound rhythmBasedCompound;
    ArrayList<Byte> codeSerial;//都需要对comRh的编码序列进行编辑
    int valueOfBeat;

    //交互发回Activity进行，简化复杂问题。
    OnGeneralDfgInteraction mListener;
    Bundle bundleForSendBack;
    boolean listIsEmpty = false;

    /* Rh控件*/
    RhythmSingleLineEditor rh_editor_EM;


    /* 辅助类*/
    CodeSerial_Rhythm csRhythmHelper;

    /* 当前选中区域的两端坐标，单code模式下，sI==eI（暂定需要这样判断实际选择区域）*/
    int currentUnitIndex = 0;
    int selectStartIndex = 0;
    int selectEndIndex = 0;
    int realIndex = 0;//临时使用。
    //注意，由于界限索引需要同UI控件交互，需要指示到可绘制的code上（所以126、127、112+都是不能指向的）

    boolean dualForward = true;//选定两个拍子时，存在朝向问题；选定其一为正另一为反。暂定向右为正，默认方向。

    boolean freeAreaModeOn = false;//必须多设这个变量才能正确判断【？】
    boolean moveAreaStart = false;//为逻辑安全起见，只要是手动移动，下方1BMO,2BMO自动关闭。
    // （否则需要每步移动都检测是否恰好是1/2拍）
    boolean moveAreaEnd = false;
    boolean oneBeatModeOn = false;//转切分、转附点在单、双拍选区下起作用；(或则单点恰=vb)
    boolean dualBeatModeOn = false;//转前后十六尽在单拍模式下起作用。
    //切换到单点（单符）模式、或者移动后置否；进入到选定单拍、双拍后置真。

    boolean sectionAddToEnd = false;

    int generalCurrentIndex;//不区分哪个指针（仅用在方法中的特殊场景）
    int fakeResultIndex;
    int indexAfterMove;



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
    TextView tv_sectionAddToEnd;
    TextView tv_deleteSection;

    TextView tv_allConfirm;

    TextView tv_topInfo;
    TextView tv_bottomInfo_rhType;
    TextView tv_bottomInfo_cursor;
    TextView tv_bottomInfo_Acursor;
    TextView tv_bottomInfo_newSectionToEnd;

    /* 音高输入组件*/
    /*TextView tv_pitch_1;
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
*/

    public BaseMelodyEditFragment() {
        // Required empty public constructor
    }

    public static BaseMelodyEditFragment newInstance(RhythmBasedCompound rhythmBasedCompound) {
        BaseMelodyEditFragment fragment = new BaseMelodyEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM", rhythmBasedCompound);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rhythmBasedCompound = getArguments().getParcelable("RHYTHM");
        if(rhythmBasedCompound==null){
            Toast.makeText(getContext(), "传递的节奏数据为空，退出", Toast.LENGTH_SHORT).show();
            getActivity().finish();//【这样退出是否正确？】
            return;
        }
        this.csRhythmHelper = new CodeSerial_Rhythm(rhythmBasedCompound);
        this.codeSerial = rhythmBasedCompound.getCodeSerialByte();
        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmBasedCompound.getRhythmType());
//        Log.i(TAG, "onCreate: rhBc type="+rhythmBasedCompound.getRhythmType());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_melody2, container, false);

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
        tv_sectionAddToEnd =rootView.findViewById(R.id.tv_atEnd);
        tv_deleteSection =rootView.findViewById(R.id.tv_sectionMinus);
        tv_allConfirm =rootView.findViewById(R.id.tv_confirmAddRhythm_EM);

        tv_topInfo =rootView.findViewById(R.id.tv_infoTop_EM);
        tv_bottomInfo_rhType = rootView.findViewById(R.id.tv_infoBottom_rtp_EM);
        tv_bottomInfo_rhType.setText(RhythmHelper.getStrRhythmType(rhythmBasedCompound.getRhythmType()));
        tv_bottomInfo_cursor = rootView.findViewById(R.id.tv_infoBottom_cI_EM);
        tv_bottomInfo_Acursor = rootView.findViewById(R.id.tv_infoBottom_aI_EM);
        tv_bottomInfo_newSectionToEnd = rootView.findViewById(R.id.tv_infoBottom_secAddEnd_EM);
        tv_bottomInfo_cursor.setText(String.format(getResources().getString(R.string.plh_currentIndex),
                rh_editor_EM.getBlueBoxSectionIndex(),rh_editor_EM.getBlueBoxUnitIndex()));//初始

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
        tv_sectionAddToEnd.setOnClickListener(this);
        tv_deleteSection.setOnClickListener(this);
        tv_allConfirm.setOnClickListener(this);
//        rh_editor_EM.setRhythm(rhythmBasedCompound);rh编辑器的设置由实现类负责

        return rootView;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_selectBeat:

                tv_topInfo.setText("选中整拍（可转切分、附点、前/后16）");
                //点击后，从默认的选中单个code变为选中所在的Beat
                //如果当前是选区模式，将按照其起始位置进行选取
                resetSelectionAreaToTotalBeat();
                oneBeatModeOn = true;
                dualBeatModeOn = false;
                if(selectStartIndex!=selectEndIndex){
                    freeAreaModeOn = true;
                }

                //通知UI（改框色、改起止范围）
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                checkMoveModeAndSetBottomInfo();
                break;

            case R.id.tv_selectDualBeat:
                //【注意，由于双拍存在方向切换，为排除错误，同时简化逻辑，在此要求只能基于单点坐标选取】
                tv_topInfo.setText("选中双整拍（可转切分、附点）");
//                Log.i(TAG, "onClick: select Dual Beats");
                resetSelectionAreaToDualBeat();
                dualBeatModeOn = true;
                oneBeatModeOn = false;
               /* if(selectStartIndex!=selectEndIndex){
                    freeAreaModeOn = true;这样是蓝色
                }*/
                freeAreaModeOn = true;//单框单拍也是绿色
                //通知自定义UI改用双拍框的颜色样式
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                checkMoveModeAndSetBottomInfo();
                break;

            case R.id.tv_selectSingleCode:
                tv_topInfo.setText("单点模式");
                moveAreaStart = false;
                moveAreaEnd = false;
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                freeAreaModeOn = false;

                selectStartIndex = selectEndIndex =currentUnitIndex;
                //通知控件
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                checkMoveModeAndSetBottomInfo();
                break;


            case R.id.tv_selectAreaStart:
                tv_topInfo.setText("选区-起点");
                if(!freeAreaModeOn){
                    selectStartIndex = selectEndIndex =currentUnitIndex;
                    freeAreaModeOn = true;
                }
                /*if(!moveAreaStart&&!moveAreaEnd){
                    //从单点模式切换而来
                    selectStartIndex = selectEndIndex =currentUnitCsIndex;
                }else if(!moveAreaStart&&moveAreaEnd){
                    //从终点模式切换而来（），保留原终点位置；只改变原起点
                    selectStartIndex = currentUnitCsIndex;
                }*///剩余一种情形是重复点击
                moveAreaStart = true;
                moveAreaEnd = false;

                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);//可能只是换了个框框颜色
                checkMoveModeAndSetBottomInfo();
                //然后在move方法中通过布尔情况判定移动的目标是谁。
                break;

            case R.id.tv_selectAreaEnd:


                tv_topInfo.setText("选区-终点");
                if(!freeAreaModeOn){
                    selectStartIndex = selectEndIndex =currentUnitIndex;
                    freeAreaModeOn = true;
                }
                /*if(!moveAreaEnd&&!moveAreaStart){
                    //从单点模式切换而来
                    selectStartIndex = selectEndIndex =currentUnitCsIndex;
                }else if(!moveAreaEnd&&moveAreaStart){
                    selectEndIndex = currentUnitCsIndex;

                }*///剩余一种情形是重复点击

                moveAreaStart = false;
                moveAreaEnd = true;
                rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                checkMoveModeAndSetBottomInfo();
                break;


            case R.id.tv_lastSection_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_LAST_SECTION);
                if(fakeResultIndex == -1){
                    return;
                }

                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_nextSection_EM:
//                Log.i(TAG, "onClick: next S");
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_NEXT_SECTION);
                if(fakeResultIndex == -1){
                    return;
                }
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_lastUnit_EM:
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_LAST_UNIT);
                if(fakeResultIndex == -1){
                    return;
                }
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_nextUnit_EM:
//                Log.i(TAG, "onClick: next U");
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                generalCurrentIndex = checkMoveModeAndGetCurrentIndex();
//                Log.i(TAG, "onClick: current index="+generalCurrentIndex);
                fakeResultIndex = moveBox(generalCurrentIndex,MOVE_NEXT_UNIT);
                if(fakeResultIndex == -1){
                    return;
                }
                indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);
                rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_over_2:
                if(selectStartIndex!=selectEndIndex){//高兼容，即使不在单点模式，但是二者相等也可。
                    Toast.makeText(getContext(), "仅对单个符号进行操作。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }

                /*【注意，单点模式下ss必=se，故!=必是选区模式；而反过来==并不一定是单点模式且ss==se并不一定与cI
                 一致；逻辑上只要求ss/se不交叉跨越，未要求不跨越cI！】(另外考虑高兼容要求，设计如下逻辑选择实际意图下的索引值)*/
                if(freeAreaModeOn){//【现在已经看不懂折断设计的意义了？！但是又不敢轻易的改】
                    realIndex = selectStartIndex;
                }else {
                    realIndex = currentUnitIndex;
                }

                if(csRhythmHelper.checkCurveCovering(realIndex)){
                    Toast.makeText(getContext(), "请先取消连音弧。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(csRhythmHelper.checkIsBar(realIndex)){
                    Toast.makeText(getContext(), "不可直接拆分-。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(csRhythmHelper.checkIsMulti(realIndex)){
                    Toast.makeText(getContext(), "不可直接拆分多连音。", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(csRhythmHelper.binaryDividingAt(realIndex)<25){
                    //拆分完成，应刷新控件
                    rh_editor_EM.codeChangedReDraw();

                    //转选区模式（原位置+包含后一个）
                    selectEndIndex = realIndex+1;
                    tv_topInfo.setText("选区模式");
                    freeAreaModeOn = true;
                    moveAreaStart = true;//强制按前端选定模式
                    moveAreaEnd = false;
                    //通知UI（改框色、改起止范围）
                    rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                    checkMoveModeAndSetBottomInfo();
                }else {
                    Toast.makeText(getContext(), "拆分失败。可能是音符含附点、音符过小等原因。", Toast.LENGTH_SHORT).show();

                }//否则无反映
                break;

            case R.id.tv_over_3:
                //改均分多连音
                //原要求只要不跨拍子，且时值符合就允许操作；现改为仅允许对单个操作。
                if(selectStartIndex!=selectEndIndex){//高兼容，即使不在单点模式，但是二者相等也可。
                    Toast.makeText(getContext(), "仅对单个符号进行操作。", Toast.LENGTH_SHORT).show();
                    return;//三种合法情形均不满足，不能执行操作
                }
                if(!csRhythmHelper.checkAreaInsideBeat(selectStartIndex,selectEndIndex)){
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
                    if(freeAreaModeOn){
                        realIndex = selectStartIndex;
                    }else {
                        realIndex = currentUnitIndex;
                    }
                    if(csRhythmHelper.changeCodeToXAt(realIndex)<25){
                        rh_editor_EM.codeChangedReDraw();

                    }
                }
                break;

            case R.id.tv_merge:
//                tv_topInfo.setText("");
                if(csRhythmHelper.checkAreaCrossBeats(selectStartIndex,selectEndIndex)){
                    Toast.makeText(getContext(), "暂不允许跨拍子合并。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!csRhythmHelper.checkAreaPureCodes(selectStartIndex,selectEndIndex)){
                    Toast.makeText(getContext(), "选区内音符不单一，不能合并。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //要先把box改为单点模式。避免越界溢出。
                tv_topInfo.setText("单点模式");
                moveAreaStart = false;
                moveAreaEnd = false;
                oneBeatModeOn = false;
                dualBeatModeOn = false;
                freeAreaModeOn = false;

                currentUnitIndex = selectStartIndex;//先改一个
                rh_editor_EM.boxMovedSuccessReDraw(currentUnitIndex,false,false);
                checkMoveModeAndSetBottomInfo();

                //调用编码辅助类的合并方法（暂定只允许对一拍、不足一拍的选区进行合并；跨拍的（含超1拍，2拍多拍的）暂不处理）
                int resultCodeMerge = csRhythmHelper.mergeArea(selectStartIndex,selectEndIndex);
                selectEndIndex = selectStartIndex;//用完了之后这侧也要修改。
                if(resultCodeMerge<3300){
                    //成功，通知自定义UI改变
                    rh_editor_EM.codeChangedReDraw();
                }else {
                    //失败，给出提示
                    Toast.makeText(getContext(),"失败代码："+resultCodeMerge,Toast.LENGTH_SHORT).show();
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
                if(!oneBeatModeOn&&!dualBeatModeOn&&csRhythmHelper.checkCodeValue(codeSerial.get(currentUnitIndex))!=valueOfBeat){
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
                if(csRhythmHelper.checkAreaUnderCurve(selectStartIndex,selectEndIndex)){
                    Toast.makeText(getContext(), "请先删除区域内的连音弧", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(freeAreaModeOn){
                    //选区模式下（包括两端实质相等时）
                    //检测选区是否恰为1或2个拍子（如不得是半+半、半+1+半等的形式；且时值符合1或2beat）
                    if(csRhythmHelper.checkAreaOneNicelyBeat(selectStartIndex,selectEndIndex)){
                        if(csRhythmHelper.replaceAreaToHaveSpot(selectStartIndex,selectEndIndex)<33){
                            //整1拍，可以改
                            currentUnitIndex = selectStartIndex;//先改一个光标（统一位置）
                            rh_editor_EM.boxMovedSuccessReDraw(currentUnitIndex,false,false);
//                            Log.i(TAG, "onClick: to Single Spot");
                            //这样在rhV中，区域模式关闭，不绘制区域选框。（在du数量减小时，不越界）
                            // 更新后再改为选区。不能直接改选区，如【形如8 8 126 16的编码段，直接改为选区
                            // (ssi,ssi+2)的后端恰=126，转换不到正确的dU，会返回-1（稍后或可对该转换方法改进？）
                            rh_editor_EM.codeChangedReDraw();
//                            Log.i(TAG, "onClick: cs="+codeSerial.toString());

                            //然后改选区，调边界
                            selectEndIndex = selectStartIndex+1;//【1拍的附点后面没有126，两拍有一个，因而需要+2】
//                            Log.i(TAG, "onClick: ssi="+selectStartIndex+",sei="+selectEndIndex);
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,true);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }else if(csRhythmHelper.checkAreaTwoNicelyBeat(selectStartIndex,selectEndIndex)){
                        //整2拍，可以改
                        if(csRhythmHelper.replaceAreaToHaveSpot(selectStartIndex,selectEndIndex)<33){

                            currentUnitIndex = selectStartIndex;//先改一个光标（统一位置）
                            rh_editor_EM.boxMovedSuccessReDraw(currentUnitIndex,false,false);
//                            Log.i(TAG, "onClick: to Single Spot");
                            //这样在rhV中，区域模式关闭，不绘制区域选框。（在du数量减小时，不越界）
                            // 更新后再改为选区。不能直接改选区，如【形如8 8 126 16的编码段，直接改为选区
                            // (ssi,ssi+2)的后端恰=126，转换不到正确的dU，会返回-1（稍后或可对该转换方法改进？）
                            rh_editor_EM.codeChangedReDraw();
//                            Log.i(TAG, "onClick: cs="+codeSerial.toString());

                            //然后改选区，调边界
                            selectEndIndex = selectStartIndex+2;//【附点后面有126，因而需要+2】
//                            Log.i(TAG, "onClick: ssi="+selectStartIndex+",sei="+selectEndIndex);
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,true);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }else {
                        //选区模式同时区域内时值不符，或不是恰整拍子
                        Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //单点模式
                    if(csRhythmHelper.checkCodeValue(codeSerial.get(currentUnitIndex))!=valueOfBeat){
                        Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if(csRhythmHelper.replaceAreaToHaveSpot(selectStartIndex,selectEndIndex)<33){
                            rh_editor_EM.codeChangedReDraw();

                            //转选区模式（原位置+包含后一个）
                            selectEndIndex = selectStartIndex+1;//单点模式下肯定不是双排，中间没有126
                            tv_topInfo.setText("选区模式");
                            freeAreaModeOn = true;
                            moveAreaStart = true;//强制按前端选定模式
                            moveAreaEnd = false;
                            //通知UI（改框色、改起止范围）
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                            checkMoveModeAndSetBottomInfo();

                        }

                    }
                }
                break;

            case R.id.tv_rwd16:
                if(csRhythmHelper.checkAreaUnderCurve(selectStartIndex,selectEndIndex)){
                    Toast.makeText(getContext(), "请先删除区域内的连音弧", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(freeAreaModeOn){
                    //选区模式下（包括两端实质相等时）
                    //检测选区是否恰为1个拍子（如不得是半+半等的形式；且时值符合1 beat）
                    if(csRhythmHelper.getAreaValue(selectStartIndex,selectEndIndex)!=valueOfBeat){
                        Toast.makeText(getContext(), "需选定整拍才可转换。", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        //可以改
                        //音符是变多的，不需转单点
                        if(csRhythmHelper.changeAreaToRwd16(selectStartIndex,selectEndIndex)<25){
                            rh_editor_EM.codeChangedReDraw();
                            //调整选区边界
                            resetSelectionAreaToTotalBeat();
                            oneBeatModeOn = true;
                            /*if(selectStartIndex!=selectEndIndex){
                                freeAreaModeOn = true;
                            }*/
                            //通知UI（改框色、改起止范围）
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }
                }else {
                    //单点模式下
                    if(csRhythmHelper.checkCodeValue(codeSerial.get(currentUnitIndex))!=valueOfBeat){
                        Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if(csRhythmHelper.changeAreaToRwd16(selectStartIndex,selectEndIndex)<33){
                            rh_editor_EM.codeChangedReDraw();
                            //转选区
                            resetSelectionAreaToTotalBeat();
                            tv_topInfo.setText("选区模式");
                            oneBeatModeOn = true;
                            freeAreaModeOn = true;//进入单拍选定后必须附带的
                            //通知UI（改框色、改起止范围）
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }
                }
                break;

            case R.id.tv_fwd16:
                if(csRhythmHelper.checkAreaUnderCurve(selectStartIndex,selectEndIndex)){
                    Toast.makeText(getContext(), "请先删除区域内的连音弧", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(freeAreaModeOn){
                    //选区模式下（包括两端实质相等时）
                    //检测选区是否恰为1个拍子（如不得是半+半等的形式；且时值符合1 beat）
                    if(csRhythmHelper.getAreaValue(selectStartIndex,selectEndIndex)!=valueOfBeat){
                        Toast.makeText(getContext(), "需选定整拍才可转换。", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        //可以改
                        //音符是变多的，不需转单点
                        if(csRhythmHelper.changeAreaToFwd16(selectStartIndex,selectEndIndex)<25){
                            rh_editor_EM.codeChangedReDraw();
                            //调整选区边界
                            resetSelectionAreaToTotalBeat();
                            oneBeatModeOn = true;
                            /*if(selectStartIndex!=selectEndIndex){
                                freeAreaModeOn = true;
                            }*/
                            //通知UI（改框色、改起止范围）
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }
                }else {
                    //单点模式下
                    if(csRhythmHelper.checkCodeValue(codeSerial.get(currentUnitIndex))!=valueOfBeat){
                        Toast.makeText(getContext(), "需选定整拍、整双拍后才可执行转换。", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if(csRhythmHelper.changeAreaToFwd16(selectStartIndex,selectEndIndex)<25){
                            rh_editor_EM.codeChangedReDraw();
                            //转选区
                            resetSelectionAreaToTotalBeat();
                            tv_topInfo.setText("选区模式");
                            oneBeatModeOn = true;
                            freeAreaModeOn = true;//进入单拍选定后必须附带的
                            //通知UI（改框色、改起止范围）
                            rh_editor_EM.boxAreaChangedReDraw(selectStartIndex,selectEndIndex,freeAreaModeOn);
                            checkMoveModeAndSetBottomInfo();
                        }
                    }
                }
                break;


            case R.id.tv_curve:
                //单点模式，只能删除：（如果有）则取消当前上方的连音弧
                //选区模式（且s不等e）只能新增；如果有，不允许增加
                tv_topInfo.setText("选中单个音符删除上方的弧；多选新增；不允许层叠");
                if(selectStartIndex!=selectEndIndex){
                    //是多个音符选中状态，新增操作
                    //判断是否符合
                    if(csRhythmHelper.checkAreaUnderCurve(selectStartIndex,selectEndIndex)){
                        Toast.makeText(getContext(), "暂不允许层叠。用单点模式删除。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(csRhythmHelper.checkAreaXOrXBarAndStartWithX(selectStartIndex,selectEndIndex)){
                        Toast.makeText(getContext(), "区域不是X开头或后续含有X/-以外的音符，拒绝添加。", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(csRhythmHelper.addCurveForArea(selectStartIndex,selectEndIndex)<126){
                        rh_editor_EM.codeChangedReDraw();
                    }

                }else {
                    //实质单点模式（但是此时选区的坐标和单点坐标可能不一致，判断到底是谁）
                    if(freeAreaModeOn){
                        realIndex = selectStartIndex;
                    }else {
                        realIndex = currentUnitIndex;
                    }
//                    Log.i(TAG, "onClick: getCurrentSection="+ csRhythmHelper.getCurrentSection(realIndex).toString());
                    if(csRhythmHelper.checkCurveCovering(realIndex)){
                        //删除
                        if(csRhythmHelper.removeCurve(realIndex,true)<3000){
                            rh_editor_EM.codeChangedReDraw();
                        }
                    }else {
                        Toast.makeText(getContext(), "上方无连音弧。", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.tv_copy:
                Toast.makeText(getContext(), "功能开发中……", Toast.LENGTH_SHORT).show();
                break;

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
                Toast.makeText(getContext(), "功能开发中……", Toast.LENGTH_SHORT).show();
                break;

            case R.id.tv_atEnd:
                sectionAddToEnd = !sectionAddToEnd;
                if(sectionAddToEnd){
                    tv_bottomInfo_newSectionToEnd.setText(getResources().getString(R.string.sectionAddToEnd));
                }else {
                    tv_bottomInfo_newSectionToEnd.setText("");
                }
                break;

            case R.id.tv_sectionAdd:
                //构造新节编码
                ArrayList<Byte> sectionForAdd = RhythmHelper.getStandardEmptySection(rhythmBasedCompound.getRhythmType());
//                Log.i(TAG, "onClick: rhType="+rhythmBasedCompound.getRhythmType()+",section for add ="+sectionForAdd.toString());
                if(sectionAddToEnd){
                    codeSerial.addAll(sectionForAdd);

                    //只有在“光标不在最后而却要在最后添加”时，才刷新box的绘制。
                    rh_editor_EM.codeChangedReDraw();//由于编码有变动，需要这种更新（仅更新框是不够的）
                    moveAreaStart=false;
                    moveAreaEnd=false;//添加后强行改为单点光标模式。
                    fakeResultIndex = moveBox(selectStartIndex,MOVE_FINAL_SECTION);//移动到最后一节（新节）
                    indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);//统一各光标计数器

                    rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);//根据新的位置，以单点模式重绘蓝框。
                    checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                }else {
                    int nextIndex127 = csRhythmHelper.findNext127(selectStartIndex);
                    if(nextIndex127<(codeSerial.size()-1)){
                        codeSerial.addAll(nextIndex127+1,sectionForAdd);
                    }else {
                        codeSerial.addAll(sectionForAdd);
                    }

                    rh_editor_EM.codeChangedReDraw();//由于编码有变动，需要这种更新（仅更新框是不够的）


                }


               /* if(fakeResultIndex == -1){
                    return; 这里如果==-1则可能是出错的情形。
                }*/

                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_sectionMinus:
                //暂时只允许删除空节（非空的弹出DFG后可强行删除）
                if(checkSectionAmountEqualsOne()) {
                    //只有一节不让删
                    Toast.makeText(getContext(), "只有一节，不能删除。", Toast.LENGTH_SHORT).show();
                }else if(!csRhythmHelper.checkSectionEmpty(selectStartIndex)){
                    Toast.makeText(getContext(), "所选小节不是空节。（暂时）不能删除", Toast.LENGTH_SHORT).show();
                }else {
                    //由于是按当前的ss光标删除，删除后改单点，强制位于该节前方一节（或后方）
                    //由于移动BOX时需要利用原有坐标，如果删除的恰位于尾部，删除并重置计算后，原box索引位越界；
                    // 因而先移动box坐标再删除和重置绘制信息
                    moveAreaStart=false;
                    moveAreaEnd=false;//添加后强行改为单点光标模式。
                    fakeResultIndex = moveBox(selectStartIndex,MOVE_ADJACENT_SECTION);
                    int tempIndex = selectStartIndex;//由于下面一句统一各光标计数器，故而另外保留一份旧值以供删除正确小节。
                    indexAfterMove = checkMoveModeAndSetResultIndex(fakeResultIndex);//统一各光标计数器
                    rh_editor_EM.boxMovedSuccessReDraw(indexAfterMove,moveAreaStart,moveAreaEnd);//根据新的位置，以单点模式重绘蓝框。

                    if(csRhythmHelper.removeSection(tempIndex)!=-1){
                        rh_editor_EM.codeChangedReDraw();
                    }

                    checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。

                    //删除之后，光标改为单点
                }
                break;

            case R.id.tv_confirmAddRhythm_EM:
                checkNotEmptyAndCommit();
                break;
        }
    }



    /*
    * 多次重复的代码，抽离成方法
    * 从选取模式转单点模式
    * 还用于合并，选区转附点等“字符实际减少”的操作之后，自动转单点以避免（位于最后时）选区越界
    * （本方法暂时只负责①设顶部tv；②bool关；
    * 其余操作：UI刷新box显示、底部tv设置等暂未操作。）
    *（暂时只有少量位置替换成了本方法）
    * */
    private void switchToSpotAndOffBool(){
        tv_topInfo.setText("单点模式");
        moveAreaStart = false;
        moveAreaEnd = false;
        oneBeatModeOn = false;
        dualBeatModeOn = false;
        freeAreaModeOn = false;
    }

    private boolean checkSectionAmountEqualsOne(){
        boolean passOne = false;
        for(int i=0;i<codeSerial.size();i++){
            if(codeSerial.get(i)==127&&!passOne){
                passOne = true;
            }else if(codeSerial.get(i)==127){
                return false;
            }
        }
        return true;
    }

    /* 部分需要对fg中全局变量进行操作的辅助方法（无法转移到csRH辅助类）*/
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

    private void checkMoveModeAndSetBottomInfo(){
        if(!freeAreaModeOn){
            //如果是单点移动，需要把改写底部信息栏的光标指示。【设置要在rhUi更新后才有效】
            tv_bottomInfo_cursor.setText(String.format(getResources().getString(R.string.plh_currentIndex),
                    rh_editor_EM.getBlueBoxSectionIndex(),rh_editor_EM.getBlueBoxUnitIndex()));
            tv_bottomInfo_Acursor.setText(getResources().getString(R.string.bar2));//初始
        }else{
            tv_bottomInfo_Acursor.setText(String.format(getResources().getString(R.string.plh_areaIndex),
                    rh_editor_EM.getSAStartSectionIndex(),rh_editor_EM.getSAStartUnitIndex(),
                    rh_editor_EM.getSAEndSectionIndex(),rh_editor_EM.getSAEndUnitIndex()));
        }
    }

    /* 开始移动之前，判断要使哪个光标移动（返回该光标的当前位置作为后续计算的当前点）*/
    private int checkMoveModeAndGetCurrentIndex(){
        if (moveAreaStart) {
            //选区起端移动模式
            return selectStartIndex;
        } else if (moveAreaEnd) {
            //选区末端移动模式
            return selectEndIndex;
        } else {
            //单点移动模式
            return currentUnitIndex;
        }

    }

    /* 选定当前光标所在的单个整拍子，将区域选定标记的起止坐标记录器设置为结果值*/
    private void resetSelectionAreaToTotalBeat(){
        int index = switchCursorForArea();
        selectStartIndex = csRhythmHelper.findBeatStartIndex(index);
        selectEndIndex = csRhythmHelper.findBeatEndIndex(index);
    }

    private int switchCursorForArea(){
        //为了防止“蠕动移位”现象。
        int index;
        if(moveAreaStart){
            index = selectStartIndex;
            moveAreaStart = false;
            moveAreaEnd = true;
        }else if(moveAreaEnd){
            index = selectEndIndex;
            moveAreaEnd = false;
            moveAreaStart =true;
        }else {
            index = currentUnitIndex;
        }
        return index;
    }
    /* 选定当前光标所在的两个整拍子，将区域选定标记的起止坐标记录器设置为结果值*/
    private void resetSelectionAreaToDualBeat(){
        int index = switchCursorForArea();
        if(!dualForward){
            //执行向右扩展
            //切换扩展方向（从当前光标所在拍开始）
            dualForward = true;

            selectStartIndex= csRhythmHelper.findBeatStartIndex(index);//区域开头仍然是本拍拍首
//            Log.i(TAG, "resetSelectionAreaToDualBeat: index="+index);
            int areaEndIndex = csRhythmHelper.findNextBeatEndIndex(index);//拍尾要选用下一拍（除非跨节（不允许））
//            Log.i(TAG, "resetSelectionAreaToDualBeat: nextBE index="+areaEndIndex);
//            int fakeNextBeatStartIndex = csRhythmHelper.getRealNextBeatStartIndexIfInSameSection(areaEndIndex);
            if(areaEndIndex == -1){
                //(-1是跨节了)，只实际选定单拍
                Toast.makeText(getContext(), "不许跨节→选定，只选定单拍。", Toast.LENGTH_SHORT).show();
                selectEndIndex = csRhythmHelper.findBeatEndIndex(index);
            }else {
                //选定下一拍的拍尾
//                Log.i(TAG, "resetSelectionAreaToDualBeat: aeI="+areaEndIndex);
                selectEndIndex = areaEndIndex;
            }
        }else {

            dualForward = false;

            selectEndIndex = csRhythmHelper.findBeatEndIndex(index);
//            Log.i(TAG, "resetSelectionAreaToDualBeat: index="+index);
            int areaStartIndex = csRhythmHelper.findLastBeatStartIndex(index);
//            Log.i(TAG, "resetSelectionAreaToDualBeat: nextBE index="+areaStartIndex);

            if(areaStartIndex==-1){
                Toast.makeText(getContext(), "不许跨节←选定，只选定单拍。", Toast.LENGTH_SHORT).show();
                selectStartIndex = csRhythmHelper.findBeatStartIndex(index);
            }else {
                selectStartIndex = areaStartIndex;
            }
        }
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




    public int moveBox(int currentIndex,int moveType){
        //移动完毕后，先经过检查（在调用方法中进行）再更新自定义UI；
        switch (moveType){
            case MOVE_NEXT_UNIT:
//                Log.i(TAG, "moveBox: Next U.");
                if(csRhythmHelper.checkIsFinalRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最后", Toast.LENGTH_SHORT).show();
                    return -1; //已在最后，不移动
                }else {
//                    Log.i(TAG, "moveBox: currentIndex="+currentIndex);
                    return csRhythmHelper.getNextRealUnitIndex(currentIndex);
                }
            case MOVE_NEXT_SECTION:
                if(csRhythmHelper.checkIsFinalSection(currentIndex)){
                    //已在最后，不移动
                    Toast.makeText(getContext(), "已在最后一节", Toast.LENGTH_SHORT).show();
                    return -1 ;
                }else {
                    return csRhythmHelper.getRealUnitIndexOfNextSection(currentIndex);

                }

            case MOVE_LAST_UNIT:
                if(csRhythmHelper.checkIsFirstRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最前", Toast.LENGTH_SHORT).show();
                    return -1;//已在最前，不移动

                }else{
                    return csRhythmHelper.getLastRealUnitIndex(currentIndex);
                }

            case MOVE_LAST_SECTION:
                if(csRhythmHelper.checkIsFirstSection(currentIndex)){
                    //已在最前节，不移动
                    Toast.makeText(getContext(), "已在第一小节", Toast.LENGTH_SHORT).show();
                    return -1 ;
                }else{
                    return csRhythmHelper.getLastRealUnitIndexOfLastSection(currentIndex);
                }

            case MOVE_FINAL_SECTION:
                //移到最后一节（用于添加完一个新的小节后）
                if(csRhythmHelper.checkIsFinalSection(currentIndex)){
                    //已在最后，不移动
                    Toast.makeText(getContext(), "已在最后一节，可能出错", Toast.LENGTH_SHORT).show();
                    return -1;
                }else {
                    return csRhythmHelper.getRealUnitIndexOfLastSection();
                }

            case MOVE_ADJACENT_SECTION:
                //删除之后，移到临近一节。优先向左（以免在删除最后小节时box下标越界）；
                // 如果被删小节是首节则移动到0,0
                if(csRhythmHelper.checkIsFirstSection(currentIndex)){
                    return csRhythmHelper.getFistRealUnitIndex();
                }else {
                    return csRhythmHelper.getLastRealUnitIndexOfLastSection(currentIndex);
                }              /*
                if(csRhythmHelper.checkIsFinalSection(currentIndex)){
                    //已在最后，不移动
                    Toast.makeText(getContext(), "已在最后一节，可能出错", Toast.LENGTH_SHORT).show();
                    return -1;
                }else {
                    return csRhythmHelper.getRealUnitIndexOfLastSection();
                }*/
        }
        return 0;
    }

    /* 检测选定坐标所在的节内是否有框*/
  /*  private boolean checkBoxInThisSection(int index,int cursorIndex){
        int sectionStart = 0;//如果最后sS还是0一般代表当前节在最前。
        int sectionEnd = 0;//如果最后sE还是0就是出错。
        //向前找127
        for (int i=index;i>0;i--){
            if(codeSerial.get(i)==127){
                sectionStart = i+1;
                break;
            }
        }

        for(int k=index;k<codeSerial.size();k++){
            if(codeSerial.get(k)==127){
                sectionEnd = k;
                break;
            }
        }

        return (selectStartIndex<cursorIndex&&selectEndIndex>cursorIndex);

    }*/

    public void checkNotEmptyAndCommit(){
        if(csRhythmHelper.checkAllListEmpty()){
            Toast.makeText(getContext(), "提交空节奏？没有意义的。", Toast.LENGTH_SHORT).show();
            listIsEmpty = true;
            return;
        }else {
            listIsEmpty =false;
        }
        rhythmBasedCompound.setCodeSerialByte(csRhythmHelper.getCodeSerial());//【不知是否需要这样操作一下？】

        bundleForSendBack = new Bundle();
        bundleForSendBack.putParcelable("COMPOUND_RHYTHM", rhythmBasedCompound);

        //子类实现后面的
//        mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_EDITED, bundleForSendBack);
    }

}
