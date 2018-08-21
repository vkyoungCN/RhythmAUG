package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;


public class RhythmView extends View {
//* 显示节奏序列
//* 有“折行、单行”两种模式；字符及字宽（暂时）手动设置数值
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。
//*
//* 三种工作模式：“节奏/旋律/有词”
//* 其中有词模式下需要绘制上方连音弧线；旋律模式下需要绘制上下方的加点。
//*

    private static final String TAG = "RhythmView";

    Context mContext;

    private ArrayList<Byte> rhythmCodes; //数据源，节奏序列的编码。根据该数据生成各字符单元上的绘制信息。
    int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
    int valueOfBeat = 16;
    int valueOfSection = 64;

    ArrayList<ArrayList<Byte>> codesInSections;//对数据源进行分析处理之后，按小节归纳起来。便于进行按节分行的判断。
    ArrayList<ArrayList<DrawingUnit>> drawingUnits;//绘制数据也需要按小节组织，以便与按小节组织的代码一并处理。
    String strLyric_1;
    String strLyric_2;

    private float topYDistanceBetweenTwoLine;//全局化以便可以提前处理，避免在for循环中多次处理。

    /* 设置参数*/
    //设置参数与数据源一并设置
    private boolean useLyric_1 =false;
    private boolean useLyric_2 =false;

    private boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
    private boolean useMultiLine = true;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）


    //    private boolean isDataInitBeInterruptedBecauseOfNoSize = false;

    /* 画笔组*/
    Paint bottomLinePaint;
    Paint codePaint;
    Paint curveNumPaint;//用于绘制均分多连音弧线中间的小数字，其字符尺寸较小
    Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。
    Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
//    private Paint textWaitingPaint;

    /* 尺寸组 */
    float padding;
    float unitStandardWidth;//24dp。单个普通音符的基准宽度。【按此标准宽度计算各节需占宽度；如果单节占宽超屏幕宽度，则需压缩单节内音符的占宽；
    // 如果下节因为超长而移到下一行，且本行剩余了更多空间，则需要对各音符占宽予以增加（但是字符大小不变）】
    float unitStandardHeight;//24dp。单个普通音符的基准高度。
    float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），暂定12dp。
    //注意，一个节拍内的音符之间没有额外间隔。
//    但是好像无法手动控制X和点之间的间距，因而本参数仅用于让后续字符自然。//暂定半宽

    float lineGap;//不同行之间的间隔。暂定12dp；如果有文字行则需额外安排文字空间。
    float additionalHeight;//用于上下加点绘制的保留区域，暂定6dp
    float curveOrLinesHeight;//用于绘制上方连音线或下方下划线的空间（上下各一份），暂定8dp


    //下划线绘制为2dp(或1dp)每条
    private float textSizeSmall;//14sp
    private float textSizeMedium;//16sp
    private float textSizeLarge;//24sp


    float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
    float curveNumSize;
    float textBaseLineBottomGap;

    int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    int sizeChangedWidth = 0;//未获取数据前设置为0


    /* 色彩组 */
    int generalColor_Gray;
    int generalColor_LightGray;
    int generalCharGray;

    public RhythmView(Context context) {
        super(context);
        mContext = context;
        Log.i(TAG, "RhythmView: mContext="+mContext);
        init(null);
//        this.listener = null;
    }

    public RhythmView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
//        Log.i(TAG, "RhythmView2: mContext="+mContext);
    }


    public RhythmView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
//        Log.i(TAG, "RhythmView3: mContext="+mContext);
        init(attributeset);
//        this.listener = null;
    }

//【当子类调用的父类方法进一步调用了二者都有的同名方法时，测试发现，实际调用的会是子类的】
    void init(AttributeSet attributeset) {
        Log.i(TAG, "init: BaseInit");
        initSize();
        initColor();
        initPaint();
        initViewOptions();
    }


    void initSize() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
