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
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;
import java.util.List;

import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.DELETE_MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_FINAL_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_UNIT;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class MelodyBaseEditFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "RhythmBaseEditFragment";
    /* 逻辑*/

    /* 当前选中区域的两端坐标，单code模式下选中的sI=eI*/
    int selectStartIndex = 0;
    int selectEndIndex = 0;


//    int valueOfBeat = 16;
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

    int currentSectionIndex = 0;
    int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

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
        tv_selectionAreaStart = rootView.findViewById(R.id.tv_selectionAreaStart) ;
        tv_selectionAreaEnd = rootView.findViewById(R.id.tv_selectionAreaEnd) ;

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
            case R.id.rh_editor_EM:

            case R.id.tv_selectBeat:
                //点击后，从默认的选中单个code变为选中所在的Beat


            case R.id.tv_selectDualBeat:
            case R.id.tv_selectSingleCode:

            case R.id.tv_merge:
            case R.id.tv_selectionAreaStart:
            case R.id.tv_selectionAreaEnd:

            case R.id.tv_over_2:
            case R.id.tv_over_3:
            case R.id.tv_toX:

            case R.id.tv_toZero:

            case R.id.tv_toDv:
            case R.id.tv_toHaveSpot:
            case R.id.tv_rwd16:
            case R.id.tv_fwd16:

            case R.id.tv_lastSection_EM:
            case R.id.tv_nextSection_EM:
            case R.id.tv_lastUnit_EM:
            case R.id.tv_nextUnit_EM:

            case R.id.tv_curve:
            case R.id.tv_copy:

            case R.id.tv_bar:
            case R.id.tv_prefix:
            case R.id.tv_sectionAdd:
            case R.id.tv_sectionMinus:

            case R.id.imv_x0_ER :
                changeCode((byte)valueOfBeat);
                break;
            case R.id.imv_xb1_ER :
                changeCode((byte)(valueOfBeat/2));
                break;
            case R.id.imv_xb2_ER :
                changeCode((byte)(valueOfBeat/4));
                break;
            case R.id.imv_xb3_ER:
                changeCode((byte)(valueOfBeat/8));
                break;
            case R.id.imv_xp_ER :
                changeCode((byte)(valueOfBeat+valueOfBeat/2));
                break;
            case R.id.imv_xpb1_ER :
                changeCode((byte)(valueOfBeat/2+valueOfBeat/4));
                break;
            case R.id.imv_xpb2_ER :
                changeCode((byte)(valueOfBeat/4+valueOfBeat/8));
                break;
            case R.id.imv_xl1_ER :
                changeCode((byte)(valueOfBeat*2));
                break;
            case R.id.imv_xl2_ER :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xl3_ER :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xm1_ER :
                int fraction = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(8,fraction);
                break;
            case R.id.imv_xm2_ER :
                int fraction_2 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(9,fraction_2);
                break;
            case R.id.imv_xm_ER :
                int fraction_3 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(7,fraction_3);
                break;
            case R.id.tv_empty_ER :
                changeToEmpty();
                break;
            case R.id.imv_longCurveEnd_ER:
                int spanNum = Integer.parseInt(edt_longCurveNum.getText().toString());
                if(spanNum>7||spanNum<2){
                    //不合理的跨度
                    Toast.makeText(getContext(), "连音跨度不合理，请检查输入", Toast.LENGTH_SHORT).show();
                    break;
                }
                insertCurveEndAfterCurrent(spanNum);
                break;
            case R.id.tv_longCurveRemove_ER:

                int returnNum = checkAndRemoveLongCurve(true);
                if(returnNum<0){
                    Toast.makeText(getContext(), "不在连音弧覆盖的范围，没有删除的目标", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_lastSection_ER:
                moveBox(MOVE_LAST_SECTION);
                break;
            case R.id.tv_lastUnit_ER:
                moveBox(MOVE_LAST_UNIT);

                break;
            case R.id.tv_nextSection_ER:
                moveBox(MOVE_NEXT_SECTION);
                break;
            case R.id.tv_nextUnit_ER:
                moveBox(MOVE_NEXT_UNIT);
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

    void changeCode(Byte newCode){
        //【逻辑修改】①要先确定当前框框套住的音符（原值）及原值+其后紧邻的所有空值的总值（可用总值）
        //新值=原值：直接改变该位置上的编码
        //新输入的值<原值，则“拆分”(实际是：删除编码*1，新增编码*2，其中后一音符要与原音符的字符一致)
        //新值>原值，但小于等于总可用值，按所占用的空间生成若干个音符（如果与整拍值不对等，则还要拆分）
        //新值>总可用值，提示先删除。

        //【可以向后面的空拍扩展，但不能向前；前面的空拍子要保留】
        int newValue = checkCodeValue(newCode);//原则上，newCode一定是大于零的。
        Log.i(TAG, "changeCode: newV="+newValue);

        availableValue();//此方法会自动更新availableValue和span

        if(newValue>availableValue){
            //所需空间比总可用都大，不改变编码
            Toast.makeText(getContext(), "剩余空时值不足，请考虑删除后续临近的既有音符", Toast.LENGTH_SHORT).show();
        }else {
            //可以改变编码
            changeCodeAndNext(newCode,currentSectionIndex,currentUnitIndexInSection,false,0);

            //通知到UI改变
            rh_editor_EM.codeChangedReDraw(codesInSections);

        }
    }


    /* 改变当前位置上的编码，根据新旧时值的不同，产生多种不同的处理情况；可能涉及对后续空拍位置的处理
    * 需要提前判断好后面有多少空余可用值，只要是调用到本方法的情形需要都是在可用可改动的时值总值内的*/
    void changeCodeAndNext(byte toCode,int sectionIndex, int unitIndex,boolean isRecursive,int recursiveTime){
        //一般：需要改成的编码和该位置上编码的情况（仅考虑该位置上的情况）
        byte oldCode = codesInSections.get(sectionIndex).get(unitIndex);
        int toValue = checkCodeValue(toCode);
        Log.i(TAG, "changeCodeAndNext: toV="+toValue);
        int oldValue = checkCodeValue(oldCode);
        //递归标记置真时，一般会产生连音、延音，需要处理。【只在新值小于或等于旧值的情况下，后方才会添加连音结束标记，
        // 而大于时仍然会继续递归，若添加连音结束符会产生错误的多层连音（注：多层连音本身在记谱中并不错误，但此递归中的是另外情形。）】
        //首次调用时，应当手动传入值为0的递归次数

        //如果当前的改变位于旧有连音线内且递归产生新连音线【上递归必然大于一个位置的时值，而如果递归则后面必有空拍或-，理论上这种情形下没有旧连音线】

        int tempCursor = unitIndex;

        if(toValue==oldValue){
            //等时值，直接替换
            codesInSections.get(sectionIndex).set(unitIndex, toCode);
            if(isRecursive){
                //递归调用要在后面添加连音线
                //（但是如果连音的开头一个音符是完整的，则似乎要将中间的完整音符改为-，但逻辑较复杂且并不是很确定乐谱记法暂略，
                // 毕竟不影响谱子的视唱。）
                codesInSections.get(sectionIndex).add(unitIndex+1,(byte)(111+recursiveTime));
            }

        }else if(toValue<oldValue){
            //新值小于旧值
            //又分两种情况（旧值如果是大附点（唯一大于基本节拍的允许编码）则改成一个）
            // 【但是好像没区别，都是改一个，然后剩余部分生成新的（只是大附点下，后续的音符会呈现为至少两半的
            // 但是可以另行手动合并，而不必直接自动合并之。也不应自动合并）】

            //【待+】①valueToCode()，对于X拆成X(3b)形式，16→2，余14；此剩余的14需要生成为2+4+8；
            // ②当节奏是8分型时，X(3b)应当取消显示、
            //【toEmpty，X改0，得到的是 - 0；需要修改。】



            //当前位置改变
//            Log.i(TAG, "changeCodeAndNext: Old code in position="+codesInSections.get(sectionIndex).get(unitIndex));
            codesInSections.get(sectionIndex).set(unitIndex, toCode);
//            Log.i(TAG, "changeCodeAndNext: New code in position="+codesInSections.get(sectionIndex).get(unitIndex));
            if(isRecursive){
                tempCursor++;
                //递归调用要在后面添加连音线
                codesInSections.get(sectionIndex).add(tempCursor,(byte)(111+recursiveTime));
            }
            //根据旧值是空拍还是实拍填充不同的新编码（实际为原旧拍剩余）
            if(oldCode>0){
                tempCursor++;
                //后面是实际拍子
                //在后面添加一个剩余时值的拍子
                codesInSections.get(sectionIndex).add(tempCursor,(byte)(oldValue-toValue));
                //指定索引+1位置上（即cUIIS+2）开始的元素都会右移。（添加到指定的索引位置上，即cUIIS+1）

            }else {
                tempCursor++;
                //oldCode<0||oldCode==0都填空拍
                //空拍子，在后面添加一个剩余时值的空拍
                codesInSections.get(sectionIndex).add(tempCursor,(byte)-(oldValue-toValue));
                //【如果进入过递归if分支，则在此临时游标实际=+2】
            }
        }else {
            //新大于旧
            //【原则上，位置上原来是什么时值，新值只能小于或等于原值。即使后方可用总空间大，但由多个小空拍组成也只能拆成“按萝卜坑填入”形式，
            // 若要安排一个更整体的音符，只能先删除后方细分音符（即合并成空拍）】

            //新拍子大于原来的一个位置，要产生连音弧。此时如果后面一个字符是112+也即连音符结尾，则①取消该标记，
            // ②将该标记的跨度增加到新标记的跨度之上，其和作为新弧的跨度。③新弧的结尾标记位于适当的新位置。

            if(oldValue==valueOfBeat){
                //旧拍子占整拍
                codesInSections.get(sectionIndex).set(unitIndex, (byte)valueOfBeat);
                //剩余的待填入部分递归调用
                tempCursor++;
                recursiveTime++;//准备递归，递归次数+1。

                if(tempCursor<codesInSections.get(sectionIndex).size()) {
                    byte tempCursorCode = codesInSections.get(sectionIndex).get(tempCursor);
                    if(tempCursorCode>=112){
                        //后一个字符，是旧连音弧结尾标记
                        recursiveTime+=(tempCursorCode-111);//把之前的“额外跨度”加总到新弧，取消旧弧

                        //旧弧结束标记删除
                        codesInSections.get(sectionIndex).remove(tempCursor);

//                        tempCursor++;[因为移除了该字符，因而不必再+1]
                        if(tempCursor<codesInSections.get(sectionIndex).size()) {
                            //仍未超节
                            //【原则上不再是连音弧结尾标记，因为暂不支持多层连音弧嵌套。不检查】
                            //改变该编码
                            changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, tempCursor,true,recursiveTime);
                        }else {
                            //跨节
                            tempCursor=0;
                            sectionIndex++;
                            changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, tempCursor,true,recursiveTime);
                        }
                    }else {
                        //后续是正常音符
                        changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, tempCursor,true,recursiveTime);
                    }
                }else {
                    //跨节
                    tempCursor=0;
                    sectionIndex++;
                    //小节首个编码不可能是连音弧结尾（即使连音弧线的末端在小节首音符，该结束标记也必然在第二个编码上；
                    // 如果连音弧线在上一节节末音符结束，则结束标记需要位于上一节末尾；如果出现首编码>112实际是出错情形）
                            changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, tempCursor,true,recursiveTime);
                }
            }else if(oldValue == valueOfBeat+valueOfBeat/2){
                //旧拍子是大附点
                // 原位置改为整拍
                codesInSections.get(sectionIndex).set(unitIndex, (byte)valueOfBeat);

                //后半按旧值是空拍还是实拍填充不同新编码（实际为新拍子的一部分的后一部分）
                if(oldCode>0){
                    tempCursor++;
                    //后面是实际拍子
                    //在后面添加一个剩余时值(附点部分)的拍子
                    codesInSections.get(sectionIndex).add(tempCursor,(byte)(valueOfBeat/2));
                    //指定索引+1位置上（即cUIIS+2）开始的元素都会右移。（添加到指定的索引位置上，即cUIIS+1）

                }else {
                    tempCursor++;
                    //oldCode<0||oldCode==0都填空拍
                    //空拍子，在后面添加一个剩余时值的空拍
                    codesInSections.get(sectionIndex).add(tempCursor,(byte)-(valueOfBeat/2));
                    //【如果进入过递归if分支，则在此临时游标实际=+2】
                }

                //剩余的新时值，递归处理
                tempCursor++;
                recursiveTime++;//准备递归，递归次数+1。

                if(tempCursor<codesInSections.get(sectionIndex).size()) {
                    byte tempCursorCode = codesInSections.get(sectionIndex).get(tempCursor);
                    if(tempCursorCode>=112) {
                        //后一个字符，是旧连音弧结尾标记
                        recursiveTime += (tempCursorCode - 110);//把之前的“额外跨度”加总到新弧，取消旧弧
                        // 【而且由于之前的大附点1符变两符，跨度要多+1，在此-110】

                        //旧弧结束标记删除
                        codesInSections.get(sectionIndex).remove(tempCursor);
                        //删除后，索引不必递增
                        if (tempCursor < codesInSections.get(sectionIndex).size()) {
                            //仍未超节
                            //【原则上不再是连音弧结尾标记，因为暂不支持多层连音弧嵌套。不检查】
                            //改变该编码
                            changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                        } else {
                            //跨节
                            tempCursor = 0;
                            sectionIndex++;
                            changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                        }
                    }else {
                        //后续是正常音符
                        changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                    }
                }else {
                    //跨节
                    tempCursor=0;
                    sectionIndex++;
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }

            }else {
                //旧符一定小于一整拍了
                // 原位置改为可用时值（根据旧值的大小）
                if(oldCode<0){
                    //空拍子，以和原码相反（等于原码的值）的编码填充
                    codesInSections.get(sectionIndex).set(tempCursor,(byte)oldValue);
                }//实拍子，占据该时值的拍子（相当于不动）

                //剩余时值到下一个拍子处理
                //剩余的新时值，递归处理
                tempCursor++;
                recursiveTime++;//准备递归，递归次数+1。


                if(tempCursor<codesInSections.get(sectionIndex).size()) {

                    byte tempCursorCode = codesInSections.get(sectionIndex).get(tempCursor);
                    if(tempCursorCode>=112) {
                        //后一个字符，是旧连音弧结尾标记
                        recursiveTime += (tempCursorCode - 111);//把之前的“额外跨度”加总到新弧，取消旧弧

                        //旧弧结束标记删除
                        codesInSections.get(sectionIndex).remove(tempCursor);
                        //删除后，索引不必递增
                        if (tempCursor < codesInSections.get(sectionIndex).size()) {
                            //仍未超节
                            //【原则上不再是连音弧结尾标记，因为暂不支持多层连音弧嵌套。不检查】
                            //改变该编码
                            changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                        } else {
                            //跨节
                            tempCursor = 0;
                            sectionIndex++;
                            changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                        }
                    }else {
                        //后续是正常音符
                        changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor, true, recursiveTime);
                    }

                }else {
                    //跨节
                    tempCursor=0;
                    sectionIndex++;
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }
            }
        }
    }

    void availableValue(){
        //span至少要包括自己在内的（也即至少=1）
        //availableValue从0初始为首位的值
        availableValue =0;//先清空
        availableValue+=checkCodeValue(codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection));//加上自己

        for(int i=currentSectionIndex; i<codesInSections.size();i++){
            ArrayList<Byte> codeInsideSection = codesInSections.get(i);
            if(i==currentSectionIndex) {
                for (int j=currentUnitIndexInSection+1;j<codeInsideSection.size();j++){
                    //从第二个开始计算的
                    byte currentCode = codeInsideSection.get(j);
                    if(currentCode>112){
                        //延音弧线结束标记，不作数
                        continue;//【这里待增加删线逻辑！！！】
                    }else if(currentCode>0){
                        //此时必然小于112，包括：有时值的音符、均分多连音两类，都是“不再可用”，要停止
                        return ;
                    }else if(currentCode==0) {
                        //空拍、延长符，都是可用的
                        span++;
                        availableValue+=valueOfBeat;
                    }else {
                        //只剩小于0
                        span++;
                        availableValue-=currentCode;

                    }
                }
            }else {
                for (Byte code : codeInsideSection) {
                    if(code>112){
                        //延音弧线结束标记，不作数
                        continue;
                    }else if(code>0){
                        //此时必然小于112，包括：有时值的音符、均分多连音两类，都是“不再可用”，要停止
                        return;
                    }else if(code==0) {
                        //空拍、延长符，都是可用的
                        span++;
                        availableValue+=valueOfBeat;
                    }else {
                        //只剩小于0
                        span++;
                        availableValue-=code;
                    }
                }
            }
        }
    }

    int availableValueInsideBeat(){
        //不需跨节遍历
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        int availableValueInsideBeat =checkCodeValue(codesInThisSection.get(currentUnitIndexInSection));//拍内可用值，先把自身加上。

        //所在拍子的后边界（需要从头遍历，要遍历到所在拍完结）
        int totalValue = 0;
//            int beatStartCursor = 0;
        beatEndCursor = codesInThisSection.size()-1;
        for (int k=0;k<codesInThisSection.size();k++) {
            byte code = codesInThisSection.get(k);

            //蓝框位置之前的大附点要特别处理
            if(code ==valueOfBeat+valueOfBeat/2 && k<currentUnitIndexInSection){
                //遇到大附点（且仍然在前半段，未到目标位置）则跳过
                totalValue+=checkCodeValue(code);
//                    beatStartCursor++;
                continue;
            }//后面的大附点不会对本位置上相关改写的逻辑造成影响。从略。

            totalValue+=checkCodeValue(code);
            if(totalValue%valueOfBeat==0){
                //一拍完结
                    /*if(k<currentUnitIndexInSection){
                        //时值加总后仍然未到改动位置，说明是前面的某拍，还未到“当前拍”
//                        beatStartCursor = k+1;
                    }*/
                if(k>=currentUnitIndexInSection){
                    //k首次跨越目标位置后的拍尾位置
                    beatEndCursor = k;
                    break;//终止遍历
                }
            }
        }//至此，已获得所在拍子的止索引

        //计算拍内从蓝框位置到拍结束的可用剩余空间
        for(int s=currentUnitIndexInSection+1;s<=beatEndCursor;s++){//由于之前已经计算了自身，因而这里s从+1处起
            availableValueInsideBeat+=checkCodeValue(codesInThisSection.get(s));
        }
        return availableValueInsideBeat;
    }

    int checkCodeValue(byte code) {
        if (code > 112) {
            //上弧连音专用符号，不记时值
            return 0;//但是由于实际上不会选中结束符，因而这种状态是错误的
        }else if(code>92){
            return 4;//三类均分多连音的时值的定值，不随内容数量改变，也与vb无关。
        }else if(code>82){
            return 8;
        }else if (code > 72) {
            //时值计算
            return 16;
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

    void changeToEmpty(){
        //这个方法直接把圈定字符改为等值的负值。
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        byte b = codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection);
        //当前已经是空拍，退出
        if(b<0){
            return;
        }
        if(b==0){
            //当前是延音号-
            //能否占满整拍（能：检测后面是否是延音号——是则继续检测延音号前的音符是什么并将后面的延音符改成整拍实拍；
            // 不是则不管。）
            //(不能占满时，检测前方实拍，将剩余部分改成正确时值的前方实拍)
            //

        }


        //①当前位置设为同时值空拍（负数等值）
        codesInThisSection.set(currentUnitIndexInSection,(byte)-b);
//        Log.i(TAG, "changeToEmpty: current code="+codesInThisSection.get(currentUnitIndexInSection));

        if(b==valueOfBeat|| b==valueOfBeat+valueOfBeat/2){
            //独占一拍，不存在合并情形
            rh_editor_EM.codeChangedReDraw(codesInSections);
            return;
        }

        int temp_centerCursor = currentUnitIndexInSection;
        byte newCode = codesInSections.get(currentSectionIndex).get(temp_centerCursor);
        if(temp_centerCursor==(codesInSections.get(currentSectionIndex).size()-1)){
            //已到小节最末
            rh_editor_EM.codeChangedReDraw(codesInSections);
            return;
        }
        byte nextCode = codesInSections.get(currentSectionIndex).get(temp_centerCursor+1);
        if(newCode==nextCode){//新符必然是某值的空拍（<0）
            //仅在前一或后一存在与本音符等值的空拍时，合并
            //如果此时未跨拍，则合并
        }
        if(temp_centerCursor==0){
            //首符
            rh_editor_EM.codeChangedReDraw(codesInSections);
            return;
        }
        byte lastCode = codesInSections.get(currentSectionIndex).get(temp_centerCursor-1);

        if(newCode == lastCode){
            //仅在前一或后一存在与本音符等值的空拍时，合并
            //如果此时未跨拍，则合并
        //【寻找拍子边界，②可能要进一步递归调用（多个可合并时（每两个合并的符号，都要值对等））】
        }

        //②临近且不超过一个拍子的空拍合并
        //先找到前后紧邻的空拍起止位置；
        int minAdjacentEmptyCursor = 0;
        int maxAdjacentEmptyCursor = currentUnitIndexInSection;
        boolean isAllEmpty = true;//同时判断是否整节全空了（若是则删除整节）

        for (int i=0;i<currentUnitIndexInSection;i++) {
            byte code = codesInThisSection.get(i);

            //从前向后捋。
            if(code>=0&&code<112){
                //该位置非空，且在改变位置之前（延音线不能算空拍）
                minAdjacentEmptyCursor=i+1;//需要让该最小相邻起始空点位于当前索引之后（因为当前位置非空）
                //极端情形下（前方没有空拍，则最小相邻起始空点=被改变位置的索引即可）

                isAllEmpty = false;//只单向设置
                break;
            }
        }

        //从后向前捋
        for (int i=codesInThisSection.size()-1;i>currentUnitIndexInSection;i--) {
            byte code = codesInThisSection.get(i);

            //从前向后捋。
            if(code>=0&&code<112){
                //该位置非空，且在改变位置之后
                maxAdjacentEmptyCursor=i-1;//需要让该最大相邻起始空点位于当前索引之前（因为当前位置非空）
                //极端情形下（后边没有空拍，则最大相邻起始空点=被改变位置的索引即可）

                isAllEmpty = false;//只单向设置
                break;
            }
        }

        //短路判断，尝试减轻做功（如果周围没有其他空拍子，就可略去执行了）
        if(minAdjacentEmptyCursor!=maxAdjacentEmptyCursor){
            //周围有空拍，需合并
            //要找到所在拍子的前后边界（必须要从头遍历，至少到达所在拍完结）
            int totalValue = 0;
            int beatStartCursor = 0;
            int beatEndCursor = codesInThisSection.size()-1;
            for (int k=0;k<codesInThisSection.size();k++) {
                byte code = codesInThisSection.get(k);

                //蓝框位置之前的大附点的特别处理逻辑
                if(code ==valueOfBeat+valueOfBeat/2 && k<currentUnitIndexInSection){
                    //遇到大附点（且仍然在前半段，未到目标位置）则跳过
                    totalValue+=checkCodeValue(code);
                    beatStartCursor++;
                    continue;
                }//后面的大附点不会对本位置上相关改写的逻辑造成影响。从略。

                totalValue+=checkCodeValue(code);
                if(totalValue%valueOfBeat==0){
                    //一拍完结
                    if(k<currentUnitIndexInSection){
                        //时值加总后仍然未到改动位置，说明是前面的某拍，还未到“当前拍”
                        beatStartCursor = k+1;
                    }
                    if(k>=currentUnitIndexInSection){
                        //k首次跨越目标位置后的拍尾位置
                        beatEndCursor = k;
                        break;//终止遍历
                    }
                }
            }//至此，已获得所在拍子的起止索引

            //接下来，比较所在拍子的起止和“邻接空拍”起止位置的（大小/位置前后）关系
            int mergeStartCursor = Math.max(beatStartCursor,minAdjacentEmptyCursor);//合并的起点，两起点比较选二者稍大的
            int mergeEndCursor = Math.min(beatEndCursor,maxAdjacentEmptyCursor);//合并的终点，两终点比较选二者稍小

            //上述范围内的编码全部删除，改为一个（时值恰当的）负值
            int equallyEmptyValue = 0;
            List<Byte> temp_emptyAdjacent = codesInThisSection.subList(mergeStartCursor,mergeEndCursor);
            for (byte code :temp_emptyAdjacent) {
                equallyEmptyValue+=checkCodeValue(code);
            }
            //移除（同拍内的临接空拍）
            codesInThisSection.remove(temp_emptyAdjacent);
            //在被移除空拍序列的起始位置上添加一个与被移除的总值等值的空拍
            codesInThisSection.add(mergeStartCursor,(byte)-equallyEmptyValue);
        }

        //【如果位于连音弧线下的某音符改为空拍，该连音弧需要被取消！（怎么对空拍位置进行连音连唱？不合理）】
        //寻找改空位置之后的连音弧结束标记，如有（且跨度覆盖本字符）则删除之。
        checkAndRemoveLongCurve(false);


        if(isAllEmpty){
            //本节没有非空的拍子了,应当整节删除
            codesInSections.remove(currentSectionIndex);
//            对索引计数器的调整，由moveB方法负责。
            if(currentSectionIndex>0) {
                currentSectionIndex--;
            }
            currentUnitIndexInSection = 0;

            moveBox(DELETE_MOVE_LAST_SECTION);
        }

        //通知到UI改变
//        Log.i(TAG, "changeToEmpty: 1st code="+codesInSections.get(0).get(0));
        rh_editor_EM.codeChangedReDraw(codesInSections);


    }

    void insertCurveEndAfterCurrent(int span){
        byte code = (byte)(110+span);//这里的跨度从2起，最小是2。
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);

        //指定在最后位置附加结束标记元素（【待？能否指定这种“超标溢出”索引？】）
        codesInThisSection.add(currentUnitIndexInSection+1,code);

        /*if((codesInThisSection.size()-1-currentUnitIndexInSection)>0){
            //后面有元素，指定索引插入
            codesInThisSection.add(currentUnitIndexInSection+1,code);
            //据文档：是在指定索引插入元素，该位置原有及后续元素均右移（如果有的话）。

        }else {
            //后面已经没有元素，要附加
            codesInThisSection.add(code);
        }*/

        //通知到UI改变
        rh_editor_EM.codeChangedReDraw(codesInSections);


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

//【设计原则5：给指定蓝框设置新字符时，可以向后面的空拍扩展借用空间，但不能向前面的空拍扩展。】
    void changeCodeToMultiDivided(int ten, int fraction){
        byte newCode = (byte)(ten*10+fraction);
        int newValue = checkCodeValue(newCode);//原则上，newCode在此一定是大于零的。
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        byte oldCode = codesInThisSection.get(currentUnitIndexInSection);
        int oldValue = checkCodeValue(oldCode);
        int availableValueInBeat = availableValueInsideBeat();

        //【所以当前的逻辑设定是，均分多连音不能跨拍子！一切逻辑皆围绕本设定展开】
        if(newValue>availableValueInBeat){
            //所需空间比总可用都大，不改变编码
            Toast.makeText(getContext(), "剩余空时值不足，请考虑删除后续临近的既有音符", Toast.LENGTH_SHORT).show();
        }else {
            //可以改变编码

            //逻辑细分【以下待实现细节】
            // ①原位置如果大于新符的时值，（改动 + 分离插入）。（不论是否附点、均分多连音，都无所谓）
            // ②原位置如果等于（直接改）
            // ③原位置如果小于，但是拍内总值大于等于（将所需的值对应的原编码删除，恰当位置插入新编码；未涉及到的后续编码不处理）
            // ④连音弧处理（删除）（均分多连音需要唱做分离的音，连音弧是唱作一个整音；不能共存，会删除。【总提示+】）

            if(newValue==oldValue){
                codesInThisSection.set(currentUnitIndexInSection,newCode);
            }else if(newValue<oldValue){
                //当前字符改变
                codesInThisSection.set(currentUnitIndexInSection,newCode);

                //原字符的剩余值新建插入后续
                if(oldCode>=0) {//对，延音似乎也应该变成实际值
                    codesInThisSection.add(currentUnitIndexInSection + 1, (byte) (oldValue - newValue));
                }else {
                    //空拍子
                    codesInThisSection.add(currentUnitIndexInSection + 1, (byte) -(oldValue - newValue));
                }
            }else if(newValue>oldValue){
                List<Byte> subListToRemove = (ArrayList<Byte>) codesInThisSection.subList(currentUnitIndexInSection,beatEndCursor);
                //本符、拍内后续空符全移除
                codesInThisSection.remove(subListToRemove);
                //在原来的本符位置追加新符；
                codesInThisSection.add(currentUnitIndexInSection,newCode);
                //后面一个位置追加全部剩余时值（空拍值）（不可能超过本拍，所以无更复杂逻辑）
                codesInThisSection.add(currentUnitIndexInSection + 1, (byte) -(oldValue - newValue));

            }

            //通知到UI改变
            rh_editor_EM.codeChangedReDraw(codesInSections);
        }
    }


    void moveBox(int moveType){
        int result = rh_editor_EM.moveBox(moveType);

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
}
