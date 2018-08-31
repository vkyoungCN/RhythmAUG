package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import java.util.ArrayList;

public class RhythmSingleLineEditor extends RhythmSingleLineView{
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
//* 在所有小节之后标示一个+号。
//* 单行模式，绘制中的小节位于屏幕中。
    private static final String TAG = "RhythmEditor";

    /*特有*/
    //逻辑
    private int blueBoxSectionIndex = 0;//蓝框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int blueBoxUnitIndex = 0;//蓝框位置(小节内du的索引)

    private int curveOrangeBoxSpan = -1;//进入连音弧绘制模式后，绘制蓝框+黄框，其中黄框可移动以便选定弧线起止位置。
    //负值记录向左侧dU的偏移量；正值记录向右侧的偏移量；0为蓝框位置，默认向左一个（即蓝框前一个）

    private int mergeGreenBoxStartDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是负值）
    private int mergeGreenBoxEndDiff = 0;//自由合并模式下，选定合并区的起点（坐标相对于蓝框的偏移，只能是正值）


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
    public static final int MOVE_NEXT_UNIT = 2901;
    public static final int MOVE_NEXT_SECTION = 2902;
    public static final int MOVE_LAST_UNIT = 2903;
    public static final int MOVE_LAST_SECTION = 2904;
    public static final int MOVE_FINAL_SECTION = 2905;
    public static final int DELETE_MOVE_LAST_SECTION = 2906;


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

        //本类特有：蓝框
        DrawingUnit blue_dU = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
            canvas.drawRect(blue_dU.left, blue_dU.top, blue_dU.right, blue_dU.bottomNoLyric, blueBoxPaint);

        //绿框仅在合并情形下绘制；（蓝框仍然绘制）

        //橘框仅在添加连音弧线时绘制

//            invalidate();
    }



    /* 设置方法使用基类的*/
    /* 蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //编码数据改变，但位置不改变
    public void codeChangedReDraw(){
        //        codesInSections =newCodes2Dimension;//不传不行啊……并不能更新绘制结果（测试发现dus还改变了）

        this.codesInSections = RhythmHelper.codeParseIntoSections(bcRhythm.getCodeSerialByte(), rhythmType);
         【到此，待。】
        initDrawingUnits_step1();//只针对节奏部分做了修改（如果是完整的CRH，词序暂时不变，音高？）  【音高、词序如何相应调整？只有Editor存在此逻辑要求】
        invalidate();
    }


    //只改位置，不改编码数据
    //①改变蓝框指针的位置；②改变绘制中心【目前只是简单的处理为：如果超绘制区域，则将新指针所在du绘制到（水平）中心（即对所有du进行平移）】
    public int moveBox(int moveType){
        int maxSectionIndex = drawingUnits.size()-1;
        int maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;

        switch (moveType){
            case MOVE_NEXT_UNIT:
                if(blueBoxUnitIndex==maxUnitIndexCurrentSection&&blueBoxSectionIndex==maxSectionIndex){
                    //已在最后，不移动
                    Toast.makeText(mContext, "已在最后", Toast.LENGTH_SHORT).show();
                    return 0;
                }else if (blueBoxUnitIndex ==maxUnitIndexCurrentSection) {
                    //跨节
                    blueBoxUnitIndex = 0;
                    blueBoxSectionIndex++;
                    maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));
                    invalidate();

                    return 11;
                }else {
                    //不跨节
                    blueBoxUnitIndex ++;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();

                    return 1;
                }
            case MOVE_NEXT_SECTION:
                if(blueBoxSectionIndex == maxSectionIndex){
                    //已在最后，不移动
                    Toast.makeText(mContext, "已在最后", Toast.LENGTH_SHORT).show();
                    return 0 ;
                }else {
                    //跨节移动到下节首
                    blueBoxUnitIndex = 0;
                    blueBoxSectionIndex++;
                    maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();

                    return 11;
                }
            case MOVE_LAST_UNIT:
                if(blueBoxUnitIndex == 0&&blueBoxSectionIndex==0){
                    //已在最前，不移动
                    Toast.makeText(mContext, "已在最前", Toast.LENGTH_SHORT).show();
                    return 0;
                }else if(blueBoxUnitIndex == 0) {
                    //跨节移到上节末尾
                    blueBoxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;
                    blueBoxUnitIndex = maxUnitIndexCurrentSection;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();
                    return -11;
                }else {
                    //本节内移动
                    blueBoxUnitIndex--;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();

                    return -1;
                }

            case MOVE_LAST_SECTION:
                if(blueBoxSectionIndex==0){
                    //已在最前，不移动
                    Toast.makeText(mContext, "已在最前", Toast.LENGTH_SHORT).show();
                    return 0 ;
                }else{
                    //跨节移到上节首
                    blueBoxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;
                    blueBoxUnitIndex = 0;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();

                    return  -19;

                }
            case DELETE_MOVE_LAST_SECTION:
                if(blueBoxSectionIndex==0){
                    //已在最前（删除的是第一小节）,小节索引不需改变，只改单元索引。
                    blueBoxUnitIndex=0;
                    invalidate();

                    return -18;
                }else {
                    //跨节移到上节首
                    blueBoxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size() - 1;
                    blueBoxUnitIndex = 0;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));

                    invalidate();

                    return -19;
                }
            case MOVE_FINAL_SECTION:
                //移到最后一节的节首（用于添加一个新的小节后）
                blueBoxSectionIndex = maxSectionIndex;//基于“引用数据源自动修改”的设想
                maxUnitIndexCurrentSection = drawingUnits.get(blueBoxSectionIndex).size()-1;

                blueBoxUnitIndex = 0;

                //移动到中心（如果超出绘制区）
                checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(blueBoxSectionIndex).get(0)));
                invalidate();

                return 20;


        }
        return 0;
        /*解释
        * 1,：移动到下一个位置
        * -1：向上，同理；
        * 11：移动到下一节节首
        * -11：移动到上一节节末
        *  -19：移动到上一节节首。
        *  -18：首节被删除后，移动到新首节的首位。
        *  0：未发生移动
        *  20：最后一节的节首
        * */


    }

    public void checkIsBoxOutOfUiAndShiftAllHorizontally(DrawingUnit drawingUnit){
        if(drawingUnit.isOutOfUi) {//判断蓝框所在位置是否超出绘制区域
            float shiftAmount = drawingUnit.shiftAmountToCenterX;//只进行水平移动（单行模式下）
            for (ArrayList<DrawingUnit> drawingUnitSection : drawingUnits) {//所有其他元素均按本元素要求的距离移动
                for (DrawingUnit du : drawingUnitSection) {
                    du.shiftEntirely(shiftAmount, 0,padding,padding, sizeChangedWidth-padding, sizeChangedHeight-padding);
                }
            }
        }
    }




}