//        unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
//        unitStandardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        beatGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        lineGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        additionalHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        curveOrLinesHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        textBaseLineBottomGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        curveNumSize =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
    }

    void initColor(){
        generalColor_Gray = ContextCompat.getColor(mContext, R.color.rhythmView_generalGray);
        generalColor_LightGray = ContextCompat.getColor(mContext, R.color.rhythmView_generalLightGray);
        generalCharGray = ContextCompat.getColor(mContext, R.color.rhythmView_generalCharGray);
    }


    //部分初始化需要使用到由外部调用方设置的尺寸大小，暂时只初始化不需要用到该种数据的
    void initPaint() {
        bottomLinePaint = new Paint();
        bottomLinePaint.setColor(generalCharGray);
        bottomLinePaint.setStrokeWidth(2);//
        bottomLinePaint.setStyle(android.graphics.Paint.Style.STROKE);

        //注意文本的尺寸此时尚未获取到，只能默认的设置为14（36px）.
        codePaint = new Paint();
        codePaint.setTextSize(36);
        codePaint.setStrokeWidth(4);
        codePaint.setColor(generalCharGray);
        codePaint.setAntiAlias(true);
        codePaint.setTextAlign(Paint.Align.CENTER);
        codePaint.setFakeBoldText(true);


        curveNumPaint = new Paint();
        curveNumPaint.setTextSize(curveNumSize);
        curveNumPaint.setStrokeWidth(2);
        curveNumPaint.setColor(generalCharGray);
        curveNumPaint.setAntiAlias(true);
        curveNumPaint.setTextAlign(Paint.Align.CENTER);


        grayEmptyPaint = new Paint();
        grayEmptyPaint.setStyle(Paint.Style.FILL);
        grayEmptyPaint.setStrokeWidth(4);
        grayEmptyPaint.setAntiAlias(true);
        grayEmptyPaint.setColor(generalColor_Gray);

    }


    void initViewOptions() {
//        Log.i(TAG, "initViewOptions: BASE");
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    //【学：据说是系统计算好控件的实际尺寸后以本方法通知用户】
    // 【调用顺序：M(多次)-S(单次)-D】。
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        sizeChangedHeight = h;
        sizeChangedWidth = w;

        if(codesInSections!=null&&!codesInSections.isEmpty()){
            //如果数据源此时非空，则代表数据源的设置早于onSC，也即数据源设置方法中的绘制信息初始化方法被中止，
            //需要再次再次初始化绘制信息（但是传入isTBOSC标记，只初始绘制信息不进行刷新）
            initDrawingUnits(true);
        }//【这里如是单线程则没有问题，如果是多线程，则可能在initDU返回前就调用了onDraw，则可能无法正确绘制，
        // 那样的话取消传入“禁止刷新”的标记即可。（倒是应该不会下标越界，毕竟是for是有判断的）】


        //万一程序对targetCode的设置（数据初始化）早于控件尺寸的确定（则是无法初始化下划线数据的），
        // 则需要在此重新对下划线数据进行设置
        /*if(isDataInitBeInterruptedBecauseOfNoSize){
            isDataInitBeInterruptedBecauseOfNoSize =false;//表示事件/状况已被消耗掉
//            Log.i(TAG, "onSizeChanged: initBL");
            initDrawingUnits(w,rhythmCodes.size());
        }*/

        super.onSizeChanged(w, h, old_w, old_h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //合理的尺寸由外部代码及布局文件实现，这里不设计复杂的尺寸交互申请逻辑，而是直接使用结果。
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));

    }

    boolean checkEmptyAndDraw(Canvas canvas){
        if(codesInSections==null||codesInSections.isEmpty()) {
            //此时节奏数据还未设置，只在中间高度绘制一条背景
            float fromX = padding;
            float fromY = sizeChangedHeight/2-20;
            float toX = sizeChangedWidth - padding;
            float toY = sizeChangedHeight/2+20;
            //条带总宽40像素。

            canvas.drawRect(fromX,fromY,toX,toY, grayEmptyPaint);
            return true;//空，返回真
        }else {
            return false;//有数据，不执行操作，返回假。
        }
    }





    @Override
    protected void onDraw(Canvas canvas) {
        if(checkEmptyAndDraw(canvas)){
            return;
            //是空的，绘制一个空占位线条然后直接退出绘制即可。
        }

//        Log.i(TAG, "onDraw: dUs="+drawingUnits.toString());
        //未退出则继续绘制主体内容
        //逐小节逐音符绘制(主体部分：各乐符、下划线、乐符间隔、拍子间隔、小节间隔、节间小节线、均分多连音、跨音符的上方连音弧线)
        drawByEachUnit(canvas);
        //绘制下方汉字
        drawLyric(canvas);
//            invalidate();
    }

    void drawByEachUnit(Canvas canvas){
        for (int i = 0;i<drawingUnits.size();i++) {
            ArrayList<DrawingUnit> sectionDrawingUnits = drawingUnits.get(i);

            for (int k=0;k<sectionDrawingUnits.size();k++) {
                DrawingUnit drawingUnit = sectionDrawingUnits.get(k);
                if (!drawingUnit.isOutOfUi) {//本字符在可显示区域

                    //字符
                    canvas.drawText(drawingUnit.getCode(), drawingUnit.getCodeCenterX(), drawingUnit.getCodeBaseY(), codePaint);

                    //下划线
                    for (BottomLine bottomLine : drawingUnit.bottomLines) {
                        canvas.drawLine(bottomLine.startX, bottomLine.startY, bottomLine.toX, bottomLine.toY, bottomLinePaint);
                    }

                    //绘制上下加点（旋律模式下）（*如果没有加点则不会进入）
                    for (RectF point : drawingUnit.additionalPoints) {
                        canvas.drawOval(point, bottomLinePaint);
                    }

                    //均分多连音的弧线和弧线内数字
                    if (drawingUnit.mCurveNumber != 0) {
                        //弧中数字
                        canvas.drawText(String.valueOf(drawingUnit.mCurveNumber), drawingUnit.mCurveNumCenterX, drawingUnit.mCurveNumBaseY, curveNumPaint);

                        //弧线
                        canvas.drawArc(drawingUnit.left, drawingUnit.top + additionalHeight + curveOrLinesHeight / 3,
                                drawingUnit.right, drawingUnit.top + additionalHeight + curveOrLinesHeight, 0, 180, false, bottomLinePaint);//是角度数
                        //【椭圆划过的角度待调整】

                    }

                    //如果到达了小节末尾
                    if (k == sectionDrawingUnits.size() - 1) {
//                        Log.i(TAG, "onDraw: section End.");
                        //则要绘制小节末的竖线
                        canvas.drawLine(drawingUnit.right + beatGap / 2, drawingUnit.top + additionalHeight + 2 * curveOrLinesHeight / 3,
                                drawingUnit.right + beatGap / 2, drawingUnit.bottomNoLyric - additionalHeight - curveOrLinesHeight / 3, bottomLinePaint);
                    }
                    //注意，小节线绘制规则：起端没有小节线，小节线只存在于末尾


                    //绘制连音弧线（延长音，不是均分多连）
                    if (drawingUnit.isEndCodeOfLongCurve) {
                        float curveStart = 0;
                        float curveEnd = drawingUnit.right;//覆盖在起端left到末端right（全盖住）

                        //获取弧线起端音符的坐标
                        int span = drawingUnit.curveLength;
                        //（注：k与span的关系）索引为k时：本音符前面有k个音符存在。span则代表本音符之前的span个音符都在弧线下，所以二者一一对应不需加减处理。
                        if (span <= k) {
                            //弧线不跨节
                            curveStart = sectionDrawingUnits.get(k - span).left;
                            canvas.drawArc(curveStart, drawingUnit.top + additionalHeight, curveEnd,
                                    drawingUnit.top + additionalHeight + curveOrLinesHeight,
                                    0, 180, false, bottomLinePaint);//绘制
                        } else {
                            //弧线跨节，可能跨行
                            DrawingUnit duCurveStart = findStartDrawingUnit(k, i, span, drawingUnits);
                            if (duCurveStart == null) {
                                Toast.makeText(mContext, "连音符跨度编码错误，忽略绘制", Toast.LENGTH_SHORT).show();
                                //没有绘制
                            } else {
                                curveStart = duCurveStart.left;
                                if (duCurveStart.top < drawingUnit.top) {
                                    //表示跨行了，分半绘制
                                    //后半段
                                    canvas.drawArc(padding, drawingUnit.top + additionalHeight, drawingUnit.right,
                                            drawingUnit.top + additionalHeight + curveOrLinesHeight,
                                            0, 90, false, bottomLinePaint);
                                    //前半段
                                    canvas.drawArc(duCurveStart.left, duCurveStart.top + additionalHeight, sizeChangedWidth - padding,
                                            duCurveStart.top + additionalHeight + curveOrLinesHeight,
                                            90, 90, false, bottomLinePaint);//sweepA据文档似乎是“扫过”幅度

                                } else {
                                    //整体绘制
                                    canvas.drawArc(curveStart, drawingUnit.top + additionalHeight, curveEnd,
                                            drawingUnit.top + additionalHeight + curveOrLinesHeight,
                                            0, 180, false, bottomLinePaint);

                                }
                            }
                        }
                    }


                }
            }

        }
    }


    /* 绘制歌词。位于乐谱部分的下方，支持一到两行歌词（数据同样已事先存入drawingUnit）*/
    void drawLyric(Canvas canvas) {
        //遍历方式进行
        for (ArrayList<DrawingUnit> drawingUnitsInSections : drawingUnits) {
            for (DrawingUnit drawingUnit : drawingUnitsInSections) {
                if (strLyric_1 != null&&!strLyric_1.isEmpty()) {
                    canvas.drawText(drawingUnit.word_1, drawingUnit.word_1_CenterX, drawingUnit.word_1_BaseY, codePaint);
                }
                if (strLyric_2 != null&&!strLyric_2.isEmpty()) {
                    canvas.drawText(drawingUnit.word_2, drawingUnit.word_2_CenterX, drawingUnit.word_2_BaseY, codePaint);
                }
            }
        }
    }


    //调用本方法时，要求跨度必然大于本节k。否则直接处理即可不需本方法参与。
    public DrawingUnit findStartDrawingUnit(int currentK, int currentSectionIndex, int curveSpan, ArrayList<ArrayList<DrawingUnit>> drawingUnitsInSections){
        int restSpan = curveSpan - currentK;
        DrawingUnit startDrawingUnit = null;

        for(int i = 1;i<=currentSectionIndex;i++) {
            if (restSpan > drawingUnitsInSections.get(currentSectionIndex - i).size()) {
                restSpan = restSpan - (drawingUnitsInSections.get(currentSectionIndex - i).size());
            }else {
                //不大于，代表所要找的起点dU就在本节
                startDrawingUnit = drawingUnitsInSections.get(currentSectionIndex - i).get(drawingUnitsInSections.get(currentSectionIndex - i).size()-restSpan);
            }
        }
        return startDrawingUnit;//如果找不到，这里返回的是空，需要再行处理。
    }



    public void setRhythmViewData(CompoundRhythm compoundRhythm){
//        Log.i(TAG, "setRhythmViewData: cRh="+compoundRhythm.toString());
        setRhythmViewData(RhythmHelper.codeParseIntoSections(compoundRhythm.getRhythmCodeSerial(),compoundRhythm.getRhythmType()),
                compoundRhythm.getRhythmType(),compoundRhythm.getPrimaryLyricSerial(),compoundRhythm.getSecondLyricSerial(),
                18,20,20);
    }

    public void setRhythmViewData(CompoundRhythm compoundRhythm, int codeSize,int unitWidth, int unitHeight){
        setRhythmViewData(RhythmHelper.codeParseIntoSections(compoundRhythm.getRhythmCodeSerial(),compoundRhythm.getRhythmType()),
                compoundRhythm.getRhythmType(),compoundRhythm.getPrimaryLyricSerial(),compoundRhythm.getSecondLyricSerial(),
                codeSize,unitWidth,unitHeight);
    }
    /* 一个简化的外部设置方法，采用默认调校好的尺寸大小*/
    public void setRhythmViewData(ArrayList<ArrayList<Byte>> codesInSections, int rhythmType, String lyric_1, String lyric_2){
        setRhythmViewData(codesInSections,rhythmType,lyric_1,lyric_2,18,20,20);
    }


    /*
     * 方法由程序调用，动态设置目标字串
     * 设置节拍类型（4/4等）
     * 设置字符大小
     * */
    public void setRhythmViewData(ArrayList<ArrayList<Byte>> codesInSections, int rhythmType, String primaryLyricString, String secondLyricString, int codeSize, int unitWidth, int unithHeight){
        this.codesInSections = codesInSections;
//        Log.i(TAG, "setRhythmViewData: codeInSection(MainData)="+codesInSections.toString());
        this.rhythmType = rhythmType;
        this.strLyric_1 = primaryLyricString;
        this.strLyric_2 = secondLyricString;

        int codeMinSize = 12;
        int codeMaxSize = 28;
        if(codeSize<codeMinSize){
            //不允许小于12【暂定】
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeMinSize, getResources().getDisplayMetrics());
        }else if(codeSize>codeMaxSize){
            //不允许大于28【暂定】
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeMaxSize, getResources().getDisplayMetrics());
        }else {
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeSize, getResources().getDisplayMetrics());
        }
        codePaint.setTextSize(textSize);//需要在设置了文字大小后重新设置画笔

        int unitMinSize = 16;
        int unitMaxSize = 32;
        if(unitWidth<unitMinSize){
            //不允许小于18【暂定】
            this.unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMinSize, getResources().getDisplayMetrics());
        }else if(unitWidth>unitMaxSize){
            //不允许大于32【暂定】
            this.unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMaxSize, getResources().getDisplayMetrics());
        }else {
            this.unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitWidth, getResources().getDisplayMetrics());
        }

        if(unithHeight<unitMinSize){
            //不允许小于18【暂定】
            this.unitStandardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMinSize, getResources().getDisplayMetrics());
        }else if(unithHeight>unitMaxSize){
            //不允许大于32【暂定】
            this.unitStandardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMaxSize, getResources().getDisplayMetrics());
        }else {
            this.unitStandardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unithHeight, getResources().getDisplayMetrics());
        }

        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        //以下是各项数据初始化，以及计算绘制信息的内容
        valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        //如果这个时候还没有获取到尺寸信息则终止操作【即暂不进行绘制信息的计算和初始】
        // 在onSizeChanged()中由于尺寸可能发生了改变，因而必须要重新计算绘制信息，即调用initDrawingUnits；
        if(sizeChangedWidth == 0){
            return;
        }//就运行表现来看，似乎是先设置了数据然后才是onSC。

        //主要计算部分：各音符对应绘制单元的绘制信息计算。
        initDrawingUnits(false);
    }


    void initDrawingUnits(boolean isTriggerFromOnSC) {
//        Log.i(TAG, "initDrawingUnits:MULTI+"+isTriggerFromOnSC);

        //本方法计算了大部分内容的绘制坐标，但是还有如下未处理：
        // ①旋律的上下加点画法；
        //
        // 控件的真实绘制起点（是否top对齐）需要在计算出所有数据后，根据所需总长与控件高度的关系
        // （即是否能占满或超出）来决定（如果在控件高度内能够绘制完成所有行，则垂直方向是居中；
        // 否则是top对齐）。如果居中，则需要在计算完成一遍后，以遍历方式对各数据单元进行平移。
        //
        //【如果本方法是从onSc中触发的调用，则最后不进行invalidate()】


        //可用总长（控件宽扣除两侧缩进）
        float availableTotalWidth = sizeChangedWidth - padding * 2;

        //装载绘制信息的总列表（按小节区分子列表管理）
        drawingUnits = new ArrayList<ArrayList<DrawingUnit>>();//初步初始（后面采用add方式，因而不需彻底初始）

        //如果有歌词信息，则还要附加在Du中。（各dU分别对应一个字，无字的du也要对应特别占位符）

        int accumulateSizeBeforeThisSection = 0;//为了能在一维的总表中对应（按小节组织的）二维表中的位置

        float lineRequiredLength = 0;//当前本行所有小节所需的长度
        int sectionAmountInLine = 0;//在本行一共有多少小节（最后一节已经移到下一行，不算）。（使用这个代替索引列表）

        int lineCursor = 0;//当前位于第几行，从0起。（用于折行模式下该行Y值的计算）
        float topDrawing_Y = padding;//临时随意指定的起绘顶点（topY），暂时按top对齐处理

        //两行之间的标准间距，（包含行高在内，是否包含词显示区高度则取决于是否有词）
        topYDistanceBetweenTwoLine=(unitStandardHeight + additionalHeight * 2 + curveOrLinesHeight * 2 + lineGap);
        if(this.strLyric_2 !=null){
            topYDistanceBetweenTwoLine+=(unitStandardHeight*2);
        }else if(this.strLyric_1 !=null){
            topYDistanceBetweenTwoLine+=unitStandardHeight;
        }


        //开始计算绘制信息。以小节为单位进行计算。
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));

            //记录到本行所需总长度
            lineRequiredLength += sectionRequiredLength;

            //本行既有小节数量+1
            sectionAmountInLine++;
