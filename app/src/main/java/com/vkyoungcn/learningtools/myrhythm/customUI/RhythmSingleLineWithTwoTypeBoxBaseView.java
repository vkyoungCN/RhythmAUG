package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;

import java.util.ArrayList;

public class RhythmSingleLineWithTwoTypeBoxBaseView extends RhythmSingleLineView{
//* 提供两种box光标（①蓝框，单独一个dU；②绿框，区域）；
    static final String TAG = "RhythmSingleLineWithTwoTypeBoxBaseView";

    /*特有*/
    int blueBoxSectionIndex = 0;//蓝框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    int blueBoxUnitIndex = 0;//蓝框位置(小节内du的索引)

//    boolean curveModeOn = false;
    boolean selectionAreaMode = false;

    int tempDuSectionIndex = 0;
    int tempDuUnitIndex = 0;

    int sAreaStartSectionIndex = 0;
    int sAreaStartUnitIndex = 0;
    int sAreaEndSectionIndex = 0;
    int sAreaEndUnitIndex = 0;

//    float h_shiftedAmount = 0;//记录移动产生的累计量（可增可减），
    // 用于当编码更新发生在移动后从而要求重新计算绘制数据时，避免位移被重置

//    DrawingUnit selectingAreaEndDU;
//    int selectingAreaStartIndex = 0;//直接在onDraw()中使用
//    int selectingAreaEndIndex = 0;

//    int curveOrangeBoxSpan = -1;//进入连音弧绘制模式后，绘制蓝框+黄框，其中黄框可移动以便选定弧线起止位置。
    //负值记录向左侧dU的偏移量；正值记录向右侧的偏移量；0为蓝框位置，默认向左一个（即蓝框前一个）

//    int orangeBoxSectionIndex = 0;//连音弧插入模式下，橘框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
//    int orangeBoxUnitIndex = 0;//连音弧插入模式下，橘框位置(小节内du的索引)


//    boolean mergeFreeModeOn = false;
//    int mergeGreenBoxStartDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是负值）
//    int mergeGreenBoxEndDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是正值）
//
//    int green_A_BoxSectionIndex = 0;//自由合并模式下选择合并区域的模式，左侧绿框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
//    int green_A_BoxUnitIndex = 0;//自由合并模式下选择合并区域的模式，左侧绿框位置(小节内du的索引)
//    int green_B_BoxSectionIndex = 0;//自由合并模式下选择合并区域的模式，右侧绿框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
//    int green_B_BoxUnitIndex = 0;//自由合并模式下选择合并区域的模式，右侧绿框位置(小节内du的索引)
//    boolean leftOn = true;//进入自由合并模式后，标志是绑定左端还有右端。


    /* 设置参数*/
    //设置参数与数据源一并设置
//    boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
//    boolean useMultiLine = false;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）

    /* 画笔组*/
//    Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
    Paint blueBoxPaint;
    Paint greenBoxPaint;//合并时的选择框

    /* 尺寸组 */
    /* 色彩组 */
    int editBox_blue;
    int editBox_green;

    /* 滑动交互所需变量*/
    /* 移动蓝框交互常量*/



    public RhythmSingleLineWithTwoTypeBoxBaseView(Context context) {
        super(context);
        mContext = context;
    }

