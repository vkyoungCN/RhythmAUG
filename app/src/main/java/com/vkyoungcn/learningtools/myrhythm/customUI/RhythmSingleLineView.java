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

public class RhythmSingleLineView extends RhythmView {
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
//* 在所有小节之后标示一个+号。
//* 单行模式，绘制中的小节位于屏幕中。

    private static final String TAG = "RhythmSingleLineView";


    //以下是基类没有，而本类特有的画笔
    private Paint maskPaint;
    private Paint slidingBallPaint;
    private Paint slidingVerticalBarPaint;
    private Paint slidingVerticalBarCenterBoxPaint;


    /* 尺寸组 */

    private float slidingVerticalBarShort;
    private float slidingVerticalBarMedium;
//    private float slidingVerticalBarLong;
    private float slidingVerticalBarGap;
    private float slidingBallDiameter;


    /* 色彩组 */

    private int slidingMask_white;
    private int slidingBall_pink;
    private int slidingVerticalBar_black;


    /* 滑动交互所需变量*/
    private boolean isSlidingModeOn =false;
    private float totalRequiredLength = 0;
    private boolean noNeedToSliding = false;

    private ArrayList<VerticalBar> verticalBars;//滑动时的刻度
    private RectF clickingBallRectF;
    private RectF slidingBarCenterBox;
    private int leftEndAddedAmount = 0;//判断刻度位置（以便绘制长线的移动后位置）



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
//        mContext = context;
        init(null);
//        this.listener = null;
//        Log.i(TAG, "RhythmSingleLineView1: mContext="+mContext);
    }

    public RhythmSingleLineView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
//        mContext = context;
//        init(attributeset);//【父类的构造器中会调用init方法，而且因为本子类有覆写实现，因而实际调用
//// 的是本子类的。（实际测试知。）因而这里不需再调用一次（否则会发现有两次调用记录）】
//        this.listener = null;
//        Log.i(TAG, "RhythmSingleLineView2: mContext="+mContext);
    }


    public RhythmSingleLineView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
//        mContext = context;
//        Log.i(TAG, "RhythmSingleLineView3: mContext="+mContext);
        init(attributeset);