//            sectionIndexesOfThisLine.add(i);//本小节在总小节编码列表中的索引,加入到“本行记录”列表中。

            //针对本小节的宽度/本行既有小节总宽度 与 本行的屏幕宽度进行比较判断。
            if (sectionRequiredLength > availableTotalWidth) {
                //单节宽度大于屏幕宽度
                // 如果本行已有其他节则需要压缩本节、扩展其他节；且本节下移
                // 否则（如果本行只有本节自己），则压缩本节，下一节新起一行（行需要宽度重置）
                if (sectionAmountInLine == 1) {
                    //①本行只有本节自己，对本节的基础宽度压缩（不需下移）
                    float unitWidthSingleZipped = (availableTotalWidth / sectionRequiredLength) * unitStandardWidth;//与外部使用的uW变量同名
                    //【在此计算本行本节的绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidthSingleZipped,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    drawingUnits.add(sectionDrawingUnit);
                    accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

                    //②行需求宽度计数器重置，③本行节索引重置
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;
                } else {
                    //本行超过1节。其他节扩展
                    float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength - sectionRequiredLength)) * unitStandardWidth;
                    //计算其他行的绘制数据
                    float sectionStartX = padding;

                    //先对行内首节进行计算
                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i-sectionAmountInLine+1), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    //并添加到总记录
                    drawingUnits.add(sectionDrawingUnit);

                    //其余各节需要依靠前一节的末尾坐标进行自身坐标的计算【仅当本行内（算上下移的那个）的小节数量≥3时才进入循环】
                    //（否则，如果==2，则是一个下移，剩余一个独占整行在之前的“首节处理”逻辑中已完成计算，不需进循环。）
                    for (int k=2;k<sectionAmountInLine;k++) {
                        //计算（剩余在本行的）后续各节。
                        int indexBeingCalculate = i-sectionAmountInLine+k;
                        int indexBeforeCalculate = i-sectionAmountInLine+k-1;
                        //先计算起始X，需要依赖同line中前一节最末音符的右边缘坐标。
                        sectionStartX = drawingUnits.get(indexBeforeCalculate).get(drawingUnits.get(indexBeforeCalculate).size()-1).right+beatGap;
                        ArrayList<DrawingUnit> sectionDrawingUnit_2 = initSectionDrawingUnit(codesInSections.get(indexBeingCalculate), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                        drawingUnits.add(sectionDrawingUnit_2);

                    }
//                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(sectionIndex), topDrawing_Y, lineCursor, padding, unitWidthSingleZipped);


                    //本节下移,独占一行，且压缩本节
                    lineCursor++;
                    float unitWidth_zipped = (availableTotalWidth / sectionRequiredLength) * unitStandardWidth;
                    //【在此计算本节绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit_3 = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidth_zipped,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    drawingUnits.add(sectionDrawingUnit_3);

                    accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

                    //行需求宽度计数器重置(以备下行使用)，本行节索引重置（以备下行使用）
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;

                }
            } else if (lineRequiredLength > availableTotalWidth) {
                //单节宽度不大于控件可用宽度，但是行累加宽度超过了；
                // 本节下移，本行其他节要放大。
                lineCursor++;
                float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength - sectionRequiredLength)) * unitStandardWidth;
                //【在此计算本行绘制数据】
                //计算其他行的绘制数据
                float sectionStartX = padding;
                //先对行内首节进行计算
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i-sectionAmountInLine+1), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                //并添加到总记录
                drawingUnits.add(sectionDrawingUnit);

                //其余各节需要依靠前一节的末尾坐标进行自身坐标的计算【仅当本行内（算上下移的那个）的小节数量≥3时才进入循环】
                //（否则，如果==2，则是一个下移，剩余一个独占整行在之前的“首节处理”逻辑中已完成计算，不需进循环。）
                for (int k=2;k<sectionAmountInLine;k++) {
                    //计算（剩余在本行的）后续各节。
                    int indexBeingCalculate = i-sectionAmountInLine+k;
                    int indexBeforeCalculate = i-sectionAmountInLine+k-1;
                    //先计算起始X，需要依赖同line中前一节最末音符的右边缘坐标。
                    sectionStartX = drawingUnits.get(indexBeforeCalculate).get(drawingUnits.get(indexBeforeCalculate).size()-1).right+beatGap;
                    ArrayList<DrawingUnit> sectionDrawingUnit_2 = initSectionDrawingUnit(codesInSections.get(indexBeingCalculate), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    drawingUnits.add(sectionDrawingUnit_2);
                }

                //本行下移后，尺寸上暂不做特别处理，但需要将本节加入下一行的宽和索引记录。
                //②行需求宽度计数器重置，③本行节索引重置
                lineRequiredLength = sectionRequiredLength;//将下一行的宽度重置为本节宽度，以便后续累加。
                sectionAmountInLine =1;//将本节仍然要位于计数内（因为本节单节不超屏宽，移到下行后，后面可能会有多节）
                // 【因为开头的add只能负责将后续小节的计数进行添加，而本项是无法（以该方式）计入；只有在此手动添加一次】

                //但是要判断此节是否是最后一节，如果是，则应扩展本节占据全行宽度
                // （另一种思路是在后方填充空拍小节，但在“本节超宽下移，剩余节只有一节”分支下，剩余节即使很短，也同样会被扩展；
                // 若在此填充则该情形同样需要填充的逻辑，故目前不予修改，暂时按简单规则进行。）
                //且在此计算本节绘制数据。
                if (i == codesInSections.size() - 1) {
                    float unitWidth_singleExtracted = (availableTotalWidth / sectionRequiredLength) * unitStandardWidth;
                    ArrayList<DrawingUnit> sectionDrawingUnit_3 = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidth_singleExtracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    drawingUnits.add(sectionDrawingUnit_3);
                }

                accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

            } else {
                //单节宽度不超控件允许宽度、本行总宽也未超总宽。
                // 本节索引已在开头自动加入行索引列表，因而不需在此再次操作。
                //在此，需判断本节是否是最后一节，如果是，则将本行现有所有节进行整体扩展，以占满全行宽度
                if (i == codesInSections.size() - 1) {
                    float unitWidth_extracted = (availableTotalWidth / lineRequiredLength) * unitStandardWidth;

                    //计算其他行的绘制数据
                    float sectionStartX = padding;
                    //先对行内首节进行计算
                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i-sectionAmountInLine+1), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                    //并添加到总记录
                    drawingUnits.add(sectionDrawingUnit);
                    accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

                    //其余各节需要依靠前一节的末尾坐标进行自身坐标的计算【仅当本行内（算上下移的那个）的小节数量≥3时才进入循环】
                    //（否则，如果==2，则是一个下移，剩余一个独占整行在之前的“首节处理”逻辑中已完成计算，不需进循环。）
                    for (int k=2;k<=sectionAmountInLine;k++) {
                        //计算（剩余在本行的）后续各节。【此情景下是包含最后一节即索引为i的节的，与前面若干分支的循环不同】
                        int indexBeingCalculate = i-sectionAmountInLine+k;
                        int indexBeforeCalculate = i-sectionAmountInLine+k-1;
                        //先计算起始X，需要依赖同line中前一节最末音符的右边缘坐标。
                        sectionStartX = drawingUnits.get(indexBeforeCalculate).get(drawingUnits.get(indexBeforeCalculate).size()-1).right+beatGap;
                        ArrayList<DrawingUnit> sectionDrawingUnit_2 = initSectionDrawingUnit(codesInSections.get(indexBeingCalculate), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted,strLyric_1,strLyric_2,accumulateSizeBeforeThisSection);
                        drawingUnits.add(sectionDrawingUnit_2);
                    }
                }//【注意！】此分支下仅当到达尾节时才进行计算（本行绘制数据的计算）【否则在后面的节超屏宽时会再次触发对本节的计算，重复负荷】

                //其余情况下只是增加本行的小节数量记录即可，不必有其他处理。
            }
        }

        //【这里应根据所需总高对各行进行平移】
        //【如果不处理则默认是顶部对齐的绘制方式】
        float totalHeightNeeded = lineCursor*topYDistanceBetweenTwoLine;
        float totalAvailableHeight = sizeChangedHeight-2*padding;
        if(totalHeightNeeded<totalAvailableHeight){
            float shiftAmount = (totalAvailableHeight-totalHeightNeeded)/2;
            for (ArrayList<DrawingUnit> drawingUnitSection: drawingUnits){
                for (DrawingUnit du :drawingUnitSection) {
                    du.shiftEntirely(0, shiftAmount, padding,padding,sizeChangedWidth-padding, sizeChangedHeight-padding);
                }
            }
        }


        if(!isTriggerFromOnSC){
            invalidate();
        }//onSC方法返回后会自动调用onD因而没必要调用invalidate方法。
    }

    //按小节计算（小节内各音符的）绘制数据
    public ArrayList<DrawingUnit> initSectionDrawingUnit(ArrayList<Byte> codesInThisSection, float topDrawing_Y,int lineCursor, float sectionStartX, float unitWidthChanged,String lyric_1,String lyric_2,int accumulateUnitsNumBeforeThisSection) {
        // *注意，sectionStartX要传入“上一小节末尾+节间隔”（非首节时）或者传入padding（是首节时）

        int totalValueBeforeThisCodeInBeat = 0;//用于计算拍子【要在循环的末尾添加，因为要使用的是“本音符之前”的总和】
        ArrayList<DrawingUnit> drawingUnitsInSection = new ArrayList<>();

        for (int j = 0; j < codesInThisSection.size(); j++) {
            byte code = codesInThisSection.get(j);

            if(code>112) {
                //大于112的没有实体绘制单元，而是在其前一单元中设置专用字段
                int curveSpanForward = code-111;//跨越的单元数量（比如，code=112时，指弧线覆盖本身及本身前的1个音符）
                //连音线末端可以在小节首音符上，但是连音线标志必然不能是小节第一个，从而可以获取前一du进行设置
                if(j==0){
                    Toast.makeText(mContext, "该小节内，连音标记前没有音符，错误编码。略过该连音。", Toast.LENGTH_SHORT).show();
                }else {
                    drawingUnitsInSection.get(j - 1).isEndCodeOfLongCurve = true;
                    drawingUnitsInSection.get(j-1).curveLength = curveSpanForward;
                }
            }else {
                DrawingUnit drawingUnit = new DrawingUnit();
                drawingUnit.top = topDrawing_Y + lineCursor * topYDistanceBetweenTwoLine;
                drawingUnit.bottomNoLyric = drawingUnit.top + (unitStandardHeight + additionalHeight * 2 + curveOrLinesHeight * 2);

                if (j == 0) {
                    drawingUnit.left = sectionStartX;//首个音符
                } else {
                    //非首位音符,根据前一音符是否是拍尾，而紧靠或有拍间隔。
                    if (totalValueBeforeThisCodeInBeat == 0) {
                        //前一音符为拍尾
                        //如果不是首音符，则前面必然是有音符的，所以下句可行
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
                        //【注意间隔是不能计算在du之内的，要在外面。因为下划线布满du内的宽度】
                    } else if (codesInThisSection.get(j - 1) > valueOfBeat) {
                        //此时，前一音节是大附点，此时本音节前方有累加时值（按当前的时值计算逻辑），按if判断应当紧靠，但事实上属于一个新拍子
                        // 因而需要有间隔；
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
                    } else {
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right;//紧靠即可

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

                //字符的绘制位置（字符按照给定左下起点方式绘制）
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

    //用于将本音符转换成正确的时值信息然后加总到节拍时值计数器（进一步用于判断是否完成了一个拍子的时值）
    public int addValueToBeatTotalValue(int thisCode, int valueOfBeat, int lastTotal){
        if(thisCode>73 || thisCode==0){
            //时值计算（这一时值是标准时值，加上相当于原值不改变（原先可能剩余非标准时值））
            lastTotal += 0;
        }else if(thisCode>0) {
            //时值计算
            lastTotal+=thisCode;

        }else {//b<0
            //时值计算：空拍带时值，时值绝对值与普通音符相同
            lastTotal-=thisCode;
        }

        //如果加总后超过一个标准节拍时值，则要“进位”处理，只留“余数”
        if(lastTotal>=valueOfBeat){
            lastTotal -= valueOfBeat;
        }
        return lastTotal;
    }


    public float standardLengthOfSection( ArrayList<Byte> codesInSingleSection) {
        //是按标准单位宽度计算的本节所需宽度，在与控件宽度比较之后，(可能)会进行压缩或扩展

        float requiredSectionLength = 0;
        int totalValue = 0;//还是需要计算时值的，因为需要在节拍后面增加节拍间隔。

        for (byte b : codesInSingleSection) {
            //大于112的是特殊记号，不绘制为实体单元，不记录长度
            if(b>73&&b<112){
                //均分多连音
                requiredSectionLength += (unitStandardWidth /2)*(b%10);
                //时值计算
                totalValue += valueOfBeat;
            }else if(b==16||b==8||b==4||b==2) {
                //是不带附点的音符,占据标准宽度
                requiredSectionLength+= unitStandardWidth;
                //时值计算
                totalValue+=b;
            }else if (b==0){
                //延长音，且不是均分多连音；即
                requiredSectionLength+= unitStandardWidth;
                //时值计算，独占节拍的延长音
                totalValue+=valueOfBeat;
            }else if (b==-2||b==-4||b==-8||b==-16){
                //空拍（不带附点时）也占标准宽
                requiredSectionLength+= unitStandardWidth;
                //时值计算：空拍带时值，时值绝对值与普通音符相同
                totalValue-=b;
            }else if(b==24||b==12||b==6||b==3){
                //带附点，标准宽*1.5
                requiredSectionLength += unitStandardWidth*1.5;
                //时值计算
                totalValue+=b;
            }else if(b==-3||b==-6||b==-12||b==-24){
                //带附点，标准宽*1.5
                requiredSectionLength += unitStandardWidth*1.5;
                //时值计算
                totalValue-=b;
            }

            //拍间隔
            if(totalValue%valueOfBeat==0){
                //到达一拍末尾
                //另：大附点（基本音符附加附点）的宽度不再额外加入拍间隔（因为很难计算、逻辑不好处理；而且附点本身有一个间隔，绘制效果应该也还可以）
                requiredSectionLength+=beatGap;
            }
        }
        return requiredSectionLength;
    }

}