    public RhythmSingleLineWithTwoTypeBoxBaseView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
    }


    public RhythmSingleLineWithTwoTypeBoxBaseView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
    }



    void initSizeAndColor() {
        super.initSizeAndColor();
        //本子类特有的（蓝色框的颜色）
        editBox_blue = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxBlue);
        editBox_green = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxGreen);

    }

    void initPaint() {
        super.initPaint();

        //本类特有
        blueBoxPaint = new Paint();
        blueBoxPaint.setStyle(Paint.Style.STROKE);
        blueBoxPaint.setStrokeWidth(2);
        blueBoxPaint.setColor(editBox_blue);

        greenBoxPaint = new Paint();
        greenBoxPaint.setStyle(Paint.Style.STROKE);
        greenBoxPaint.setStrokeWidth(2);
        greenBoxPaint.setColor(editBox_green);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

      /*  //本类特有：蓝框、绿框。【各种模式下，蓝框均绘制】【由子类实现，因为两类的绘制方式不同。】
        if(!selectionAreaMode){
            //单点选择模式，绘制蓝框
            DrawingUnit drawingUnit = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
            canvas.drawRect(drawingUnit.left, drawingUnit.top, drawingUnit.right, drawingUnit.bottomNoLyric, blueBoxPaint);
        }else {
            //选区模式
            DrawingUnit duStart = drawingUnits.get(sAreaStartSectionIndex).get(sAreaStartUnitIndex);
            DrawingUnit duEnd = drawingUnits.get(sAreaEndSectionIndex).get(sAreaEndUnitIndex);
            canvas.drawRect(duStart.left, duStart.top, duEnd.right, duEnd.bottomNoLyric, greenBoxPaint);

        }*/
//            invalidate();
    }



    /* 设置方法使用基类的*/
    /* 蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //编码数据改变，但位置不改变
    public void codeChangedReDraw(){
        //        codesInSections =newCodes2Dimension;//不传不行啊……并不能更新绘制结果（测试发现dus还改变了）
        this.codesInSections = RhythmHelper.codeParseIntoSections(bcRhythm.getCodeSerialByte(), rhythmType);
        initDrawingUnits(false);//【计划中两个子类：RhvSLEditor、LyricEditor都会对编码进行改变】
    }

    public void boxAreaChangedReDraw(int startIndex, int endIndex,boolean freeModeOn){
        if(freeModeOn) {
            //选区模式【单纯使用ss==se并不一定不是选区】
            selectionAreaMode = true;
            changeOneDimCsIndexToTwoDimDuIndex(startIndex);
            sAreaStartSectionIndex = tempDuSectionIndex;
            sAreaStartUnitIndex = tempDuUnitIndex;

            changeOneDimCsIndexToTwoDimDuIndex(endIndex);
            sAreaEndSectionIndex = tempDuSectionIndex;
            sAreaEndUnitIndex = tempDuUnitIndex;
            //原来的蓝框必然（？）在画面中，因而在此不移动du列表。（暂简化）
        }else {
            selectionAreaMode = false;
            changeOneDimCsIndexToTwoDimDuIndex(startIndex);
            blueBoxSectionIndex = tempDuSectionIndex;
            blueBoxUnitIndex = tempDuUnitIndex;
            //判断是否超出绘制区
            checkAndShiftWhenOutOfUI(blueBoxSectionIndex,blueBoxUnitIndex);
        }
        invalidate();
    }

    public void boxMovedSuccessReDraw(int indexAfterMove,boolean saStart, boolean saEnd){
        if(!saStart && !saEnd){
            //蓝框模式
            selectionAreaMode = false;
            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            blueBoxSectionIndex = tempDuSectionIndex;
            blueBoxUnitIndex = tempDuUnitIndex;

            //判断是否超出绘制区
            checkAndShiftWhenOutOfUI(blueBoxSectionIndex,blueBoxUnitIndex);

        }else if(saStart) {
            selectionAreaMode = true;
            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            sAreaStartSectionIndex = tempDuSectionIndex;
            sAreaStartUnitIndex = tempDuUnitIndex;

            //判断被移动的端头是否超出绘制区，如是则以其为标志（令其移到中心）对整体duList进行移动
            checkAndShiftWhenOutOfUI(sAreaStartSectionIndex,sAreaStartUnitIndex);

        }else {
            selectionAreaMode = true;
            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            sAreaEndSectionIndex = tempDuSectionIndex;
            sAreaEndUnitIndex = tempDuUnitIndex;

            checkAndShiftWhenOutOfUI(sAreaEndSectionIndex,sAreaEndUnitIndex);
        }

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

    void changeOneDimCsIndexToTwoDimDuIndex(int index){
        //遍历查找正确位置上的dU,以及其二维坐标
        for(int i=0; i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> duList = drawingUnits.get(i);
            for(int j=0;j<duList.size();j++){
                DrawingUnit drawingUnit = duList.get(j);
                if(drawingUnit.indexInCodeSerial == index){
                    tempDuUnitIndex = j;
                    tempDuSectionIndex = i;
                    return;
                }
            }
        }
        tempDuSectionIndex =-1;
        tempDuUnitIndex = -1;
//        return ;//找不到
    }


    public int getBlueBoxSectionIndex() {
        return blueBoxSectionIndex+1;
    }

    public int getBlueBoxUnitIndex() {
        return blueBoxUnitIndex+1;
    }

    public int getSAStartSectionIndex() {
        return sAreaStartSectionIndex+1;
    }

    public int getSAStartUnitIndex() {
        return sAreaStartUnitIndex+1;
    }

    public int getSAEndSectionIndex() {
        return sAreaEndSectionIndex+1;
    }

    public int getSAEndUnitIndex() {
        return sAreaEndUnitIndex+1;
    }
}
