package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit.PHRASE_EMPTY;
import static com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit.PHRASE_END;
import static com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit.PHRASE_START;

public class LyricEditorBaseOnRSLE extends RhythmSingleLineWithTwoTypeBoxBaseView{
    /*
    * 【说明（重要）】
    * ①文字的安排也是有一定规则的；在节奏/旋律（唱和部分），一般在0后-前的部分安置文字；
    * 0、-、弧跨非首音位置、前缀都是禁止安置文字的（安置了也没法唱；强行唱奏则是实际产生了新节奏）
    *
    * dU中的phraseMark是用于绘图，不用于外部的逻辑检测；
    * cs中的124、125用于外部逻辑检测、绘图信息的初始化时的检测；但124、125实际上是符合①的要求而由cs
    * 的编码情况（近乎唯一地）确定的；因而事实上（几乎）不存在手动调整乐句边界的必要。
    * 124、125仍然有存在的必要：有时候两个连续的xx可能划分到两个乐句中去；此外意义就不大了。
    *
    * */

// 同时只修改一列词；允许多字输入（一次输入一串字）；允许输入超过容量的字，不予显示但会保存
// 点击确定时为各句增加#结束号；同时检测相应l_Id是否存在（存在则更新其cs；不存在则新建）
// 词的显示使用RhV即可（本就带词的显示能力）

// 注意蓝框改标下方的lyric区域。（不再使用绿框）
// 能填词的地方使用淡灰色背景块描绘（多连音描绘多个；注意间隔）（这样就不再需要使用起止三角标记和数字了）

// 能返回当前光标所在的乐句位置（index+1），第几个字

// 设计对应的（LyricEdit）dfg，（其中能对lyric编码进行一维的整体修改（手动同步））
// dfg功能：①移动蓝框；②显示位置；显示容量
//    （不可对dU结构再做改动若觉得不合适退出先改dU【本逻辑后期可能改进】）
    private static final String TAG = "LyricEditorBaseOnRSLE";

    private OnLyricPhraseChangeListner mListener;

    /* 本类特有 */
    boolean modifyPrimary = true;
    private int tempDuSectionIndex = 0;
    private int tempDuUnitIndex = 0;
    int phraseIndex = 1;//数字是从1起。代表物理位置。如果代表索引则-1；在进行1维2维转换时，会重置计算。
    int wordInPhraseIndex = 1;


    /* 画笔组*/
    private Paint unEmptyUnitBkgPaint;//添加上弧连音线时的另一端位置选择框。
    private Paint phraseNumPaint;//绘制乐句容量
    private Paint pointPaint_A;//绘制起端的下标点
    private Paint pointPaint_B;//绘制终端的下标点

    /* 尺寸组 */
    /* 色彩组 */
    private int unEmptyUnitBkg_gray;



    public LyricEditorBaseOnRSLE(Context context) {
        super(context);
        mContext = context;
    }

    public LyricEditorBaseOnRSLE(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
    }


    public LyricEditorBaseOnRSLE(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
    }



