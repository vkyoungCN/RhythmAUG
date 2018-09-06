package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;

import java.util.ArrayList;

public class RhythmSingleLineView extends BaseRhythmView {
//* 单行模式，绘制中的小节位于屏幕中。
    static final String TAG = "RhythmSingleLineView";

    //以下是基类没有，而本类特有的画笔
    /* 用于滑动*/
    Paint maskPaint;
    Paint slidingBallPaint;
    Paint slidingVerticalBarPaint;
    Paint slidingVerticalBarCenterBoxPaint;

    float h_shiftedAmount = 0;//记录移动产生的累计量（可增可减），


    /* 尺寸组，特有：用于滑动 */
    float slidingVerticalBarShort;
    float slidingVerticalBarMedium;
//    float slidingVerticalBarLong;
    float slidingVerticalBarGap;
    float slidingBallDiameter;


    /* 色彩组，特有：用于滑动 */
    int slidingMask_white;
    int slidingBall_pink;
    int slidingVerticalBar_black;


    /* 滑动交互所需变量*/
    boolean isSlidingModeOn =false;
    float totalRequiredLength = 0;
    boolean noNeedToSliding = false;

    ArrayList<VerticalBar> verticalBars;//滑动时的刻度
    RectF clickingBallRectF;
    RectF slidingBarCenterBox;
    int leftEndAddedAmount = 0;//判断刻度位置（以便绘制长线的移动后位置）
    int slidingAmount =0;//累加量（用于记录滑动量，在反向滑回时，最多滑回到0；向后滑动时最大滑动到所需总长值（sA*uW））



    //用于进入滑动模式时的刻度短线绘制
    class VerticalBar{
        float x;
        float top;
        float bottom;

        VerticalBar(float x, float top, float bottom) {
            this.x = x;
            this.top = top;
            this.bottom = bottom;
        }
    }


    public RhythmSingleLineView(Context context) {
        super(context);
    }

    public RhythmSingleLineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
//        init(attributeSet);//【父类的构造器中会调用init方法，而且因为本子类有覆写实现，因而实际调用
// 的是本子类的。（实际测试知。）因而这里不需再调用一次（否则会发现有两次调用记录）】
    }

    public RhythmSingleLineView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
    }


    void init(AttributeSet attributeset) {
        initSizeAndColor();
        initPaint();
        initViewOptions();//当本类中没有该方法实现时，会转而调用基类的实现
    }


    void initSizeAndColor() {
        super.initSize();//需要先调用基类以便完成共有变量的初始
        super.initColor();

        //本类特有
        slidingVerticalBarShort = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        slidingVerticalBarMedium = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
//        slidingVerticalBarLong = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        slidingVerticalBarGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        slidingBallDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, getResources().getDisplayMetrics());

        slidingMask_white = ContextCompat.getColor(mContext, R.color.rhythmView_sdMaskWhite);
        slidingBall_pink = ContextCompat.getColor(mContext, R.color.rhythmView_sdBallPink);
        slidingVerticalBar_black =  ContextCompat.getColor(mContext, R.color.rhythmView_sdVBarBlack);
    }

    void initPaint() {
        super.initPaint();

        //特有部分
        maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setStrokeWidth(4);
        maskPaint.setAlpha(70);
        maskPaint.setColor(slidingMask_white);

        slidingBallPaint = new Paint();
        slidingBallPaint.setStyle(Paint.Style.FILL);
        slidingBallPaint.setStrokeWidth(2);
        slidingBallPaint.setAntiAlias(true);
        slidingBallPaint.setColor(slidingBall_pink);

        slidingVerticalBarPaint = new Paint();
        slidingVerticalBarPaint.setStyle(Paint.Style.FILL);
        slidingVerticalBarPaint.setStrokeWidth(2);
        slidingVerticalBarPaint.setAntiAlias(true);
        slidingVerticalBarPaint.setColor(slidingVerticalBar_black);

        slidingVerticalBarCenterBoxPaint = new Paint();
        slidingVerticalBarCenterBoxPaint.setStyle(Paint.Style.STROKE);
        slidingVerticalBarCenterBoxPaint.setStrokeWidth(2);
        slidingVerticalBarCenterBoxPaint.setAntiAlias(true);
        slidingVerticalBarCenterBoxPaint.setColor(slidingBall_pink);
    }


    /* onSizeChange方法直接使用基类的，一模一样。且本类不是View的直接子类，无需实现onSc*/


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //下方是特有的行为
        //如果进入了滑动模式
        if(isSlidingModeOn){
            //【不调用slidingStart方法的话，是不会进入此模式的，点击就是无效的】
            //滑动模式下，额外复制一层遮罩；以及遮罩上方的小球、刻度
            //【当滑动开始后（滑动了一定程度），设置新的滑动中绘制参数即可；（而且下方各小节的绘制位置信息也产生了改变）】

            //绘制半透明背景遮罩
            canvas.drawRect(0,0,sizeChangedWidth,sizeChangedHeight,maskPaint);

            //绘制小圆点（小圆点停留在原地，在一次滑动中不随手指移动）
            canvas.drawArc(clickingBallRectF,0,360,true,slidingBallPaint);

            //绘制上方标线
            for (VerticalBar vb: verticalBars) {
                canvas.drawLine(vb.x, vb.top, vb.x, vb.bottom, slidingVerticalBarPaint);
            }

            //绘制标线中央框
            canvas.drawRect(slidingBarCenterBox,slidingVerticalBarCenterBoxPaint);

        }
