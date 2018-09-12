package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.LyricEditorBaseOnRSLE;
import com.vkyoungcn.learningtools.myrhythm.helper.CodeSerial_Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class BaseLyricPhrasesRhythmBasedEditorFragment extends Fragment
        implements View.OnClickListener,LyricEditorBaseOnRSLE.LyricPhrasesInputListener {
    /*
    * 【已取消124编码】
    * 125改为辅助分隔标记。乐句划分要求依照节奏布局规则进行，暂不实现手动调整乐句结构。
    * */
    private static final String TAG = "BaseLyricPhrasesRhythmBasedEditorFragment";

    public static final int MOVE_NEXT_UNIT = 2921;
    public static final int MOVE_NEXT_PHRASE = 2922;
    public static final int MOVE_LAST_UNIT = 2923;
    public static final int MOVE_LAST_PHRASE = 2924;


    RhythmBasedCompound rhythmBasedCompound;//传递进来的数据
    ArrayList<Byte> codeSerial;

    boolean isModifyPrimary = true;//传递数据时一并传递选择（对哪个词条进行处理）
    String modifyAllPhrasesInOne = "";
    boolean modifiedAllInOne = false;
    boolean confirmClicked = false;//第一次点击后有时需要提示，再次点击后强制提交（然后重置标记）
    boolean beenAutoPhrased = false;//多次点击时提示。
    //数据说明：rbc中Lyric的ArrayList数据仅用于初始时向Editor传递数据；
    //在稍后的修改中，是Editor和整体edt间的同步；尽在最后确认时，附加相应的标记后执行Editor和Lyric之间的同步（以及DB）；
    //然后返回更新rbc的显示
    //所以本fg中不初始持有一维数据，仅在点击手动同步后从Rh控件获取

    //交互发回Activity进行，简化复杂问题。
    OnGeneralDfgInteraction mListener;
    Bundle bundleForSendBack;
    boolean listIsEmpty = false;


    CodeSerial_Rhythm csRhythmHelper;


    /* 当前选中区域的两端坐标，单code模式下，sI==eI（暂定需要这样判断实际选择区域）*/
    int currentUnitIndex = 0;
    //注意，由于界限索引需要同UI控件交互，需要指示到可绘制的code上（所以126、127、112+都是不能指向的）
    // （否则需要每步移动都检测是否恰好是1/2拍）
    //切换到单点（单符）模式、或者移动后置否；进入到选定单拍、双拍后置真。


    /* 控件*/
    /*结构*/
    TextView tv_lastSection;
    TextView tv_nextSection;
    TextView tv_lastUnit;
    TextView tv_nextUnit;
    TextView tv_inputSwitch;
    TextView tv_addTag;
    TextView tv_deleteTag;


    TextView tv_allConfirm;

    EditText edt_handModifyAllInOne;
    TextView tv_syncToRhv;
    TextView tv_syncFromRhv;
    TextView tv_bottomInfoAmount;
    TextView tv_bottomInfoCursorIndex;

    LyricEditorBaseOnRSLE ly_editor_LE;



    /* 构造器*/
    public BaseLyricPhrasesRhythmBasedEditorFragment() {
        // Required empty public constructor
    }

    public static BaseLyricPhrasesRhythmBasedEditorFragment newInstance(RhythmBasedCompound rhythmBasedCompound, boolean trueIfModifyPrimary) {
        BaseLyricPhrasesRhythmBasedEditorFragment fragment = new BaseLyricPhrasesRhythmBasedEditorFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM", rhythmBasedCompound);
        bundle.putBoolean("PRIMARY",trueIfModifyPrimary);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rhythmBasedCompound = getArguments().getParcelable("RHYTHM");
        this.isModifyPrimary = getArguments().getBoolean("PRIMARY",true);
        if(rhythmBasedCompound==null){
            Toast.makeText(getContext(), "传递的节奏数据为空，退出", Toast.LENGTH_SHORT).show();
            getActivity().finish();//【这样退出是否正确？】
//            return;
        }
        if(isModifyPrimary){
            //判断本fg持有的副本（一维）词序复制自哪条？主行还是副行（要求与Rhv中被修改的是同一条）
            modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(rhythmBasedCompound.getPrimaryLyricPhrases());
        }else {
            modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(rhythmBasedCompound.getSecondLyricPhrases());

        }
        this.csRhythmHelper = new CodeSerial_Rhythm(rhythmBasedCompound);
        this.codeSerial = rhythmBasedCompound.getCodeSerialByte();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_lyric_phrases, container, false);

        ly_editor_LE = rootView.findViewById(R.id.rh_editor_LE);
        ly_editor_LE.setRhythmViewData(rhythmBasedCompound, isModifyPrimary);
        ly_editor_LE.setCodeChangeListener(this);
        ly_editor_LE.requestFocus();

        tv_lastSection = rootView.findViewById(R.id.tv_lastPhrase_LE);
        tv_nextSection = rootView.findViewById(R.id.tv_nextPhrase_LE);
        tv_lastUnit=rootView.findViewById(R.id.tv_lastUnit_LE);
        tv_nextUnit = rootView.findViewById(R.id.tv_nextUnit_LE);
                
        tv_allConfirm =rootView.findViewById(R.id.tv_confirm_LE);//这个没变，就是用的旧布局的
        tv_inputSwitch =rootView.findViewById(R.id.tv_onAndOff_EL);
        tv_addTag = rootView.findViewById(R.id.tv_addPhrasesTag_EL);
        tv_deleteTag = rootView.findViewById(R.id.tv_deletePhrasesTag_EL);

        edt_handModifyAllInOne =  rootView.findViewById(R.id.edt_modifyBH_LE);
        edt_handModifyAllInOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!modifiedAllInOne) {
                    modifiedAllInOne = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tv_syncToRhv =  rootView.findViewById(R.id.tv_syncToRhv_LE);
        tv_syncFromRhv =  rootView.findViewById(R.id.tv_syncFromRhv_LE);

        tv_bottomInfoAmount = rootView.findViewById(R.id.tv_infoBottom_amount_LE);
        tv_bottomInfoCursorIndex = rootView.findViewById(R.id.tv_infoBottom_cI_LE);

        tv_bottomInfoAmount.setText("");//暂时无法从Rhv获取数据
        tv_bottomInfoCursorIndex.setText(String.format(getResources().getString(R.string.plh_currentIndex_LY),0,0));

        //设监听
        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);
        tv_addTag.setOnClickListener(this);
        tv_deleteTag.setOnClickListener(this);
        tv_allConfirm.setOnClickListener(this);

        tv_inputSwitch.setOnClickListener(this);

        tv_syncToRhv.setOnClickListener(this);
        tv_syncFromRhv.setOnClickListener(this);
        //        ly_editor_LE.setRhythm(rhythmBasedCompound);rh编辑器的设置由实现类负责

        return rootView;

    }



    @Override
    public void onClick(View v) {
        int moveType;

        switch (v.getId()){
            case R.id.tv_lastPhrase_LE:
                //（在本fg设计内）此按键仅用于移动单个选定的蓝框，不移动选区。
                //蓝框仅能在可承载字词的位置上移动
                moveType= MOVE_LAST_PHRASE;
                moveReDrawAndSetBottom(moveType);
                break;
 /*if(indexAfterMove == -1){
                    return;
                }*/
            case R.id.tv_nextPhrase_LE:
                moveType= MOVE_NEXT_PHRASE;
                moveReDrawAndSetBottom(moveType);
                break;

            case R.id.tv_lastUnit_LE:
                moveType= MOVE_LAST_UNIT;
                moveReDrawAndSetBottom(moveType);
                break;

            case R.id.tv_nextUnit_LE:
                moveType= MOVE_NEXT_UNIT;
                moveReDrawAndSetBottom(moveType);
                break;

            case R.id.tv_syncToRhv_LE:
                syncToRhv();
                break;

            case R.id.tv_syncFromRhv_LE:
                syncFromRhv();//方法中自带对下方tv信息的更新时设置。
                break;

            case R.id.tv_confirmAddRhythm_EM:
                checkNotEmptyAndCommitBack();
                break;


            case R.id.tv_onAndOff_EL:
                //InputMethodManager来控制输入法弹起和缩回。
                InputMethodManager methodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                methodManager.toggleSoftInput(0,InputMethodManager.RESULT_SHOWN);
                break;


            case R.id.tv_addPhrasesTag_EL:
                //在XX中间插入乐句分隔标记125（仅限XX情形）【实际为在当前选定X的后面】
                int resultNum = csRhythmHelper.addPhrasesTagAfter(currentUnitIndex);
                if(resultNum<0){
                    Toast.makeText(getContext(), "Something goes wrong..."+resultNum, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(), "成功。", Toast.LENGTH_SHORT).show();
                    ly_editor_LE.codeChangedReDraw();
                }

                break;
            case R.id.tv_deletePhrasesTag_EL:
                //删除乐句中间的间隔（仅限XX情形）

                if(csRhythmHelper.deletePhrasesTagAt(currentUnitIndex)<0){
                    Toast.makeText(getContext(), "Something goes wrong...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(), "成功。", Toast.LENGTH_SHORT).show();
                    ly_editor_LE.codeChangedReDraw();
                }

                break;

//【恰当的使用规范】：①自动划分乐句，②手动选定乐句，选定一端进行微调（通常是句末）；③提交（结构更改）
// [1~3步骤已略去。要求按节奏的规则进行不需手动调整。仅对XX型中间的分隔进行插入划分]
// ④开启文字输入（选区光标退出，蓝框开始显示，系统输入法面板展开）（可以输入单字或多字，按容量显示一部分；
// 未显示的也可保留只是不显示。）⑤调整输入光标的位置，微调或输入其他句内容；⑥提交（文字变动）；
// 整体修改功能一般不使用，使用时，通过#号手动分隔乐句。

        }
    }


//考虑新增跨越/涵盖空位置时的提示【现在根本不允许了】

    private void moveReDrawAndSetBottom(int moveType){
        int tempIndex = moveBox(currentUnitIndex,moveType);
        checkMoveValidAndReDrawOrRevert(tempIndex);
        checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
    };

    private void checkMoveValidAndReDrawOrRevert(int targetIndex){

        if(targetIndex<0){
            Toast.makeText(getContext(), "移动错误。不予移动。", Toast.LENGTH_SHORT).show();
        }else {
            ly_editor_LE.boxMovedSuccessReDraw(targetIndex);
            currentUnitIndex = targetIndex;
        }

    }

    /* 部分需要对fg中全局变量进行操作的辅助方法（无法转移到csRH辅助类）*/
    /* 返回的值是移动后的索引值（或者在移动无效时是移动前的值），但都是当前模式对应的正确的索引项的值
    * 增加一个返回值的原因是便于后续的利用（不必再判断模式）
    * */


    private void checkMoveModeAndSetBottomInfo(){
            //改写底部信息栏的光标指示。【设置要在rhUi更新后才有效】
            tv_bottomInfoCursorIndex.setText(String.format(getResources().getString(R.string.plh_currentIndex_LY),
                    ly_editor_LE.getPhraseIndex(), ly_editor_LE.getWordInPhraseIndex()));
//            tv_bottomInfo_Acursor.setText(getResources().getString(R.string.bar2));//初始
    }


    public int moveBox(int currentIndex,int moveType){
        //移动完毕后，先经过检查（在调用方法中进行）再更新自定义UI；
        switch (moveType){
            case MOVE_NEXT_UNIT:
                if(csRhythmHelper.checkIsFinalAvailableRealUnit(currentIndex)){
                    //检查是否已是最后一个可承载字词的实际音符
                    Toast.makeText(getContext(), "已在最后", Toast.LENGTH_SHORT).show();
                    return currentIndex; //已在最后，不移动
                }else {
                    return csRhythmHelper.getNextAvailableRealUnit(currentIndex);
                }
            case MOVE_NEXT_PHRASE:
                //如果已在最后一节，直接调到最后一词
                //如果已在最后一词，提示
                if(csRhythmHelper.checkIsFinalAvailableRealUnit(currentIndex)){
                    //检查是否已是最后一个可承载字词的实际音符
                    Toast.makeText(getContext(), "已在最后", Toast.LENGTH_SHORT).show();
                    return currentIndex; //已在最后，不移动
                }else {
                    return csRhythmHelper.getRealAvailableUnitIndexOfNextPhrase(currentIndex);
                    //此方法更新为“已在最后一句时自动返回最后一句的最后一词坐标”从而不必做额外处理。
                    //一切错误时返回当前位置（暂不是）。
                }


            case MOVE_LAST_UNIT:
                if(csRhythmHelper.checkIsFirstAvailableRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最前", Toast.LENGTH_SHORT).show();
                    //已在最前，不移动
                    return currentIndex;
                }else{
                    return csRhythmHelper.getLastAvailableRealUnit(currentIndex);
                }

            case MOVE_LAST_PHRASE:
                if(csRhythmHelper.checkIsFirstAvailableRealUnit(currentIndex)){
                    Toast.makeText(getContext(), "已在最前", Toast.LENGTH_SHORT).show();
                    return currentIndex;//已在最前，不移动

                }else{
                    return csRhythmHelper.getLastAvailableUnitIndexOfLastSection(currentIndex);
                }

            /*case MOVE_FINAL_PHRASE:
                //移到最后一节（用于添加完一个新的小节后）
                if(csRhythmHelper.checkIsFinalSection(currentIndex)){
                    //已在最后，不移动
                    Toast.makeText(getContext(), "已在最后一节，可能出错", Toast.LENGTH_SHORT).show();
                    return -1;
                }else {
                    return csRhythmHelper.getRealUnitIndexOfLastSection();
                }

            case MOVE_ADJACENT_PHRASE:
                //删除之后，移到临近一节。优先向左（以免在删除最后小节时box下标越界）；
                // 如果被删小节是首节则移动到0,0
                if(csRhythmHelper.checkIsFirstSection(currentIndex)){
                    return csRhythmHelper.getFistRealUnitIndex();
                }else {
                    return csRhythmHelper.getLastRealUnitIndexOfLastSection(currentIndex);
                }    */
            /*
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

    private void syncToRhv(){
        //将fg中的一维长串同步到RhV中，待Rhv重绘后，获取新的Amount值（本乐句容量、实际容量）
        //在dfg中直接对一维长串进行的修改只有同步到Rhv后才会更新下方显示。
        if(modifyAllPhrasesInOne.isEmpty()){
            Toast.makeText(getContext(), "将歌词改为空？这是无意义的操作请返回检查。", Toast.LENGTH_SHORT).show();
//            listIsEmpty = true;
            return;
        }
        ly_editor_LE.updatePhrasesAndReDraw(Lyric.toPhrasesByCodeSerialString(modifyAllPhrasesInOne));
        modifiedAllInOne = false;//更新过去后，此标记也要重置。
//        int designSizeCurrentPhase = checkCurrentPhraseDesignSize(ly_editor_LE.getCurrentDuCsIndex());
        tv_bottomInfoAmount.setText(String.format(getResources().getString(R.string.plh_currentAmount),ly_editor_LE.getCurrentPhaseRealSize()));

    }


    //由本方法可见，不传递完整rbc似乎无法正常工作。【方法有错，暂未排除】
   /* public int checkCurrentPhraseDesignSize(int currentCsIndex){
        int availableNum = 0;
        //向后遍历找乐句结尾
        for (int i=currentCsIndex; i <codeSerial.size() ; i++) {
            byte b = codeSerial.get(i);
            if(b==125){//如果在125前先遇到124则实际是错误。
                break;//125乐句结束
            }else if(b<120&&b>111){
                //连音弧跨
                //弧跨下只能是X（每个之前/或之后向前遍历时已个计算了一个位置），但是弧跨仅提供一个位置因而要把跨度减去
                availableNum -= (b-111);

            }else if(b>73){
                availableNum += b%10 ;//算多个
            }else if(b>=0){
                availableNum++;//算一个
            }//<0,111,126,127不记
        }

        //接下来向前遍历。累加(当前索引只计一次哦，上一遍历已计)
        for (int j=currentCsIndex-1;j>0;j--) {
            byte b = codeSerial.get(j);
            if (b == 124) {//如果在125前先遇到124则实际是错误。
                break;
            } else if (b < 120 && b > 111) {
                //连音弧跨
                //弧跨下只能是X（每个之前/或之后向前遍历时已个计算了一个位置），但是弧跨仅提供一个位置因而要把跨度减去
                availableNum -= (b - 111);

            } else if (b > 73) {
                availableNum += b % 10;//算多个
            } else if (b >= 0) {
                availableNum++;//算一个
            }//<0,111,126,127不记.如果遍历到头也是可以的
        }
            return availableNum;
    }
*/


    private void syncFromRhv(){
        this.modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(ly_editor_LE.getPrimaryPhrases());
        edt_handModifyAllInOne.setText(modifyAllPhrasesInOne);
        modifiedAllInOne = false;//重置标记
        //不需对tvInfo做任何修改。
    }



    public void checkNotEmptyAndCommitBack(){
        //按RhV中的内容提交；外部一维编辑条中的内容必须提交到Rh之后才有效。
        if(modifiedAllInOne&& !modifyAllPhrasesInOne.isEmpty()&&!confirmClicked){
            Toast.makeText(getContext(), "整体编辑条中的内容已修改但未与节奏记录同步，再次点击确定强制按节奏记录中现有内容提交。", Toast.LENGTH_SHORT).show();
            confirmClicked = false;
//            return;
            //但是如果它是空的则不必提示。
            //如果已修改，非空；但已经点过一次，不提示
        }else {
            //未修改、修改过已提交、是空的、强制这四种情况之一则直接按Rhv控件中已修改的情况提交
            //数据再设置一下
            /*if(isModifyPrimary) {
                rhythmBasedCompound.setPrimaryLyricPhrases(ly_editor_LE.getPrimaryPhrases());
            }else {
                rhythmBasedCompound.setSecondLyricPhrases(ly_editor_LE.getPrimaryPhrases());
            }*/
            bundleForSendBack = new Bundle();
            bundleForSendBack.putString("STRING", Lyric.toCodeSerialStringByPhrases(ly_editor_LE.getPrimaryPhrases()));
            //子类实现后面的
//        mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_EDITED, bundleForSendBack);
        }

/*
        if(modifyAllPhrasesInOne.isEmpty()){
            Toast.makeText(getContext(), "将歌词改为空？请直接删除词条关联，此处禁止本操作。", Toast.LENGTH_SHORT).show();
            return;
        }
*/
//        rhythmBasedCompound.setCodeSerialByte(csRhythmHelper.getCodeSerial());//【不知是否需要这样操作一下？】

//        bundleForSendBack.putString("STRING", modifyAllPhrasesInOne);//传来的时候是rbc(因为显示Rhv需要全部信息)
        //由于调用方需要rbc，所以还是需要传整体rbc


    }

    @Override
    public void onCodeChanged() {
//        int designSizeCurrentPhase = checkCurrentPhraseDesignSize(ly_editor_LE.getCurrentDuCsIndex());
        tv_bottomInfoAmount.setText(String.format(getResources().getString(R.string.plh_currentAmount),ly_editor_LE.getCurrentPhaseRealSize()));
    }



}