    void initSizeAndColor() {
        super.initSizeAndColor();
        //本子类特有的（蓝色框的颜色）
        unEmptyUnitBkg_gray = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxLiteGray);
    }

    void initPaint() {
        super.initPaint();
        //本类特有
        unEmptyUnitBkgPaint = new Paint();
        unEmptyUnitBkgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        unEmptyUnitBkgPaint.setStrokeWidth(2);
        unEmptyUnitBkgPaint.setColor(unEmptyUnitBkg_gray);

        phraseNumPaint = new Paint();
        phraseNumPaint.setTextSize(40);
        phraseNumPaint.setStrokeWidth(2);
        phraseNumPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        phraseNumPaint.setColor(generalColor_Gray);
//        phraseNumPaint.setTextAlign(Paint.Align.CENTER);
//        phraseNumPaint.setFakeBoldText(true);

        pointPaint_A = new Paint();
        pointPaint_A.setStrokeWidth(8);
        pointPaint_A.setColor(editBox_blue);
        pointPaint_A.setStyle(Paint.Style.FILL_AND_STROKE);

        pointPaint_B = new Paint();
        pointPaint_B.setStrokeWidth(6);
        pointPaint_B.setColor(generalColor_Gray);
        pointPaint_B.setStyle(Paint.Style.FILL);

    }


    void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }


    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // outAttrs就是我们需要设置的输入法的各种类型最重要的就是:
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        outAttrs.inputType = InputType.TYPE_NULL;
        return new PhrasesConnection(this,true);
    }
    private class PhrasesConnection extends BaseInputConnection {
        public PhrasesConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }


        //大概是由于输入法可能通过文本提交（大部分），也可能通过按键事件提交（部分输入法、部分字符）
        // 所以重写两种方法。
        /* 文本输入*/
        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            setPhraseProperly(text.toString());

            initPrimaryLyric(true,phraseIndex-1);
            postInvalidate();


//            postInvalidate()在工作者线程(UI以外的线程)刷新
            return true;
        }


        /*按键输入*/
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    //删除按键
                    deletePhraseProperly();
                    initPrimaryLyric(true,phraseIndex-1);

                } /*else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    //回车按键
                    nowString = nowString+"\n"+inputString;
                }【这是网上抄的例子，学习注释】
                */
            }

            postInvalidate();
            return true;
