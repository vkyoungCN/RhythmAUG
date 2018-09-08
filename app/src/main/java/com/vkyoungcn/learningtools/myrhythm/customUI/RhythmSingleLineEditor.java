package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;

import java.util.ArrayList;

public class RhythmSingleLineEditor extends RhythmSingleLineView{
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
//* 在所有小节之后标示一个+号。
//* 单行模式，绘制中的小节位于屏幕中。
    private static final String TAG = "RhythmEditor";

    /*特有*/
    //逻辑
//    private int boxSectionIndex = 0;//（dUs中的）音符单元的选框位置指针，小节索引。
    // （通常模式下，就是蓝框的位置；在锁定模式下蓝框指针与之脱离，用于为绿框左右端、橘框提供索引。）
//    private int boxUnitIndex = 0;//(小节内du的索引)
//    private boolean lockBlueBox = false;//某些模式下（加连音弧、合并区域选择等）蓝框位置要固定不动。【改用两个布尔变量判断】

    private int blueBoxSectionIndex = 0;//蓝框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int blueBoxUnitIndex = 0;//蓝框位置(小节内du的索引)

    private boolean curveModeOn = false;
    private boolean selectionAreaMode = false;

    private int tempDuSectionIndex = 0;
    private int tempDuUnitIndex = 0;

    private int sAreaStartSectionIndex = 0;
    private int sAreaStartUnitIndex = 0;
    private int sAreaEndSectionIndex = 0;
    private int sAreaEndUnitIndex = 0;

//    private float h_shiftedAmount = 0;//记录移动产生的累计量（可增可减），
    // 用于当编码更新发生在移动后从而要求重新计算绘制数据时，避免位移被重置

    private DrawingUnit selectingAreaEndDU;
    private int selectingAreaStartIndex = 0;//直接在onDraw()中使用
    private int selectingAreaEndIndex = 0;

//    private int curveOrangeBoxSpan = -1;//进入连音弧绘制模式后，绘制蓝框+黄框，其中黄框可移动以便选定弧线起止位置。
    //负值记录向左侧dU的偏移量；正值记录向右侧的偏移量；0为蓝框位置，默认向左一个（即蓝框前一个）

//    private int orangeBoxSectionIndex = 0;//连音弧插入模式下，橘框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
//    private int orangeBoxUnitIndex = 0;//连音弧插入模式下，橘框位置(小节内du的索引)


    private boolean mergeFreeModeOn = false;
    private int mergeGreenBoxStartDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是负值）
    private int mergeGreenBoxEndDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是正值）

    private int green_A_BoxSectionIndex = 0;//自由合并模式下选择合并区域的模式，左侧绿框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int green_A_BoxUnitIndex = 0;//自由合并模式下选择合并区域的模式，左侧绿框位置(小节内du的索引)
    private int green_B_BoxSectionIndex = 0;//自由合并模式下选择合并区域的模式，右侧绿框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int green_B_BoxUnitIndex = 0;//自由合并模式下选择合并区域的模式，右侧绿框位置(小节内du的索引)
    private boolean leftOn = true;//进入自由合并模式后，标志是绑定左端还有右端。


    /* 设置参数*/
    //设置参数与数据源一并设置
    private boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
    private boolean useMultiLine = false;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）

    /* 画笔组*/
    private Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
    private Paint blueBoxPaint;
    private Paint greenBoxPaint;//合并时的选择框
    private Paint orangeBoxPaint;//添加上弧连音线时的另一端位置选择框。

    /* 尺寸组 */
    /* 色彩组 */
    private int editBox_blue;
    private int editBox_green;
    private int editBox_orange;

    /* 滑动交互所需变量*/
    /* 移动蓝框交互常量*/



    public RhythmSingleLineEditor(Context context) {
        super(context);
        mContext = context;
    }

    public RhythmSingleLineEditor(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
    }


    public RhythmSingleLineEditor(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
    }



    void initSizeAndColor() {
        super.initSizeAndColor();
        //本子类特有的（蓝色框的颜色）
        editBox_blue = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxBlue);
        editBox_green = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxGreen);
        editBox_orange = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxOrange);

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

        orangeBoxPaint = new Paint();
        orangeBoxPaint.setStyle(Paint.Style.STROKE);
        orangeBoxPaint.setStrokeWidth(2);
        orangeBoxPaint.setColor(editBox_orange);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //本类特有：蓝框、绿框。【各种模式下，蓝框均绘制】
        if(!selectionAreaMode){
            //单点选择模式，绘制蓝框
            DrawingUnit drawingUnit = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
//            Log.i(TAG, "onDraw: this du isOutOfUi = "+drawingUnit.isOutOfUi);
            canvas.drawRect(drawingUnit.left, drawingUnit.top, drawingUnit.right, drawingUnit.bottomNoLyric, blueBoxPaint);
        }else {
            //选区模式
            DrawingUnit duStart = drawingUnits.get(sAreaStartSectionIndex).get(sAreaStartUnitIndex);
            DrawingUnit duEnd = drawingUnits.get(sAreaEndSectionIndex).get(sAreaEndUnitIndex);
            canvas.drawRect(duStart.left, duStart.top, duEnd.right, duEnd.bottomNoLyric, greenBoxPaint);

        }

        //橘框仅在添加连音弧线时绘制
