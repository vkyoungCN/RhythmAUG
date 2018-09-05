package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.MelodyBaseEditFragment.BOX_TYPE_BLUE;
import static com.vkyoungcn.learningtools.myrhythm.fragments.MelodyBaseEditFragment.BOX_TYPE_GREEN_END;
import static com.vkyoungcn.learningtools.myrhythm.fragments.MelodyBaseEditFragment.BOX_TYPE_GREEN_START;

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

    public void boxAreaChangedReDraw(int startIndex, int endIndex){
        if(startIndex!=endIndex) {
            //一定是选区模式了
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

    private void changeOneDimCsIndexToTwoDimDuIndex(int index){
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
        return ;//找不到
    }



    //只改位置，不改编码数据
    //①改变蓝框指针的位置；②改变绘制中心【目前只是简单的处理为：如果超绘制区域，则将新指针所在du绘制到（水平）中心（即对所有du进行平移）】
    /*public int moveBox(int moveType){
        //按照模式将移动的结果传给相应选框；



        if(mergeFreeModeOn){

        }
        int maxSectionIndex = drawingUnits.size()-1;
        int maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;

        switch (moveType){
            case MOVE_NEXT_UNIT:
                if(boxUnitIndex ==maxUnitIndexCurrentSection&& boxSectionIndex ==maxSectionIndex){
                    //已在最后，不移动
                    Toast.makeText(mContext, "已在最后", Toast.LENGTH_SHORT).show();
                    return 0;
                }else if (boxUnitIndex ==maxUnitIndexCurrentSection) {
                    //跨节
                    boxUnitIndex = 0;
                    boxSectionIndex++;
                    maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));
                    invalidate();

                    return 11;
                }else {
                    //不跨节
                    boxUnitIndex++;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();

                    return 1;
                }
            case MOVE_NEXT_SECTION:
                if(boxSectionIndex == maxSectionIndex){
                    //已在最后，不移动
                    Toast.makeText(mContext, "已在最后", Toast.LENGTH_SHORT).show();
                    return 0 ;
                }else {
                    //跨节移动到下节首
                    boxUnitIndex = 0;
                    boxSectionIndex++;
                    maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;
                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();

                    return 11;
                }
            case MOVE_LAST_UNIT:
                if(boxUnitIndex == 0&& boxSectionIndex ==0){
                    //已在最前，不移动
                    Toast.makeText(mContext, "已在最前", Toast.LENGTH_SHORT).show();
                    return 0;
                }else if(boxUnitIndex == 0) {
                    //跨节移到上节末尾
                    boxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;
                    boxUnitIndex = maxUnitIndexCurrentSection;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();
                    return -11;
                }else {
                    //本节内移动
                    boxUnitIndex--;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();

                    return -1;
                }

            case MOVE_LAST_SECTION:
                if(boxSectionIndex ==0){
                    //已在最前，不移动
                    Toast.makeText(mContext, "已在最前", Toast.LENGTH_SHORT).show();
                    return 0 ;
                }else{
                    //跨节移到上节首
                    boxSectionIndex--;
                    maxUnitIndexCurrentSection = drawingUnits.get(boxSectionIndex).size()-1;
                    boxUnitIndex = 0;

                    //移动到中心（如果超出绘制区）
                    checkIsBoxOutOfUiAndShiftAllHorizontally((drawingUnits.get(boxSectionIndex).get(0)));

                    invalidate();

                    return  -19;

                }
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


        }
        return 0;
        *//*解释
        * 1,：移动到下一个位置
        * -1：向上，同理；
        * 11：移动到下一节节首
        * -11：移动到上一节节末
        *  -19：移动到上一节节首。
        *  -18：首节被删除后，移动到新首节的首位。
        *  0：未发生移动
        *  20：最后一节的节首
        *  -25：处于锁定模式不能移动蓝框
        * *//*


    }*/

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


    public void curveModeOn(){
        this.curveModeOn = true;
        //将蓝框位置保存
        blueBoxUnitIndex = boxUnitIndex;
        blueBoxSectionIndex = boxSectionIndex;

        //尝试将当前框位减一（向左移）（当处于首节首位时不产生移动）
        moveBox(MOVE_LAST_UNIT);//框位指示器已经变化（如果可以的话）
        //新框位赋值给橘色框
        orangeBoxUnitIndex = boxUnitIndex;
        orangeBoxSectionIndex = boxSectionIndex;

        //重绘，此时curveMode已打开，需要绘制橘色框+蓝色框
        invalidate();//但dUs信息并不改变不需initDw.

    }

    public void curveBoxMove(int moveType){
        moveBox(moveType);
        orangeBoxUnitIndex = boxUnitIndex;
        orangeBoxSectionIndex = boxSectionIndex;
        invalidate();
    }

    public void setCurveModeCancel(){
        //取消弧线模式（只退出，不添加）
        this.curveModeOn =false;

        //将框位指示器重置回蓝框的位置
        boxUnitIndex = blueBoxUnitIndex;
        boxSectionIndex = blueBoxSectionIndex;

        invalidate();
    }

    public void setCurveModeFinishOK(){
        //取消弧线模式（退出，添加，绘制弧线）
        this.curveModeOn =false;
        //将框位指示器重置回蓝框的位置
        boxUnitIndex = blueBoxUnitIndex;
        boxSectionIndex = blueBoxSectionIndex;

        //添加在外部进行，对rbC的编码字段直接修改。
        //此时编码已发生改变，需要重置绘制数据（至少各dU所对应的在原始编码中的索引值已变化，需重新计算）
        codeChangedReDraw();//方法内自带invalidate()
    }

    public int getOrangeIndex(){
        return drawingUnits.get(orangeBoxSectionIndex).get(orangeBoxUnitIndex).indexInCodeSerial;
    }//【提示】如果调用方发现橘框位置和蓝框位置一致，则应拒绝添加弧线标记！二者不一致时，注意选择在其中稍大者后面添加。

    //【注意】如果在弧线模式接近结束时获取蓝框的值，应在调用结束方法前调用本方法；
    // 否则蓝框索引会被结束方法重置。
    public int getBlueIndex(){
        return drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex).indexInCodeSerial;
    }

    public int getGreenA_Index(){
        return drawingUnits.get(green_A_BoxSectionIndex).get(green_A_BoxUnitIndex).indexInCodeSerial;
    }//【提示】如果调用方发现绿框两端的位置一致（都和蓝框一致），则应拒绝合并操作。

    public int getGreenB_Index(){
        return drawingUnits.get(green_B_BoxSectionIndex).get(green_B_BoxUnitIndex).indexInCodeSerial;
    }


    /* 绿框操作区*/
    //【绿、橘不能共存】
    public void mergeFreeModeOn(){
        //【还应该考虑如果橘色框模式未退出，应该先触发其cancel。该逻辑或由外部调用方负责；或二者都有负责双保险】
        if(curveModeOn){
            setCurveModeCancel();
        }

        this.mergeFreeModeOn = true;
        //将蓝框位置保存
        blueBoxUnitIndex = boxUnitIndex;
        blueBoxSectionIndex = boxSectionIndex;

        //绿框模式初始时和蓝框同位置。
        green_A_BoxSectionIndex = boxSectionIndex;
        green_B_BoxSectionIndex = boxSectionIndex;
        green_A_BoxUnitIndex = boxUnitIndex;
        green_B_BoxUnitIndex = boxUnitIndex;
        //重绘，此时mFM已打开，需要绘制绿框+蓝色框
        invalidate();//但dUs信息并不改变不需initDw.

    }

    /* 本方法在进入合并模式后（点击合并的左右两键之一），在mFMO方法调用完毕后随即调用（或right方法，取决于所按按钮）*/
    //作用就是将左/右端坐标指针与移动指针绑定。
    public void mergeFreeModeChoseLeft(){
        //选定左侧，右侧解绑
        if(!leftOn){
            //当前是右侧绑定
            //解绑
            //先保存已有右侧位置
            green_B_BoxSectionIndex = boxSectionIndex;
            green_B_BoxUnitIndex = boxUnitIndex;

            //位置指针重置到左侧
            boxSectionIndex = green_A_BoxSectionIndex;
            boxUnitIndex = green_A_BoxUnitIndex;

            leftOn = true;//标记置左，当移动发生时，更新的位置将赋给左侧
        }//else 已经是左侧，则无操作。

        //此时没有显示上的实质改变，不修改UI
    }

    public void mergeFreeModeChoseRight(){
        if(leftOn){
            //当前是左侧绑定
            //解绑
            //先保存已有左侧位置
            green_A_BoxSectionIndex = boxSectionIndex;
            green_A_BoxUnitIndex = boxUnitIndex;

            //位置指针重置到右侧
            boxSectionIndex = green_B_BoxSectionIndex;
            boxUnitIndex = green_B_BoxUnitIndex;

            leftOn = false;//标记置右，当移动发生时，更新的位置将赋给右侧
        }//else 已经是该侧，则无操作。

        //此时没有显示上的实质改变，不修改UI
    }


    public void merge_BoxMove(int moveType){
        int tempSI = boxSectionIndex;//如果移动操作非法时，用本数据还原。
        int tempUI = boxUnitIndex;

        moveBox(moveType);

        if(leftOn) {
            //判断移动后的坐标是否合法
            if(boxSectionIndex>blueBoxSectionIndex){
                //非法移动，坐标退回，方法退出
                boxSectionIndex = tempSI;
                boxUnitIndex = tempUI;
                Toast.makeText(mContext, "移动不能跨越蓝框。", Toast.LENGTH_SHORT).show();
                return;
            }else if(boxSectionIndex == blueBoxSectionIndex && boxUnitIndex>blueBoxUnitIndex){
                //非法移动，坐标退回，方法退出
                boxSectionIndex = tempSI;
                boxUnitIndex = tempUI;
                Toast.makeText(mContext, "移动不能跨越蓝框。", Toast.LENGTH_SHORT).show();
                return;
            }//else再然后的情形，从移动角度来说都是合理的，即使移动到蓝框位置（此时只是在结束并提交时，由外部检查合并选区不合理，拒绝合并即可）

            //移动后的坐标赋给左侧
            green_A_BoxSectionIndex = boxSectionIndex;
            green_A_BoxUnitIndex = boxUnitIndex;
        }else {
            //判断移动后的坐标是否合法
            if(boxSectionIndex<blueBoxSectionIndex){
                //非法移动，坐标退回，方法退出
                boxSectionIndex = tempSI;
                boxUnitIndex = tempUI;
                Toast.makeText(mContext, "移动不能跨越蓝框。", Toast.LENGTH_SHORT).show();
                return;
            }else if(boxSectionIndex == blueBoxSectionIndex && boxUnitIndex<blueBoxUnitIndex){
                //非法移动，坐标退回，方法退出
                boxSectionIndex = tempSI;
                boxUnitIndex = tempUI;
                Toast.makeText(mContext, "移动不能跨越蓝框。", Toast.LENGTH_SHORT).show();
                return;
            }
            //合法移动下，
            //移动后的坐标赋给右侧
            green_B_BoxSectionIndex = boxSectionIndex;
            green_B_BoxUnitIndex = boxUnitIndex;
        }
        invalidate();
    }

    public void setMergeFreeModeCancel(){
        //取消自由合并模式（只退出，不添加）
        this.mergeFreeModeOn =false;

        //将框位指示器重置回蓝框的位置
        boxUnitIndex = blueBoxUnitIndex;
        boxSectionIndex = blueBoxSectionIndex;

        invalidate();
    }

    public void setMergeFreeModeFinishOK(){
        //取消自由合并模式（只退出，不添加）
        this.mergeFreeModeOn =false;

        //合并的结束相对复杂，由外部调用方先获取合并区的起止索引，根据正确规则将索引区内的编码全部
        // 合并为一个音符对应的编码（如果产生延音则占据多个编码位）；拍结束、节结束要重新安置；
        // 如果当中存在连音弧则也要处理；音高序列也需要处理（方案1,合并后的音高采用原绿框左端位对应
        // 的音高；方案2，存在不同音高时拒绝合并（不太好啊，或者给提示后可以强行合并））
        //——仍然是由外部负责编码序列的改写操作。完事后由本方法收尾（重置位置指针、刷新UI）


        //合并后，框位指示器重置回绿框的左侧位置（合并区首位）
        boxUnitIndex = green_A_BoxUnitIndex;
        boxSectionIndex = green_A_BoxSectionIndex;

        //添加在外部进行，对rbC的编码字段直接修改。
        //此时编码已发生改变，需要重置绘制数据（至少各dU所对应的在原始编码中的索引值已变化，需重新计算）
        codeChangedReDraw();//方法内自带invalidate()
    }


}