//            return super.sendKeyEvent(event);
        }

        //当然删除的时候也会触发
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return true;
        }

        @Override
        public boolean finishComposingText() {
            //结束组合文本输入的时候，这个方法基本上会出现在切换输入法类型，点击回车（完成、搜索、发送、下一步）点击输入法右上角隐藏按钮会触发。
            return true;
        }

    }


    //调用方（activity）实现本接口。VE中获取对调用方Activity的引用，然后调用这两个方法进行通信
    public interface OnLyricPhraseChangeListner {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered

        /* 输入一个字符时触发 */
        void onCodeChanged(int indexInCs);//改变后，光标可能移动，将移动后的光标位置传递给调用方


    }

    public void setCodeChangeListener(OnLyricPhraseChangeListner listener) {
        this.mListener = listener;
    }


    /* 安全设置新数据，检测当前蓝框所在位置对应的乐句位置是否数据越界；若是，补齐；然后改动或添加数据*/
    private void setPhraseProperly(String text){
        String currentPhrase = primaryPhrases.get(phraseIndex-1);//由于初始化时已对primaryPhrases数据补齐（元素是empty字串）
        // [注意句计数器是1起。]
        // 所以不须再检测其安全性

        StringBuilder tempSbd = new StringBuilder(currentPhrase);
        int currentBoxIndexInPhrase = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).orderNumInPharse-1;

        //但是补齐的部分都是“”，且有些句子可能原始数据就不够，因而还需要检测分句安全性。
        if(currentBoxIndexInPhrase<tempSbd.length()){
            //安全，先删后加
            tempSbd.deleteCharAt(currentBoxIndexInPhrase);
            //添加新数据
            tempSbd.insert(currentBoxIndexInPhrase,text);

        }else {
            //不足，先补齐（到前一个）
            for(int i=tempSbd.length();i<currentBoxIndexInPhrase;i++){
                tempSbd.append(".");
            }
            //追加新数据
            tempSbd.append(text);

        }
        primaryPhrases.set(phraseIndex-1,tempSbd.toString());
        mListener.onCodeChanged(moveBoxNextWord());
        invalidate();
    }


    private void deletePhraseProperly(){
        String currentPhrase = primaryPhrases.get(phraseIndex-1);//由于初始化时已对primaryPhrases数据补齐（元素是empty字串）
        // 所以不须再检测其安全性

        StringBuilder tempSbd = new StringBuilder(currentPhrase);
        int currentBoxIndexInPhrase = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).orderNumInPharse-1;

        //但是补齐的部分都是“”，且有些句子可能原始数据就不够，因而还需要检测分句安全性。
        if(currentBoxIndexInPhrase<tempSbd.length()){
            //安全，删除
            tempSbd.deleteCharAt(currentBoxIndexInPhrase);
            primaryPhrases.set(phraseIndex-1,tempSbd.toString());
            mListener.onCodeChanged(moveBoxLastWord());
            invalidate();
        }//不足时删除无意义，无反应
    }

    public int moveBoxLastWord(){
        //向前移动一个字的位置（在乐句内，暂定不跨句）
        //如果当前单元不是PmStart，则移动（要求格式一定严谨）
        if(drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).phraseMark!=PHRASE_START){
            return checkLastTillAvailable(blueBoxSectionIndex,blueBoxUnitIndex);
        }

        //如果已到句末，直接不移动。
        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
        //当前值，无移动。
    }


    int checkLastTillAvailable(int currentSectionIndex,int currentUnitIndex){
        if(currentUnitIndex>0){
            //可以在节内移动
            int tempUnitIndex = currentUnitIndex-1;
            if(drawingUnits.get(currentSectionIndex).get(tempUnitIndex).phraseMark!=PHRASE_EMPTY){
                blueBoxSectionIndex = currentSectionIndex;
                blueBoxUnitIndex = tempUnitIndex;
                //就是它了
                wordInPhraseIndex--;
                //phraseIndex不调整（原则上不跨句。）
                return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
            }else {
                checkLastTillAvailable(currentSectionIndex,tempUnitIndex);
            }
        }else if(currentSectionIndex>0) {//要跨节，可以跨节
            int tempSectionIndex = blueBoxSectionIndex-1;
            int tempUnitIndex = drawingUnits.get(tempSectionIndex).size()-1;
            if(drawingUnits.get(tempSectionIndex).get(tempUnitIndex).phraseMark!=PHRASE_EMPTY){
                blueBoxSectionIndex = tempSectionIndex;
                blueBoxUnitIndex = tempUnitIndex;

                wordInPhraseIndex--;
                return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
            }else {
                checkLastTillAvailable(tempSectionIndex,tempUnitIndex);
            }

        }

        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
        //当前值，无移动。

    }



    public int moveBoxNextWord(){
        //向后移动一个字的位置（在乐句内，暂定不跨句）
        //如果当前单元不是PmEnd，则向后移动（要求格式一定严谨，如果后面没End就惨了）
        if(drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).phraseMark!=PHRASE_END){
            return checkNextTillAvailable(blueBoxSectionIndex,blueBoxUnitIndex);
        }

        //如果已到句末，直接不移动。
        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
        //当前值，无移动。
    }

    int checkNextTillAvailable(int currentSectionIndex,int currentUnitIndex){
        if(currentUnitIndex<drawingUnits.get(currentSectionIndex).size()-1){
            //可以在节内移动
            int tempUnitIndex = currentUnitIndex+1;
            if(drawingUnits.get(currentSectionIndex).get(tempUnitIndex).phraseMark!=PHRASE_EMPTY){
                blueBoxSectionIndex = currentSectionIndex;
                blueBoxUnitIndex = tempUnitIndex;
                //就是它了
                wordInPhraseIndex++;
                //phraseIndex不调整（原则上不跨句。）
                return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
            }else {
                checkNextTillAvailable(currentSectionIndex,tempUnitIndex);
            }
        }else if(currentSectionIndex<drawingUnits.size()-1) {//要跨节，可以跨节
            int tempUnitIndex = 0;
            int tempSectionIndex = blueBoxSectionIndex+1;
            if(drawingUnits.get(tempSectionIndex).get(tempUnitIndex).phraseMark!=PHRASE_EMPTY){
                blueBoxSectionIndex = tempSectionIndex;
                blueBoxUnitIndex = tempUnitIndex;

                wordInPhraseIndex++;
                return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
            }else {
                checkNextTillAvailable(tempSectionIndex,tempUnitIndex);
            }

        }

        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
        //当前值，无移动。

    }


    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //本类特有：①LY位置非空单位的浅灰背景；②背景末端的各句容量数字；③LY位置的蓝框；

        //绘制灰色背景（可装词的dU处；尺寸少小）
        float bkTop_Y;
        float bkBottom_Y;
        for(ArrayList<DrawingUnit> duList:drawingUnits){
            for (DrawingUnit drawingUnit :duList) {
                if(drawingUnit.phraseMark != PHRASE_EMPTY) {
//                    Log.i(TAG, "onDraw: du.pmk="+drawingUnit.phraseMark+",code="+drawingUnit.code);
                    bkTop_Y = drawingUnit.bottomNoLyric -16;
                    bkBottom_Y = drawingUnit.bottomNoLyric + unitHeight + 24;

                    canvas.drawRect(drawingUnit.left + 4, bkTop_Y, drawingUnit.right - 4, bkBottom_Y, unEmptyUnitBkgPaint);
                    //左右留出间隔
                    if (drawingUnit.phraseMark == PHRASE_END) {//绘制容量数字
//                    Log.i(TAG, "onDraw: du End");
                        canvas.drawText(String.valueOf(drawingUnit.orderNumInPharse), drawingUnit.codeCenterX, drawingUnit.bottomNoLyric + 2.8f * unitHeight, phraseNumPaint);
                    }
                }
            }
        }

        //画框（取决于是单个字选定的蓝框、或是乐句调整的绿框）

        if(!selectionAreaMode){
            //单点选择模式，绘制蓝框
            DrawingUnit drawingUnit = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
            canvas.drawRect(drawingUnit.left, drawingUnit.bottomNoLyric-16, drawingUnit.right, drawingUnit.bottomNoLyric+unitHeight+24, blueBoxPaint);
        }else {
            //选区模式
            DrawingUnit duStart = drawingUnits.get(sAreaStartSectionIndex).get(sAreaStartUnitIndex);
            DrawingUnit duEnd = drawingUnits.get(sAreaEndSectionIndex).get(sAreaEndUnitIndex);
            canvas.drawRect(duStart.left, duStart.bottomNoLyric-16, duEnd.right, duEnd.bottomNoLyric+unitHeight+24, greenBoxPaint);
        }

        //起止位置特殊表现
        drawStartAndEndMarks(canvas);

        //绘制歌词（之前的已跳过未绘制）
        drawLyricAgain(canvas);
        //            invalidate();

    }

    void drawLyric_1(Canvas canvas) {
       //为了防止被后绘的背景挡住，先留空跳过绘制
    }

    void drawLyricAgain(Canvas canvas) {
        //遍历方式进行
        for (ArrayList<DrawingUnit> drawingUnitsInSections : drawingUnits) {
            for (DrawingUnit drawingUnit : drawingUnitsInSections) {
                canvas.drawText(drawingUnit.lyricWord_1, drawingUnit.lyricWord_1_CenterX, drawingUnit.lyricWord_1_BaseY, codePaint);
//                Log.i(TAG, "drawLyricAgain: word="+drawingUnit.lyricWord_1+",i="+drawingUnitsInSections.indexOf(drawingUnit));
            }
        }
    }
    void drawStartAndEndMarks(Canvas canvas ){
        for (ArrayList<DrawingUnit> drawingUnitsInSections : drawingUnits) {
            for (DrawingUnit drawingUnit : drawingUnitsInSections) {
                if(drawingUnit.phraseMark==PHRASE_START){
                    canvas.drawPoint(drawingUnit.codeCenterX, drawingUnit.bottomNoLyric+2.3f*unitHeight,pointPaint_A);
//                    canvas.drawPoint(drawingUnit.left, drawingUnit.bottomNoLyric+3*unitHeight, pointPaint_A);
                }/*else if(drawingUnit.phraseMark==PHRASE_END){
                    canvas.drawPoint(drawingUnit.right, drawingUnit.bottomNoLyric+2.5f*unitHeight, pointPaint_B);
                }*/
            }
        }
    }



    /* 原设置方法重写，仅允许设置一条词条（默认主词条）；如果设置两项，只有一项有效*/

    /* 新方法：可以选择设置哪条*/
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound,boolean modifyPrimary) {
        this.modifyPrimary = modifyPrimary;
        this.bcRhythm = rhythmBasedCompound;
        this.rhythmType = rhythmBasedCompound.getRhythmType();
        this.codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmType);

        //副列部分始终设为null这样保证只显示一行词作；
        //但是这一行显示主还是副，是基于传入的boolean设置的。
        if(modifyPrimary) {
            this.primaryPhrases = rhythmBasedCompound.getPrimaryLyricPhrases();//【在计算时，会改为按节管理版本，才能正确放置位置】
        }else {
            this.primaryPhrases = rhythmBasedCompound.getSecondLyricPhrases();//【在计算时，会改为按节管理版本，才能正确放置位置】
        }
        this.secondPhrases = null;
        this.pitchSerial = rhythmBasedCompound.getLinkingPitches();

        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        checkAndSetThreeStates();
        setSizeOfCodeAndUnit(18,20,20);

        //计算绘制信息（核心方法）
        if(sizeChangedWidth == 0){
            //此时尚未获取控件尺寸，无法真正计算绘制信息，中止
            return;
            //当运行到尺寸确定的方法（onSc）时，会对数据情况进行检查，如果有数据则会触发再次计算。
        }
        new Thread(new CalculateDrawingUnits(true)).start();