//            invalidate();
    }

    @Override
    void initDrawingUnits(boolean isTriggerFromSC){
        initDrawingUnits_step1(h_shiftedAmount);//因为要调用的方法虽然与基类同名但实际签名不同从而无法由基类直接调用。
        //所以在此重写了主initDU

        initDrawingUnits_step2();
        if(!isTriggerFromSC){
            invalidate();
        }//onSC方法返回后会自动调用onD因而没必要调用invalidate方法。

    }


    /* 设置方法使用基类的;绘制信息的入口方法也使用基类的*/

    /* 以下方法是对基类同名方法的覆写（绘制信息计算第一部分方法）
    【注意如果基类方法保留的private，而前一调用方法又是基类的，则将会直接调用基类同名方法】*/
    void initDrawingUnits_step1(float h_shiftedAmount) {
        Log.i(TAG, "initDrawingUnits_step1: RhSingleLineView");
        super.initDrawingUnits_step1();

        totalRequiredLength = 0;//每次重新计算绘制信息前要清空。

        //确定绘制的Y
        //找中心高度
        float center = (sizeChangedHeight/2);
        //中心对齐到无词状态的底部，的相应的顶部位置。
        topDrawing_Y = center-unitHeight-curveOrLinesHeight*2-additionalPointsHeight*2;


        //开始计算绘制信息。以小节为单位进行。
        //如果有歌词信息，则要（将各字）附加在各Du中。
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));

            //如果不换行，则不需复杂的计算逻辑，直接向后扩展即可
            if(i == 0){
                //第一小节
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, 1,padding+h_shiftedAmount, unitWidth);
                drawingUnits.add(sectionDrawingUnit);

            }else {
                float startX = drawingUnits.get(i-1).get(drawingUnits.get(i-1).size()-1).right+beatGap;
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, 1,startX, unitWidth);
                drawingUnits.add(sectionDrawingUnit);
            }

            //记录到所需总长度
            totalRequiredLength += sectionRequiredLength;//【仍然需要，最终滑动时需要】
        }
    }


    //与调用方的交互
    //在刚刚按下时调用本方法，绘制出①半透明白色遮罩；②按压位置处的粉红小圆；③上方刻度；（下方节奏照样绘制）
    public void slidingStart(float x, float y){
        if(totalRequiredLength < (sizeChangedWidth-2*padding)){
            //没有滑动的必要，不再执行后续动作
            noNeedToSliding = true;
            isSlidingModeOn = false;//onDraw中通过此变量判断是否绘制遮罩及滑动组件。
        }else {
            noNeedToSliding = false;
            isSlidingModeOn = true;
            //要绘制出相应的状态

            //刻度计数器清零备用
            leftEndAddedAmount = 0;

            //在此计算各数据
            clickingBallRectF = new RectF(x-slidingBallDiameter/2,y-slidingBallDiameter/2,x+slidingBallDiameter/2,y+slidingBallDiameter/2);

            verticalBars = new ArrayList<>(19);
            //计划绘制①中央及两侧中长线*3；③两侧短线共四组*16
            //从左向右添加
            float middleX = sizeChangedWidth/2;
            //要先确定中央位置
            for(int i=0;i<19;i++) {
                if (i == 4 || i == 9 || i == 14) {
                    //中长线
                    new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding + slidingVerticalBarMedium);
                } else {
                    //短线
                    new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding + slidingVerticalBarShort);
                }
                //滑动后，更新：①底层的节奏数据各X坐标；②上方刻度（每touch一次重置一回中心线，否则（如多次调用本方法）只是改变其余各线的状态，模拟滑动刻度）
            }

            //套在中央标线上的框
            slidingBarCenterBox = new RectF(middleX-5,padding-2,middleX+5,padding+slidingVerticalBarMedium+3);

        }

        invalidate();//别忘了哦，这样才能绘制出去
    }


    //滑动到达一定程度后，更新绘制（相当于滑动到了一个新刻度）
    public void slidingChange(boolean isToLeft){
        //一次按下手指到抬起手指前的所有滑动操作属于一个事件，在调用方，通过判断滑动量，离散式的调用到
        //本方法，每次传递一个差分的量，而不是传递累积的量；因而这边可直接在前次修改的基础上再次修改。
        //暂定只传递向左or向右滑动【每次滑动一个标准drawingUnit宽度】（暂不设计得太精确，毕竟自己用且急用）
        if(noNeedToSliding) {
            return;
        }

        if(isToLeft){
            //向左是向后？（展示后面的内容？）
            if(slidingAmount*unitWidth>=(totalRequiredLength-unitWidth)){
                //已滑到最后(还剩最后一个字符宽)，不可继续。
                return;
            }
            leftEndAddedAmount++;
        }else {
            //回滑
            if(slidingAmount==0){
                //到头，不可继续。
                return;
            }
            leftEndAddedAmount--;
        }//根据该值绘制稍长刻度的位置

        float middleX = sizeChangedWidth/2;

        //改变刻度线序列绘制位置
        for(int i=0;i<19;i++) {//仍然只绘制19条
            if (i == (4+(leftEndAddedAmount%5)) || i == (9+(leftEndAddedAmount%5)) || i == (14+(leftEndAddedAmount%5))) {
                //中长线
                new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding + slidingVerticalBarMedium);
            } else {
                //短线
                new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding + slidingVerticalBarShort);
            }
            //滑动后，更新：①底层的节奏数据各X坐标；②上方刻度（每touch一次重置一回中心线，否则（如多次调用本方法）只是改变其余各线的状态，模拟滑动刻度）
        }
        //红色圆点+中央框的信息不变，不修改

        float shiftAmount_X = leftEndAddedAmount*unitWidth;//单行模式下采用标准宽度，无压缩、扩展因而移动时直接按标准宽移动即可。
       //改变下层数据绘制位置
        for (ArrayList<DrawingUnit> duInSections:drawingUnits) {
            for (DrawingUnit du :duInSections) {
                du.shiftEntirely(shiftAmount_X,0,padding,padding,sizeChangedWidth-padding,sizeChangedHeight-padding);
            }
        }

        invalidate();
    }

    //手指抬起、单次滑动结束
    public void slidingEnd(){
        //此时底层dus已经移动到新位置，且不需改变（若要回移，应由下次触屏触发）
        if(noNeedToSliding) {
            return;
        }

        //刻度计数器清零
        leftEndAddedAmount = 0;

        //释放，下次点击时重新实例化各绘制信息组件。
        clickingBallRectF = null;
        verticalBars = null;
        slidingBarCenterBox = null;

        //单次滑动结束，标志变量置否
        isSlidingModeOn = false;

        invalidate();//别忘了哦，这样才能绘制出去
    }


}
