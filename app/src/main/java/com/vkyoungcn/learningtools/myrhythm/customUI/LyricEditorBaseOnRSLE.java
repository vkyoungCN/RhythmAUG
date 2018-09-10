package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit.PHRASE_EMPTY;
import static com.vkyoungcn.learningtools.myrhythm.customUI.DrawingUnit.PHRASE_END;

public class LyricEditorBaseOnRSLE extends RhythmSingleLineView{
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

    /*改写*/
    private int blueBoxSectionIndex = 0;//蓝框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int blueBoxUnitIndex = 0;//蓝框位置(小节内du的索引)

    boolean modifyPrimary = true;
    private int tempDuSectionIndex = 0;
    private int tempDuUnitIndex = 0;
    int phraseIndex = 0;
    int wordInPhraseIndex = 0;

    /* 画笔组*/
    private Paint blueBoxPaint;
    private Paint unEmptyUnitBkgPaint;//添加上弧连音线时的另一端位置选择框。

    /* 尺寸组 */
    /* 色彩组 */
    private int boxBlue;
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
        boxBlue = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxBlue);
        unEmptyUnitBkg_gray = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxLiteGray);

    }

    void initPaint() {
        super.initPaint();
        //本类特有
        blueBoxPaint = new Paint();
        blueBoxPaint.setStyle(Paint.Style.STROKE);
        blueBoxPaint.setStrokeWidth(2);
        blueBoxPaint.setColor(boxBlue);

        unEmptyUnitBkgPaint = new Paint();
        unEmptyUnitBkgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        unEmptyUnitBkgPaint.setStrokeWidth(2);
        unEmptyUnitBkgPaint.setColor(unEmptyUnitBkg_gray);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //本类特有：①LY位置的蓝框；②LY位置非空单位的浅灰背景；
        //绘制灰色背景（可装词的dU处）
        for(ArrayList<DrawingUnit> duList:drawingUnits){
            for (DrawingUnit drawingUnit :duList) {
                canvas.drawRect(drawingUnit.left+4, drawingUnit.bottomNoLyric+4, drawingUnit.right-4, drawingUnit.bottomNoLyric+unitHeight-4, blueBoxPaint);
                //左右留出间隔
            }
        }

        // 绘制蓝框[]
        DrawingUnit drawingUnit = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
        canvas.drawRect(drawingUnit.left, drawingUnit.bottomNoLyric, drawingUnit.right, drawingUnit.bottomNoLyric+unitHeight, blueBoxPaint);

        //            invalidate();
    }



    /* 原设置方法重写，仅允许设置一条词条（默认主词条）；如果设置两项，只有一项有效*/
    /* 此外提供一个可以选择设置哪条的设置方法*/
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
        initDrawingUnits(false);
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
        initDrawingUnits(false);
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
        initDrawingUnits(false);

    }
    /* 蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //本控件不涉及编码数据的改变

    //框移动后，更新数据与刷新显示
    public void boxMovedSuccessReDraw(int indexAfterMove){

            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            blueBoxSectionIndex = tempDuSectionIndex;
            blueBoxUnitIndex = tempDuUnitIndex;

            //判断是否超出绘制区
            checkAndShiftWhenOutOfUI(blueBoxSectionIndex,blueBoxUnitIndex);

        invalidate();
    }


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

    private void changeOneDimCsIndexToTwoDimDuIndex(int index){
        //遍历查找正确位置上的dU,以及其二维坐标【同时设置供返回的乐句位置数据】
        boolean passEnd = false;//设置这个变量的意图是确保“跨过end”后（到下一个非空dU时）乐句计数器才+1。
        for(int i=0; i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> duList = drawingUnits.get(i);
            for(int j=0;j<duList.size();j++){
                DrawingUnit drawingUnit = duList.get(j);
                if(drawingUnit.phraseMark!=PHRASE_EMPTY){
                    wordInPhraseIndex++;
                    if(drawingUnit.phraseMark==PHRASE_END&&!passEnd){
                        passEnd=true;
                        continue;
                    }
                    if(passEnd){
                        phraseIndex++;
                        passEnd=false;
                    }
                }//不支持只有一个字的乐句（也无支持之的无意义）
//                Log.i(TAG, "changeOneDimCsIndexToTwoDimDuIndex: du.indexInCs="+drawingUnit.indexInCodeSerial);
                if(drawingUnit.indexInCodeSerial == index){
                    tempDuUnitIndex = j;
                    tempDuSectionIndex = i;
                    return;
                }
            }
        }
        tempDuSectionIndex =-1;
        tempDuUnitIndex = -1;
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