//        initDrawingUnits(false);
    }

    @Override
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound) {
        this.bcRhythm = rhythmBasedCompound;
        this.rhythmType = rhythmBasedCompound.getRhythmType();
        this.codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmType);

        this.primaryPhrases = rhythmBasedCompound.getPrimaryLyricPhrases();//【默认修改主词】
        this.secondPhrases = null;//【二行无论如何都是设null以保证只显示1行；只是在传递true时，首行的内容被强制设置成了副词序】
        this.pitchSerial = rhythmBasedCompound.getLinkingPitches();

        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        checkAndSetThreeStates();
        setSizeOfCodeAndUnit(18,20,20);

        //计算绘制信息（核心方法）
        if(sizeChangedWidth == 0){
            //此时尚未获取控件尺寸，无法真正计算绘制信息，中止
            return;
            //当运行到尺寸确定的方法（onSc）时，会对数据情况进行检查，如果有数据则会触发再次计算。
        }
        new Thread(new CalculateDrawingUnits(true)).start();
//        initDrawingUnits(false);
    }

    @Override
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound, int codeSize, int unitWidth, int unitHeight) {
        this.bcRhythm = rhythmBasedCompound;
        this.rhythmType = rhythmBasedCompound.getRhythmType();
        this.codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmType);

        this.primaryPhrases = rhythmBasedCompound.getPrimaryLyricPhrases();//【在计算时，会改为按节管理版本，才能正确放置位置】
        this.secondPhrases = null;
        this.pitchSerial = rhythmBasedCompound.getLinkingPitches();

        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        checkAndSetThreeStates();
        setSizeOfCodeAndUnit(codeSize,unitWidth,unitHeight);

        //计算绘制信息（核心方法）
        if(sizeChangedWidth == 0){
            //此时尚未获取控件尺寸，无法真正计算绘制信息，中止
            return;
            //当运行到尺寸确定的方法（onSc）时，会对数据情况进行检查，如果有数据则会触发再次计算。
        }
        new Thread(new CalculateDrawingUnits(true)).start();
