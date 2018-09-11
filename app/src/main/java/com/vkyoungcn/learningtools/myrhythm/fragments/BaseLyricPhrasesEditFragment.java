package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.LyricEditorBaseOnRSLE;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class BaseLyricPhrasesEditFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "BaseLyricPhrasesEditFragment";

    public static final int MOVE_NEXT_UNIT = 2921;
    public static final int MOVE_NEXT_PHRASE = 2922;
    public static final int MOVE_LAST_UNIT = 2923;
    public static final int MOVE_LAST_PHRASE = 2924;
    public static final int MOVE_FINAL_PHRASE = 2925;
    public static final int DELETE_MOVE_LAST_PHRASE = 2926;
    public static final int MOVE_ADJACENT_PHRASE = 2927;


    RhythmBasedCompound rhythmBasedCompound;//传递进来的数据
    ArrayList<Byte> codeSerial;

    boolean modifyPrimary = true;//传递数据时一并传递选择（对哪个词条进行处理）
    String modifyAllPhrasesInOne = "";
    //数据说明：rbc中Lyric的ArrayList数据仅用于初始时向Editor传递数据；
    //在稍后的修改中，是Editor和整体edt间的同步；尽在最后确认时，附加相应的标记后执行Editor和Lyric之间的同步（以及DB）；
    //然后返回更新rbc的显示
    //所以本fg中不初始持有一维数据，仅在点击手动同步后从Rh控件获取

    //交互发回Activity进行，简化复杂问题。
    OnGeneralDfgInteraction mListener;
    Bundle bundleForSendBack;
    boolean listIsEmpty = false;


//    CodeSerial_Rhythm csRhythmHelper;


    /* 当前选中区域的两端坐标，单code模式下，sI==eI（暂定需要这样判断实际选择区域）*/
    int currentUnitIndex = 0;
//    int selectStartIndex = 0;
//    int selectEndIndex = 0;
//    int realIndex = 0;//临时使用。
    //注意，由于界限索引需要同UI控件交互，需要指示到可绘制的code上（所以126、127、112+都是不能指向的）

//    boolean dualForward = true;//选定两个拍子时，存在朝向问题；选定其一为正另一为反。暂定向右为正，默认方向。

//    boolean freeAreaModeOn = false;//必须多设这个变量才能正确判断【？】
//    boolean moveAreaStart = false;//为逻辑安全起见，只要是手动移动，下方1BMO,2BMO自动关闭。
    // （否则需要每步移动都检测是否恰好是1/2拍）
//    boolean moveAreaEnd = false;
//    boolean oneBeatModeOn = false;//转切分、转附点在单、双拍选区下起作用；(或则单点恰=vb)
//    boolean dualBeatModeOn = false;//转前后十六尽在单拍模式下起作用。
    //切换到单点（单符）模式、或者移动后置否；进入到选定单拍、双拍后置真。

//    boolean sectionAddToEnd = false;

//    int currentUnitIndex;//不区分哪个指针（仅用在方法中的特殊场景）
//    int indexAfterMove;
    int indexAfterMove;

    /* 控件*/
    TextView tv_lastSection;
    TextView tv_nextSection;
    TextView tv_lastUnit;
    TextView tv_nextUnit;
    TextView tv_allConfirm;

    EditText edt_handModifyAllInOne;
    TextView tv_syncToRhv;
    TextView tv_syncFromRhv;
    TextView tv_bottomInfoAmount;
    TextView tv_bottomInfoCursorIndex;

    LyricEditorBaseOnRSLE ly_editor_LE;



    /* 构造器*/
    public BaseLyricPhrasesEditFragment() {
        // Required empty public constructor
    }

    public static BaseLyricPhrasesEditFragment newInstance(RhythmBasedCompound rhythmBasedCompound,boolean trueIfModifyPrimary) {
        BaseLyricPhrasesEditFragment fragment = new BaseLyricPhrasesEditFragment();
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
        this.modifyPrimary = getArguments().getBoolean("PRIMARY",true);
        if(rhythmBasedCompound==null){
            Toast.makeText(getContext(), "传递的节奏数据为空，退出", Toast.LENGTH_SHORT).show();
            getActivity().finish();//【这样退出是否正确？】
//            return;
        }
        if(modifyPrimary){
            //判断本fg持有的副本（一维）词序复制自哪条？主行还是副行（要求与Rhv中被修改的是同一条）
            modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(rhythmBasedCompound.getPrimaryLyricPhrases());
        }else {
            modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(rhythmBasedCompound.getSecondLyricPhrases());

        }
//        this.csRhythmHelper = new CodeSerial_Rhythm(rhythmBasedCompound);
        this.codeSerial = rhythmBasedCompound.getCodeSerialByte();//插入124、125还是要修改cs的。
//        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmBasedCompound.getRhythmType());
//        Log.i(TAG, "onCreate: rhBc type="+rhythmBasedCompound.getRhythmType());

    }
    功能待：调整乐句容量（移动124、125）。

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_lyric_phrases, container, false);

        ly_editor_LE = rootView.findViewById(R.id.rh_editor_FELP);

        tv_lastSection = rootView.findViewById(R.id.tv_lastPhrase_EL);
        tv_nextSection = rootView.findViewById(R.id.tv_nextPhrase_EL);
        tv_lastUnit=rootView.findViewById(R.id.tv_lastUnit_EL);
        tv_nextUnit = rootView.findViewById(R.id.tv_nextUnit_EL);
        tv_allConfirm =rootView.findViewById(R.id.tv_confirmAddRhythm_EM);//这个没变，就是用的旧布局的

        edt_handModifyAllInOne =  rootView.findViewById(R.id.edt_modifyBH_FELP);
        tv_syncToRhv =  rootView.findViewById(R.id.tv_syncToRhv_FELP);
        tv_syncFromRhv =  rootView.findViewById(R.id.tv_syncFromRhv_FELP);

        tv_bottomInfoAmount = rootView.findViewById(R.id.tv_infoBottom_amount_FELP);
        tv_bottomInfoCursorIndex = rootView.findViewById(R.id.tv_infoBottom_cI_FELP);

        tv_bottomInfoAmount.setText("");//暂时无法从Rhv获取数据
        //String.format(getResources().getString(R.string.plh_currentAmount),0,0
        tv_bottomInfoCursorIndex.setText("");//初始