/*
        if(curveModeOn){
            DrawingUnit orange_dU = drawingUnits.get(orangeBoxSectionIndex).get(orangeBoxUnitIndex);
            canvas.drawRect(orange_dU.left-2, orange_dU.top+additionalPointsHeight+curveOrLinesHeight, orange_dU.right+2, orange_dU.bottomNoLyric+2, blueBoxPaint);
            //暂定比蓝框大一圈(但是高度比蓝框低，让出弧线位置)，万一重合也能分辨。

            //弧线也绘制(非重合时)【暂时不考虑左右？判断太复杂了，左右坐标若设反是否存在问题，待。】
            if(orangeBoxUnitIndex!=blueBoxUnitIndex && orangeBoxSectionIndex!=blueBoxSectionIndex){
                canvas.drawArc(orange_dU.left,orange_dU.top+additionalPointsHeight,blue_dU.right,
                        orange_dU.top+additionalPointsHeight+curveOrLinesHeight,
                        0,180,false,bottomLinePaint);
            }

        }
*/

        //绿框仅在合并情形下绘制；（蓝框仍然绘制）【单行模式下没有跨行问题，不需考虑绿框的跨行处理逻辑】
/*
        if (mergeFreeModeOn){
            DrawingUnit green_dU_A = drawingUnits.get(green_A_BoxSectionIndex).get(green_A_BoxUnitIndex);
            //绿框需要由两端的两个dU分别提供左右坐标（其实也可能二者指向同一位置）
            DrawingUnit green_dU_B = drawingUnits.get(green_B_BoxSectionIndex).get(green_B_BoxUnitIndex);
            canvas.drawRect(green_dU_A.left-4, green_dU_A.top-4, green_dU_B.right+4, green_dU_B.bottomNoLyric+4, blueBoxPaint);
            //暂定比橘色框还大一圈，万一重合也能分辨。
        }
*/

//            invalidate();
    }



    /* 设置方法使用基类的*/
    /* 蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //编码数据改变，但位置不改变
    public void codeChangedReDraw(){
        //        codesInSections =newCodes2Dimension;//不传不行啊……并不能更新绘制结果（测试发现dus还改变了）
        this.codesInSections = RhythmHelper.codeParseIntoSections(bcRhythm.getCodeSerialByte(), rhythmType);
        initDrawingUnits(false);
    }

    public void boxAreaChangedReDraw(int startIndex, int endIndex,boolean freeModeOn){
        if(freeModeOn) {
            //选区模式【单纯使用ss==se并不一定不是选区】
            selectionAreaMode = true;
            changeOneDimCsIndexToTwoDimDuIndex(startIndex);
            sAreaStartSectionIndex = tempDuSectionIndex;
            sAreaStartUnitIndex = tempDuUnitIndex;
            Log.i(TAG, "boxAreaChangedReDraw: SASSI/SASUI="+sAreaStartSectionIndex+"/"+sAreaStartUnitIndex);

            changeOneDimCsIndexToTwoDimDuIndex(endIndex);
            sAreaEndSectionIndex = tempDuSectionIndex;
            sAreaEndUnitIndex = tempDuUnitIndex;
            Log.i(TAG, "boxAreaChangedReDraw: SAESI/SAEUI="+sAreaEndSectionIndex+"/"+sAreaEndUnitIndex);
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
//            Log.i(TAG, "boxMovedSuccessReDraw: index after move="+indexAfterMove);
            changeOneDimCsIndexToTwoDimDuIndex(indexAfterMove);
            blueBoxSectionIndex = tempDuSectionIndex;
            blueBoxUnitIndex = tempDuUnitIndex;
//            Log.i(TAG, "boxMovedSuccessReDraw: temp SI="+tempDuSectionIndex+"；temp UI="+tempDuUnitIndex);

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
//            Log.i(TAG, "boxMovedSuccessReDraw: Index after move="+indexAfterMove);
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

    private void changeOneDimCsIndexToTwoDimDuIndex(int index){
        //遍历查找正确位置上的dU,以及其二维坐标
        for(int i=0; i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> duList = drawingUnits.get(i);
            for(int j=0;j<duList.size();j++){
                DrawingUnit drawingUnit = duList.get(j);
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
        return ;//找不到
    }


   /* public void checkIsBoxOutOfUiAndShiftAllHorizontally(DrawingUnit drawingUnit){
        if(drawingUnit.isOutOfUi) {//判断蓝框所在位置是否超出绘制区域
            float shiftAmount = drawingUnit.shiftAmountToCenterX;//只进行水平移动（单行模式下）
            for (ArrayList<DrawingUnit> drawingUnitSection : drawingUnits) {//所有其他元素均按本元素要求的距离移动
                for (DrawingUnit du : drawingUnitSection) {
                    du.shiftEntirely(shiftAmount, 0,padding,padding, sizeChangedWidth-padding, sizeChangedHeight-padding);
                }
            }
        }
    }*/


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