//        initDrawingUnits(false);

    }


    @Override
    void initDrawingUnits(boolean needReFresh) {
        super.initDrawingUnits(needReFresh);
//        Log.i(TAG, "initDrawingUnits:Of LEBORSLE.. ");
        checkBoxInitPosition();
    }

    //默认将box二维索引设为0，0但有时候首位是不可承载音符，需要调整
    public void checkBoxInitPosition(){
        if(drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).phraseMark==PHRASE_EMPTY){
            //要向后调整
            int csIndex = checkNextTillAvailable(blueBoxSectionIndex,blueBoxUnitIndex);//由于句计数器默认1初始，应不需改。
//            Log.i(TAG, "checkBoxInitPosition: bs="+blueBoxSectionIndex+",bu="+blueBoxUnitIndex);

            Message msg = new Message();
            msg.what = CALL_LISTENER_WITH_CS_INDEX;
            msg.arg1 = csIndex;
            handler.sendMessage(msg);
//            mListener.onCodeChanged(csIndex);
//            postInvalidate();
        }

    }

    void handleMessage(Message message) {
        switch (message.what){
            case CALL_LISTENER_WITH_CS_INDEX:
//                invalidate();
            mListener.onCodeChanged(message.arg1);//基类没有监听器引用。
                invalidate();

                break;
        }

    }

    void checkAndSetThreeStates(){
        useLyric_1 = true;
        useLyric_2 = false;
        drawPitches = false;
    }

    public void updatePhrasesAndReDraw(ArrayList<String> phrases){
        this.primaryPhrases = phrases;
        initPrimaryLyric(false,0);//要全部更新
        invalidate();
    }

    /* 用于和fg间的数据交互*/
    public ArrayList<String> getPrimaryPhrases(){
        return primaryPhrases;
    }

    //当前乐句已填入的实际容量
    public int getCurrentPhaseRealSize(){
        if(primaryPhrases.isEmpty()){
            return 0;
        }
        return primaryPhrases.get(phraseIndex-1).length();
    }

    public int getCurrentDuCsIndex(){//用于进一步（由fg执行）获取当前乐句设计容量
        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
    }

    public int getCurrentPhaseDesignSize(){
        int phraseNum = 0;
        for(int i=blueBoxSectionIndex;i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> duList = drawingUnits.get(i);
            for (int j=blueBoxUnitIndex; j<duList.size(); j++) {
                DrawingUnit du = duList.get(j);
                if(du.phraseMark==PHRASE_END){
                    phraseNum = du.orderNumInPharse;
                    return phraseNum;
                }else if(du.phraseMark==PHRASE_START){
                    //先遇到124说明在句外
                    return 0;
                }
            }
        }
        //后面124、125都没有（可能是最后一句之后的空白区；或是全节奏未分句）,pN还是0。
        //向前检索
        for(int k=blueBoxSectionIndex-1;k>0;k--){
            ArrayList<DrawingUnit> duList = drawingUnits.get(k);
            for (int h=blueBoxUnitIndex; h<duList.size(); h++) {
                DrawingUnit du = duList.get(h);
                if(du.phraseMark==PHRASE_END){
                    return 0;//说明有分句，刚才是在句末外，返回0
                }
            }
        }
        return -1;//未做分句，由外部处理（可以显示--）

    }

    /*
    * 句首调整、句末调整；删除所在分句；增加分句（增加开头和结束的标记）；
    * 自动生成句界（通常，旋律节奏中0后开始，-前结束；和声伴奏可能没有这种规律但很多乐器本身也不需要气口）；
    *
    *   乐句划分原则上需要同节奏中的0和-的存在有关；否则没有换气气口（可以给出提示；毕竟是用于诵唱的歌节奏）
    * 移动：左、右；（句界的移动规则不同于蓝框，可到所有位置）（蓝框移动规则重写：只允许在非空位置移动）
    *
    * */

    /* 蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //本控件不涉及编码数据的改变

    //框移动后，更新数据与刷新显示
    public void boxMovedSuccessReDraw(int indexAfterMove){

            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            blueBoxSectionIndex = tempDuSectionIndex;
            blueBoxUnitIndex = tempDuUnitIndex;
//            updatePhraseIndex();【一维光标转二维方法内已更新该数据】

            //判断是否超出绘制区
            checkAndShiftWhenOutOfUI(blueBoxSectionIndex,blueBoxUnitIndex);

        invalidate();
    }

    /*方法存在错误，计数器不对
    private void updatePhraseIndex(){
        int tempPrIndex = 0;
        for (int i = 0; i <= blueBoxSectionIndex; i++) {
            ArrayList<DrawingUnit> dus = drawingUnits.get(i);
            int boundaryIndex = Math.min(dus.size(),blueBoxUnitIndex);
            for (int j = 0; j <boundaryIndex ; j++) {
                DrawingUnit du = dus.get(j);
                if(du.phraseMark==PHRASE_END){
                    tempPrIndex++;
                }
            }
        }
        phraseIndex = tempPrIndex;

    }
*/
    void checkAndShiftWhenOutOfUI(int sectionIndex, int unitIndex){
        DrawingUnit drawingUnit = drawingUnits.get(sectionIndex).get(unitIndex);
        if(drawingUnit.checkIsOutOfUi(padding,padding,sizeChangedWidth-padding,sizeChangedHeight-padding)){
            //如果超出绘制区，则要对所有du元素平移
            float hAmount = drawingUnit.shiftAmountToCenterX;
            h_shiftedAmount += hAmount;

            float vAmount = drawingUnit.shiftAmountToCenterY;
            for(int i=0; i<drawingUnits.size();i++){
                ArrayList<DrawingUnit> duList = drawingUnits.get(i);
                for(int j=0;j<duList.size();j++){
                    DrawingUnit du = duList.get(j);
                    du.shiftEntirely(hAmount,vAmount,padding,padding,sizeChangedWidth-padding,sizeChangedHeight-padding);
                }
            }
        }
    }

    void changeOneDimCsIndexToTwoDimDuIndex(int index){
        //遍历查找正确位置上的dU,以及其二维坐标【同时设置供返回的乐句位置数据】
//        boolean passEnd = false;//设置这个变量的意图是确保“跨过end”后（到下一个非空dU时）乐句计数器才+1。

        //先进行附带的乐句计数操作
        wordInPhraseIndex =0;
        phraseIndex=0;

        //由于下方的return，循环只会进行懂啊当前光标处。
        for(int i=0; i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> duList = drawingUnits.get(i);
            for(int j=0;j<duList.size();j++){
                DrawingUnit drawingUnit = duList.get(j);
//                Log.i(TAG, "changeOneDimCsIndexToTwoDimDuIndex: du.pm="+drawingUnit.phraseMark);
                if(drawingUnit.phraseMark!=PHRASE_EMPTY){
                    wordInPhraseIndex++;
                    if(drawingUnit.phraseMark==PHRASE_START){
                        phraseIndex++;
                        wordInPhraseIndex=1;//遇到新一句，句内计数器置1。
                    }
                }//不支持只有一个字的乐句（也无支持之的无意义）

                //当前光标位置转二维
                if(drawingUnit.indexInCodeSerial == index){
                    tempDuUnitIndex = j;
                    tempDuSectionIndex = i;
                    return;
                }
            }
        }
        tempDuSectionIndex =-1;
        tempDuUnitIndex = -3;
    }



    public int getBlueBoxSectionIndex() {
        return blueBoxSectionIndex+1;
    }
    public int getBlueBoxUnitIndex() {
        return blueBoxUnitIndex+1;
    }

    public int getPhraseIndex() {
        return phraseIndex;
    }

    public int getWordInPhraseIndex() {
        return wordInPhraseIndex;
    }
}