/*String.format(getResources().getString(R.string.plh_currentIndex_LY),
                ly_editor_LE.getBlueBoxSectionIndex(), ly_editor_LE.getBlueBoxUnitIndex())*/
        
//设监听
        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);

        tv_allConfirm.setOnClickListener(this);

        tv_syncToRhv.setOnClickListener(this);
        tv_syncFromRhv.setOnClickListener(this);
        //        ly_editor_LE.setRhythm(rhythmBasedCompound);rh编辑器的设置由实现类负责

        return rootView;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_lastSection_EM:
                indexAfterMove = moveBox(currentUnitIndex,MOVE_LAST_PHRASE);
                if(indexAfterMove == -1){
                    return;
                }
                currentUnitIndex = indexAfterMove;
                ly_editor_LE.boxMovedSuccessReDraw(currentUnitIndex);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_nextSection_EM:
                indexAfterMove = moveBox(currentUnitIndex,MOVE_NEXT_PHRASE);
                if(indexAfterMove == -1){
                    return;
                }
                currentUnitIndex = indexAfterMove;
                ly_editor_LE.boxMovedSuccessReDraw(currentUnitIndex);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_lastUnit_EM:
                indexAfterMove = moveBox(currentUnitIndex,MOVE_LAST_UNIT);
                if(indexAfterMove == -1){
                    return;
                }
                currentUnitIndex = indexAfterMove;
                ly_editor_LE.boxMovedSuccessReDraw(currentUnitIndex);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_nextUnit_EM:
                indexAfterMove = moveBox(currentUnitIndex,MOVE_NEXT_UNIT);
                if(indexAfterMove == -1){
                    return;
                }
                currentUnitIndex = indexAfterMove;
                ly_editor_LE.boxMovedSuccessReDraw(currentUnitIndex);
                checkMoveModeAndSetBottomInfo();//注意顺序，要在rhUI更新后。
                break;

            case R.id.tv_syncToRhv_FELP:
                syncToRhv();

                break;
            case R.id.tv_syncFromRhv_FELP:
                syncFromRhv();//方法中自带对下方tv信息的更新时设置。

                break;
            case R.id.tv_confirmAddRhythm_EM:
                checkNotEmptyAndCommitBack();
                break;

        }
    }





    /* 部分需要对fg中全局变量进行操作的辅助方法（无法转移到csRH辅助类）*/
    /* 返回的值是移动后的索引值（或者在移动无效时是移动前的值），但都是当前模式对应的正确的索引项的值
    * 增加一个返回值的原因是便于后续的利用（不必再判断模式）
    * */


    private void checkMoveModeAndSetBottomInfo(){
            //改写底部信息栏的光标指示。【设置要在rhUi更新后才有效】
            tv_bottomInfoCursorIndex.setText(String.format(getResources().getString(R.string.plh_currentIndex_LY),
                    ly_editor_LE.getBlueBoxSectionIndex(), ly_editor_LE.getBlueBoxUnitIndex()));
//            tv_bottomInfo_Acursor.setText(getResources().getString(R.string.bar2));//初始
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
            case MOVE_NEXT_PHRASE:
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

            case MOVE_LAST_PHRASE:
                if(csRhythmHelper.checkIsFirstSection(currentIndex)){
                    //已在最前节，不移动
                    Toast.makeText(getContext(), "已在第一小节", Toast.LENGTH_SHORT).show();
                    return -1 ;
                }else{
                    return csRhythmHelper.getLastRealUnitIndexOfLastSection(currentIndex);
                }

            case MOVE_FINAL_PHRASE:
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
        int designSizeCurrentPhase = checkCurrentPhraseDesignSize(ly_editor_LE.getCurrentDuCsIndex());
        tv_bottomInfoAmount.setText(String.format(getResources().getString(R.string.plh_currentAmount),designSizeCurrentPhase,ly_editor_LE.getCurrentPhaseRealSize()));

    }


    //由本方法可见，不传递完整rbc似乎无法正常工作。
    public int checkCurrentPhraseDesignSize(int currentCsIndex){
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



    private void syncFromRhv(){
        this.modifyAllPhrasesInOne = Lyric.toCodeSerialStringByPhrases(ly_editor_LE.getPrimaryPhrases());
        //不需对tvInfo做任何修改。
    }



    public void checkNotEmptyAndCommitBack(){

        if(modifyAllPhrasesInOne.isEmpty()){
            Toast.makeText(getContext(), "将歌词改为空？这是无意义的操作请返回检查。", Toast.LENGTH_SHORT).show();
//            listIsEmpty = true;
            return;
        }
//        rhythmBasedCompound.setCodeSerialByte(csRhythmHelper.getCodeSerial());//【不知是否需要这样操作一下？】

        bundleForSendBack = new Bundle();
        bundleForSendBack.putString("STRING", modifyAllPhrasesInOne);//传来的时候是rbc(因为显示Rhv需要全部信息)
        //传回的时候只传整体字串就行了（就算phrases都不用传因为转换就行了），

        //子类实现后面的
//        mListener.onButtonClickingDfgInteraction(RHYTHM_CREATE_EDITED, bundleForSendBack);
    }

}