//        this.listener = null;
    }


    void init(AttributeSet attributeset) {
//        Log.i(TAG, "init: Child INIT");【已确定，基类构造器中对init的调用（基类、子类同名方法）实际
// 的调用目标是这里，本方法。而基类中的同名方法未得到调用（log未显示）】
        //【而如果基类中的同名方法使用了private标记（同时子类方法无修饰符）则不会调用子类方法（直接调用父类私有方法），
        // 暂不懂这样设计的原因。（原理暂不清楚：是优先选择限制更严格的方法嘛（子类的必须比基类宽松或相等））】
        initSizeAndColor();
        initPaint();
        initViewOptions();//当本类中没有该方法实现时，会转而调用基类的实现
    }


    void initSizeAndColor() {
        super.initSize();//需要先调用基类以便完成共有变量的初始
        super.initColor();
//        Log.i(TAG, "initSizeAndColor: ");【测试已确定是经过基类的构造方法中对init的调用到达了本方法】]
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


    /* onSizeChange方法直接使用基类的，一模一样。而且本类不是View的直接子类，无需实现onSc*/



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


    /* 设置方法使用基类的*/


    /* 以下方法是对基类同名方法的覆写【注意如果基类方法保留的private，而前一调用方法又是基类的，
    则将会直接调用基类同名方法】*/
    void initDrawingUnits(boolean isTriggerFromOnSC) {
        totalRequiredLength = 0;//每次重新计算绘制信息前要清空。

        //装载绘制信息的总列表（按小节区分子列表管理）
        drawingUnits = new ArrayList<ArrayList<DrawingUnit>>();//初步初始（后面采用add方式，因而不需彻底初始）

        float bottomDrawing_Y = (sizeChangedHeight/3)*2;

        //开始计算绘制信息。以小节为单位进行。
        //如果有歌词信息，则要（将各字）附加在各Du中。

        int accumulateSizeBeforeThisSection = 0;//用于在一维的总表中定位（按小节组织的）二维表中的位置
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));

            //如果不换行，则不需复杂的计算逻辑，直接向后扩展即可
            if(i == 0){
                //第一小节
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit_singleLineMode(codesInSections.get(i), bottomDrawing_Y, padding, unitStandardWidth,strLyric_1,strLyric_2,0);
                drawingUnits.add(sectionDrawingUnit);
                accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

            }else {
                float startX = drawingUnits.get(i-1).get(drawingUnits.get(i-1).size()-1).right+beatGap;
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit_singleLineMode(codesInSections.get(i), bottomDrawing_Y, startX, unitStandardWidth,strLyric_1,strLyric_2  ,accumulateSizeBeforeThisSection);
                drawingUnits.add(sectionDrawingUnit);
                accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();
            }

            //记录到所需总长度
            totalRequiredLength += sectionRequiredLength;//【仍然需要，最终滑动时需要】
        }

        if(!isTriggerFromOnSC){
            Log.i(TAG, "initDrawingUnits: going to INVALIDATE");
            invalidate();

        }//onSC方法返回后会自动调用onD因而没必要调用invalidate方法。
    }

    //按小节计算（小节内各音符的）绘制数据
    private ArrayList<DrawingUnit> initSectionDrawingUnit_singleLineMode(ArrayList<Byte> codesInThisSection, float bottomDrawing_Y, float sectionStartX, float unitWidthChanged,String lyric_1,String lyric_2,int accumulateUnitsNumBeforeThisSection) {
        // *注意，sectionStartX要传入“上一小节末尾+节间隔”（非首节时）或者传入padding（是首节时）

        int totalValueBeforeThisCodeInBeat = 0;//用于计算拍子【要在循环的末尾添加，因为要使用的是“本音符之前”的总和】
        ArrayList<DrawingUnit> drawingUnitsInSection = new ArrayList<>();

        for (int j = 0; j < codesInThisSection.size(); j++) {
            byte code = codesInThisSection.get(j);

            if (code > 112) {
                //大于112的没有实体绘制单元，而是在其前一单元中设置专用字段
                int curveSpanForward = code - 110;//跨越的单元数量
                //连音线末端可以在小节首音符上，但是连音线标志必然不能是小节第一个，从而可以获取前一du进行设置
                if (j == 0) {
                    Toast.makeText(mContext, "该小节内，连音标记前没有音符，错误编码。略过该连音。", Toast.LENGTH_SHORT).show();
                } else {
                    drawingUnitsInSection.get(j - 1).isEndCodeOfLongCurve = true;
                    drawingUnitsInSection.get(j - 1).curveLength = curveSpanForward;
                }
            } else {
                DrawingUnit drawingUnit = new DrawingUnit();
                //绘制在同一行内
                drawingUnit.bottomNoLyric = bottomDrawing_Y;
                drawingUnit.top = drawingUnit.bottomNoLyric - (unitStandardHeight + additionalHeight * 2 + curveOrLinesHeight * 2);


                if (j == 0) {
                    drawingUnit.left = sectionStartX;//首个音符
                } else {
                    //非首位音符,根据前一音符是否是拍尾，而紧靠或有拍间隔。
                    if (totalValueBeforeThisCodeInBeat == 0) {
                        //前一音符为拍尾//(如果不是首音符，则前面必然是有音符的，所以下句可行)
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
//                        Log.i(TAG, "initSectionDrawingUnit_singleLineMode: hasGap");
                        //【注意间隔是不能计算在du之内的，要在外面。因为下划线布满du内的宽度】
                    } else if (codesInThisSection.get(j - 1) > valueOfBeat) {
                        //此时，前一音节是大附点，此时本音节前方有累加时值（按当前的时值计算逻辑），按if判断应当紧靠，但事实上属于一个新拍子
                        // 因而需要有间隔；
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
                    } else {
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right;//紧靠即可
//                        Log.i(TAG, "initSectionDrawingUnit_singleLineMode: NoGap");

                    }
                }

                //单元的右侧在后面分条件（按不同类型的code）确定。独占一拍时，拍间隔也不计入宽度（毕竟不能在间隔上绘制下划线）【但是附点、均分多连音的宽度有所变化】

                //绘制下划线、音符和附点；或延音线；或均分多连音（由于valueOfBeat不是常量，不能用sw判断。case分支要求使用常量条件）
                //字符处理（后面还有对均分多连音字符的处理）
                String charForCode = "X";
                if (code < 0) {
                    charForCode = "0";
                } else if (code == 0) {
                    charForCode = "-";
                }//如果是旋律绘制，这里改成相应数字即可

                //字符的绘制位置（字符按照给定中下起点方式绘制）
                //【为了保证①字符基本位于水平中央、②字符带不带附点时的起始位置基本不变，因而采用：左+三分之一单位宽度折半值 方式，后期据情况调整】
                drawingUnit.codeCenterX = drawingUnit.left + unitWidthChanged / 2;//。
                drawingUnit.codeBaseY = drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight - 8;//暂定减8像素。
                //这样所有长度的字串都指定同一起始点即可。

                if (code == valueOfBeat + valueOfBeat / 2 || code == -valueOfBeat - valueOfBeat / 2) {
                    //大附点，无下划线
                    //单位右边缘
                    drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                    drawingUnit.code = charForCode + "·";

                } else if (code == valueOfBeat || code == -valueOfBeat) {
                    //独立音符
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.code = charForCode;
                } else if (code == valueOfBeat / 2 || code == -valueOfBeat / 2) {
                    //仅有一条下划线
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.code = charForCode;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                } else if (code == valueOfBeat / 2 + valueOfBeat / 4 || code == -valueOfBeat / 2 - valueOfBeat / 4) {
                    //一线、一附点的画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                    drawingUnit.code = charForCode + "·";
                } else if (code == valueOfBeat / 4 || code == -valueOfBeat / 4) {
                    //两线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10));
                    drawingUnit.code = charForCode;

                } else if (code == valueOfBeat / 4 + valueOfBeat / 8 || code == -valueOfBeat / 4 - valueOfBeat / 8) {
                    //两线、一附点
                    drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10));
                    drawingUnit.code = charForCode + "·";
                } else if (code == valueOfBeat / 8 || code == -valueOfBeat / 8) {
                    //三线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 20, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 20));
                    drawingUnit.code = charForCode;
                } else if (code == 0) {
                    //独立占拍延音线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.code = charForCode;
                }

                //均分多连音画法（仅数字部分，顶部圆弧另外处理）
                //音符数量处理
                if (code > 73 && code < 99) {
                    //有几个音符
                    StringBuilder codeBuilder = new StringBuilder();
                    int codeNum = code % 10;
                    for (int k = 1; k < codeNum; k++) {
                        codeBuilder.append(X);
                    }
                    drawingUnit.right = drawingUnit.left + (unitWidthChanged / 2) * (codeNum);
                    drawingUnit.code = codeBuilder.toString();
                    drawingUnit.mCurveNumber = codeNum;
                    drawingUnit.mCurveNumCenterX = (drawingUnit.right - drawingUnit.left) / 2 + drawingUnit.left;
                    drawingUnit.mCurveNumBaseY = drawingUnit.top + additionalHeight + curveOrLinesHeight / 3;//【注意，稍后画弧线时，顶部也应留出1/3距离】

                    //下划线处理
                    if (valueOfBeat == 16) {
                        //73~79之间无下划线不处理。
                        if (code > 83 && code < 89) {
                            //一条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                        } else if (code > 93) {//外层if有<99判断
                            //两条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight + 10));
                        }//不支持三条的
                    } else if (valueOfBeat == 8) {//外层if有<99判断
                        if (code > 93) {
                            //一条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight));
                        }//无下划线的不处理；八分音符下不可能有四分时值的均分多连音，不处理。
                    }
                }

                //在这一层循环的末尾，将本音符的时值累加到本小节的记录上；然后更新“tVBTCIS”记录以备下一音符的使用。
                totalValueBeforeThisCodeInBeat = addValueToBeatTotalValue(code, valueOfBeat, totalValueBeforeThisCodeInBeat);
//                Log.i(TAG, "initSectionDrawingUnit_singleLineMode: totalValueBeforeThisCodeInBeat="+totalValueBeforeThisCodeInBeat);
                drawingUnitsInSection.add(drawingUnit);//添加本音符对应的绘制信息。

                if(lyric_1!=null&&!lyric_1.isEmpty()){
                    drawingUnit.word_1 = lyric_1.substring((accumulateUnitsNumBeforeThisSection+j),(accumulateUnitsNumBeforeThisSection+j));
                    drawingUnit.word_1_BaseY = drawingUnit.bottomNoLyric + unitStandardHeight;
                    drawingUnit.word_1_CenterX = drawingUnit.codeCenterX;
                }
                if(lyric_2!=null&&!lyric_2.isEmpty()){
                    drawingUnit.word_2 = lyric_1.substring((accumulateUnitsNumBeforeThisSection+j),(accumulateUnitsNumBeforeThisSection+j));
                    drawingUnit.word_2_BaseY = drawingUnit.bottomNoLyric + unitStandardHeight*2;
                    drawingUnit.word_2_CenterX = drawingUnit.codeCenterX;
                }

                drawingUnit.checkIsOutOfUi(padding,padding,sizeChangedWidth-padding,sizeChangedHeight-padding);
            }

        }
        return drawingUnitsInSection;//返回本小节对应的绘制信息列表
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
            leftEndAddedAmount++;
        }else {
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

        float shiftAmount_X = leftEndAddedAmount*unitStandardWidth;//单行模式下采用标准宽度，无压缩、扩展因而移动时直接按标准宽移动即可。
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
