package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class BaseRhythmView extends View {
//* 显示节奏序列
//* 有“折行、单行”两种模式；字符及字宽（暂时）手动设置数值
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。
//*
//* 三种工作模式：“节奏/旋律/有词”
//* 其中有词模式下需要绘制上方连音弧线；旋律模式下需要绘制上下方的加点。
//*

    static final String TAG = "BaseRhythmView";

    Context mContext;

    RhythmBasedCompound bcRhythm; //数据源
    int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
    int valueOfBeat = 16;
    int valueOfSection = 64;

    ArrayList<ArrayList<Byte>> codesInSections;//按小节管理的节奏编码。
    ArrayList<ArrayList<DrawingUnit>> drawingUnits;//按小节管理的绘制数据。

    String lyricInString_1;
    String lyricInString_2;
    ArrayList<Byte> pitchSerial;

    /* 绘制所需*/
    float twoLinesTopYBetween;//仅在多行绘制模式下有意义。

    //可用总长（控件宽扣除两侧缩进）
    float availableTotalWidth;//（在initDrawing方法中初始化）
    //可以指定起绘点（顶部高度，topY）位置
    float topDrawing_Y;//（在initDrawing方法中初始化）

    //在本行一共有多少小节（最后一节已经移到下一行，不算）。（使用这个代替索引列表）
    int sectionAmountInLine = 0;
    //当前本行所有小节所需的长度
    float lineRequiredLength = 0;
    //当前位于第几行，从0起。（用于折行模式下该行Y值的计算）
    int lineCursor = 0;

    int accumulationNumInCodeSerial = 0;//用于确定任意dU在原始编码中所对应的位置，某些功能（如确定连音弧选框位置）需要
    //值传入按节初始的方法中，将所有code全部记录，包括112、127等特别编码。


    /* 用于放缩使用*/
    int codeSizeDip;
    int unitWidthDip;
    int unitHeightDip;

    /* 设置参数*/
    //设置参数与数据源一并设置
    boolean useLyric_1 =false;
    boolean useLyric_2 =false;

    boolean drawPitches = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
//    boolean useMultiLine = true;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）


    /* 画笔组*/
    Paint bottomLinePaint;
    Paint codePaint;//绘制音符
    Paint curveNumPaint;//用于绘制均分多连音弧线中间的小数字，其字符尺寸较小
    Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。

    /* 尺寸组 */
    float padding;
    float unitWidth;//24dp。单个普通音符的基准宽度。【按此标准宽度计算各节需占宽度；如果单节占宽超屏幕宽度，则需压缩单节内音符的占宽；
    // 如果下节因为超长而移到下一行，且本行剩余了更多空间，则需要对各音符占宽予以增加（但是字符大小不变）】
    float unitHeight;//24dp。单个普通音符的基准高度。
    float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），暂定12dp。
    //注意，一个节拍内的音符之间没有额外间隔。
//    但是好像无法手动控制X和点之间的间距，因而本参数仅用于让后续字符自然。//暂定半宽

    float lineGap;//不同行之间的间隔。暂定12dp；如果有文字行则需额外安排文字空间。
    float additionalPointsHeight;//用于上下加点绘制的保留区域，暂定6dp
    float curveOrLinesHeight;//用于绘制上方连音线或下方下划线的空间（上下各一份），暂定8dp
    //下划线绘制为2dp(或1dp)每条

    float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
    float curveNumSize;
    float textBaseLineBottomGap;

    int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    int sizeChangedWidth = 0;//未获取数据前设置为0


    /* 色彩组 */
    int generalColor_Gray;
    int generalColor_LightGray;
    int generalCharGray;


    /* 构造器*/
    public BaseRhythmView(Context context) {
        super(context);
        mContext = context;
//        Log.i(TAG, "RhythmView: mContext="+mContext);
        init(null);
//        this.listener = null;
    }

    public BaseRhythmView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
//        Log.i(TAG, "RhythmView2: mContext="+mContext);
    }


    public BaseRhythmView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
//        Log.i(TAG, "RhythmView3: mContext="+mContext);
        init(attributeset);
//        this.listener = null;
    }

//【当子类调用的父类方法进一步调用了二者都有的同名方法时，测试发现，实际调用的会是子类的】
    void init(AttributeSet attributeset) {
//        Log.i(TAG, "init: BaseInit");
        initSize();
        initColor();
        initPaint();
        initViewOptions();
    }


    void initSize() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        beatGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        lineGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        additionalPointsHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
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
        bottomLinePaint.setStyle(Paint.Style.STROKE);

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
            //需要再次再次初始化绘制信息（但是传入isTriggerFromSC标记，只初始绘制信息不进行刷新）
            initDrawingUnits(true);
        }
        super.onSizeChanged(w, h, old_w, old_h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //合理的尺寸由外部代码及布局文件实现，这里不设计复杂的尺寸交互申请逻辑，而是直接使用结果。
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
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

        //未退出则继续绘制主体内容
        //逐小节逐音符绘制(主体部分：各乐符、下划线、乐符间隔、拍子间隔、小节间隔、节间小节线、均分多连音、跨音符的上方连音弧线)
        drawByEachUnit(canvas);

        //绘制下方歌词
        if(useLyric_1){
            drawLyric_1(canvas);
        }
        if(useLyric_2){
            drawLyric_2(canvas);
        }

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
                        canvas.drawArc(drawingUnit.left, drawingUnit.top + additionalPointsHeight + curveOrLinesHeight / 3,
                                drawingUnit.right, drawingUnit.top + additionalPointsHeight + curveOrLinesHeight, 0, 180, false, bottomLinePaint);//是角度数
                        //【椭圆划过的角度待调整】

                    }

                    //如果到达了小节末尾
                    if (k == sectionDrawingUnits.size() - 1) {
//                        Log.i(TAG, "onDraw: section End.");
                        //则要绘制小节末的竖线
                        canvas.drawLine(drawingUnit.right + beatGap / 2, drawingUnit.top + additionalPointsHeight + 2 * curveOrLinesHeight / 3,
                                drawingUnit.right + beatGap / 2, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight / 3, bottomLinePaint);
                    }
                    //注意，小节线绘制规则：起端没有小节线，小节线只存在于末尾


                    //绘制连音弧线（顶部弧形延长音，不是均分多连）
                    if (drawingUnit.isEndCodeOfLongCurve) {
                        float curveStart = 0;
                        float curveEnd = drawingUnit.right;//覆盖在起端left到末端right（全盖住）

                        //获取弧线起端音符的坐标
                        int span = drawingUnit.curveLength;
                        //（注：k与span的关系）索引为k时：本音符前面有k个音符存在。span则代表本音符之前的span个音符都在弧线下，所以二者一一对应不需加减处理。
                        if (span <= k) {
                            //弧线不跨节
                            curveStart = sectionDrawingUnits.get(k - span).left;
                            canvas.drawArc(curveStart, drawingUnit.top + additionalPointsHeight, curveEnd,
                                    drawingUnit.top + additionalPointsHeight + curveOrLinesHeight,
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
                                    canvas.drawArc(padding, drawingUnit.top + additionalPointsHeight, drawingUnit.right,
                                            drawingUnit.top + additionalPointsHeight + curveOrLinesHeight,
                                            0, 90, false, bottomLinePaint);
                                    //前半段
                                    canvas.drawArc(duCurveStart.left, duCurveStart.top + additionalPointsHeight, sizeChangedWidth - padding,
                                            duCurveStart.top + additionalPointsHeight + curveOrLinesHeight,
                                            90, 90, false, bottomLinePaint);//sweepA据文档似乎是“扫过”幅度

                                } else {
                                    //整体绘制
                                    canvas.drawArc(curveStart, drawingUnit.top + additionalPointsHeight, curveEnd,
                                            drawingUnit.top + additionalPointsHeight + curveOrLinesHeight,
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
    void drawLyric_1(Canvas canvas) {
        //遍历方式进行
        for (ArrayList<DrawingUnit> drawingUnitsInSections : drawingUnits) {
            for (DrawingUnit drawingUnit : drawingUnitsInSections) {
                    canvas.drawText(drawingUnit.lyricWord_1, drawingUnit.lyricWord_1_CenterX, drawingUnit.lyricWord_1_BaseY, codePaint);
            }
        }
    }

    void drawLyric_2(Canvas canvas) {
        //遍历方式进行
        for (ArrayList<DrawingUnit> drawingUnitsInSections : drawingUnits) {
            for (DrawingUnit drawingUnit : drawingUnitsInSections) {
                canvas.drawText(drawingUnit.lyricWord_2, drawingUnit.lyricWord_2_CenterX, drawingUnit.lyricWord_2_BaseY, codePaint);
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


    /* 用在数据设置方法中*/
    void checkAndSetThreeStates(){
        if(lyricInString_1!=null && !lyricInString_1.isEmpty()){
            useLyric_1 = true;
        }
        if(lyricInString_2!=null && !lyricInString_2.isEmpty()){
            useLyric_2 = true;
        }
        if(pitchSerial!=null&&!pitchSerial.isEmpty()){
            drawPitches = true;
        }
    }

    /* 用在数据设置方法中*/
    void setSizeOfCodeAndUnit(int codeSize,int unitWidth,int unitHeight){
        int codeMinSize = 12;
        int codeMaxSize = 28;
        if(codeSize<codeMinSize){
            //不允许小于12【暂定】
            this.codeSizeDip =codeMinSize;
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeMinSize, getResources().getDisplayMetrics());
        }else if(codeSize>codeMaxSize){
            //不允许大于28【暂定】
            this.codeSizeDip =codeMaxSize;
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeMaxSize, getResources().getDisplayMetrics());
        }else {
            this.codeSizeDip =codeSize;
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeSize, getResources().getDisplayMetrics());
        }
        codePaint.setTextSize(textSize);//需要在设置了文字大小后重新设置画笔

        int unitMinSize = codeMinSize+4;//和字符尺寸相差4dp（sp?）。
        int unitMaxSize = codeMaxSize+4;
        if(unitWidth<unitMinSize){
            //不允许小于18【暂定】
            this.unitWidthDip = unitMinSize;
            this.unitWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMinSize, getResources().getDisplayMetrics());
        }else if(unitWidth>unitMaxSize){
            //不允许大于32【暂定】
            this.unitWidthDip = unitMaxSize;
            this.unitWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMaxSize, getResources().getDisplayMetrics());
        }else {
            this.unitWidthDip = unitWidth;
            this.unitWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitWidth, getResources().getDisplayMetrics());
        }

        if(unitHeight<unitMinSize){
            //不允许小于18【暂定】
            this.unitHeightDip = unitMinSize;
            this.unitHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMinSize, getResources().getDisplayMetrics());
        }else if(unitHeight>unitMaxSize){
            //不允许大于32【暂定】
            this.unitHeightDip = unitMaxSize;
            this.unitHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitMaxSize, getResources().getDisplayMetrics());
        }else {
            this.unitHeightDip = unitHeight;
            this.unitHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, unitHeight, getResources().getDisplayMetrics());
        }
    }


    /* 数据设置方法一（简化版）*/
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound){
       setRhythmViewData(rhythmBasedCompound,18,20,20);
    }

    /* 数据设置方法二（提供尺寸设定功能）*/
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound, int codeSize, int unitWidth, int unitHeight){
        this.bcRhythm = rhythmBasedCompound;
        this.rhythmType = rhythmBasedCompound.getRhythmType();
        this.codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmType);

        this.lyricInString_1 = rhythmBasedCompound.getPrimaryLyricSerial();//【在计算时，会改为按节管理版本，才能正确放置位置】
        this.lyricInString_2 = rhythmBasedCompound.getSecondLyricSerial();
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

    void initDrawingUnits(boolean isTriggerFromSC){
        initDrawingUnits_step1();
        initDrawingUnits_step2();

        if(!isTriggerFromSC){
            invalidate();
        }//onSC方法返回后会自动调用onD因而没必要调用invalidate方法。

    }



    /*（现已禁止直接传入编码数组，请传入混合Rhythm类）
    public void setRhythmViewData(ArrayList<ArrayList<Byte>> codesInSections, int rhythmType, String primaryLyricString, String secondLyricString, int codeSize, int unitWidth, int unithHeight){
        this.codesInSections = codesInSections;
        this.rhythmType = rhythmType;
        this.lyricInString_1 = primaryLyricString;
        this.lyricInString_2 = secondLyricString;


        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        //以下是各项数据初始化，以及计算绘制信息的内容
        valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        checkAndSetThreeStates();
        setSizeOfCodeAndUnit(codeSize,unitWidth,unithHeight);
        //如果这个时候还没有获取到尺寸信息则终止操作【即暂不进行绘制信息的计算和初始】
        // 在onSizeChanged()中由于尺寸可能发生了改变，因而必须要重新计算绘制信息，即调用initDrawingUnits；
        if(sizeChangedWidth == 0){
            return;
        }//就运行表现来看，似乎是先设置了数据然后才是onSC。

        //主要计算部分：各音符对应绘制单元的绘制信息计算。
        initDrawingUnits_step1(false);
    }
*/


    /*
    * 目前（已分配的）编码
    * ①0：延音符-（延音符不允许位于序列首部。但是允许位于节首（为了简化，否则要添加连音弧））
    * ②1、2、3、4、6、8、12、16、24。各种时值的实体节奏符X（带下划线的是基本、非附点的音符）
    * (1/4 = 16)
    * ③上述（②条目中）各值的负值：各种时值的空拍子【为简化，空拍不允许有附点。】
    * ④73~79、83~89、103~109，总时值分别为（1/4、1/8、1/16）的均分多连音；
    * (新版中10= 1/4 =16，8 = 1/8 = 8， 7 = 1/16 = 4。便于（x/10 - 6）*4直接得对应时值)
    * 以code%10的余数表示其内含的多连音个数（3~9个）【编辑器暂时只支持3、5、7连音】
    * ⑤112~125：连音弧结束标记，（以code-110代表其跨度，暂时支持2~15跨度）
    * ⑥126：拍尾标记
    * ⑦127：小节尾标记
    * */
    void initDrawingUnits_step1() {
        //子类根据具体规则实现
        //（折行模式要判断各节、各行与屏宽关系；单行模式向后累加）

        //可用总长（控件宽扣除两侧缩进）
        availableTotalWidth = sizeChangedWidth - padding * 2;
        //可以指定起绘点（顶部高度，topY）位置
        topDrawing_Y = padding;

        //装载绘制信息的总列表（按小节区分子列表管理）
        drawingUnits = new ArrayList<>();//初步初始（后面采用add方式，因而不需彻底初始）

        lineRequiredLength =0;//每次重新计算时要重置。
        //两行之间的标准间距，（包含行高在内，是否包含词显示区高度则取决于是否有词）
        //其中*2的原因：上下加点区都要预留，总高为单侧加点*2；上方弧线、下方横线区都要预留。（不论相关内容是否存在）。
        twoLinesTopYBetween =(unitHeight + additionalPointsHeight * 2 + curveOrLinesHeight * 2 + lineGap);
        if(useLyric_2){//使用第二词序则直接预留两行高度，不论主词序是否存在（否则词序初始化方法中不好判断位置，简化处理）
            twoLinesTopYBetween +=(unitHeight *2);
        }else if(useLyric_1){
            twoLinesTopYBetween += unitHeight;
        }//两者皆不使用则高度差不做调整。

        accumulationNumInCodeSerial = 0;//每次初始数据前，将该计数器重置为0
        //在各按小节计算的方法中，对该全局变量直接操作；并将“当前”dU的对应位置（-1，索引位）存入dU。

    }

    //按小节计算（小节内各音符的）绘制数据（未对音序、词序处理（但预留了空间）；应在完整dUs列表生成后再处理词、音序列）
    public ArrayList<DrawingUnit> initSectionDrawingUnit(ArrayList<Byte> codesInThisSection, float topDrawing_Y,int lineCursor, float sectionStartX, float unitWidthChanged) {
        //如果是折行的，根据各节所在位置传入相应startX（行首padding，非行首则是上一节末尾+gap；如果是单行模式则只有首节处于行首，其余向后累加即可。）
        //int totalValueBeforeThisCodeInBeat = 0;//用于计算拍子【要在循环的末尾添加，因为要使用的是“本音符之前”的总和】

        ArrayList<DrawingUnit> drawingUnitsInSection = new ArrayList<>();

        for (int j = 0; j < codesInThisSection.size(); j++) {
            accumulationNumInCodeSerial++;
            byte code = codesInThisSection.get(j);

            if(code>112&&code<126) {
                //大于112的没有实体绘制单元，而是在其前一单元中设置专用字段
                int curveSpanForward = code-111;//跨越的单元数量（比如，code=112时，指弧线覆盖本身及本身前的1个音符）
                //连音线末端可以在小节首音符上，但是连音线标志必然不能是小节第一个，从而可以获取前一du进行设置
                if(j==0){
                    Toast.makeText(mContext, "该小节内，连音标记前没有音符，错误编码。略过该连音。", Toast.LENGTH_SHORT).show();
                }else {
                    drawingUnitsInSection.get(j-1).isEndCodeOfLongCurve = true;
                    drawingUnitsInSection.get(j-1).curveLength = curveSpanForward;
                }
            }else {
                DrawingUnit drawingUnit = new DrawingUnit();
                drawingUnit.top = topDrawing_Y + lineCursor * twoLinesTopYBetween;
                drawingUnit.bottomNoLyric = drawingUnit.top + (unitHeight + additionalPointsHeight * 2 + curveOrLinesHeight * 2);
                drawingUnit.indexInCodeSerial = accumulationNumInCodeSerial;//在没有dU的Code下，该值直接递增；在有对应dU的时候，将值存给dU。

                //判断计算起点位置（startX）
                if (j == 0) {
                    drawingUnit.left = sectionStartX;//首个音符
                } else {
                    //非首位音符,根据前一音符是否是拍尾，而紧靠或有拍间隔。
                    if (codesInThisSection.get(j-1) > 125) {//【新判断方式（126拍尾，127节尾），原来是加总时值判断】
                        //前一音符为拍尾
                        //如果不是首音符，则前面必然是有音符的，所以下句可行
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
                        //注意间隔要计算在du的外面。因为下划线布满du内的宽度。

                    } else {
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right;//紧靠即可

                    }

                    /*（新的计算方案下，大附点后方也有126或127，因而不必再单独处理）
                    else if (codesInThisSection.get(j - 1) > valueOfBeat) {
                        //此时，前一音节是大附点，此时本音节前方有累加时值（按当前的时值计算逻辑），按if判断应当紧靠，但事实上属于一个新拍子
                        // 因而需要有间隔；
                        drawingUnit.left = drawingUnitsInSection.get(j - 1).right + beatGap;//要加入拍间隔
                    }*/

                }

                //单元的右侧在后面分条件（按不同类型的code）确定。独占一拍时，拍间隔也不计入宽度（因为下划线布满dU宽）【附点、均分多连音的宽度有所变化】

                //下划线、音符（本方法只处理X/0,有值音高另行方法处理。）和附点；
                // 或延音线；或均分多连音（由于valueOfBeat不是常量，不能用sw判断。case分支要求使用常量条件）
                //字符（另：均分多连音字符在后面处理）
                String charForCode = "X";
                if (code < 0) {
                    charForCode = "0";
                } else if (code == 0) {
                    charForCode = "-";
                }

                //字符位置（字符按照给定左下起点方式绘制）
                //【为了保证①字符基本位于水平中央、②字符带不带附点时的起始位置基本不变，因而采用：左+三分之一单位宽度折半值 方式，后期据情况调整】
                drawingUnit.codeCenterX = drawingUnit.left + unitWidthChanged / 2;//。
                drawingUnit.codeBaseY = drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight - 8;//暂定减8像素。
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
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                } else if (code == valueOfBeat / 2 + valueOfBeat / 4 || code == -valueOfBeat / 2 - valueOfBeat / 4) {
                    //一线、一附点的画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                    drawingUnit.code = charForCode + "·";
                } else if (code == valueOfBeat / 4 || code == -valueOfBeat / 4) {
                    //两线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10));
                    drawingUnit.code = charForCode;

                } else if (code == valueOfBeat / 4 + valueOfBeat / 8 || code == -valueOfBeat / 4 - valueOfBeat / 8) {
                    //两线、一附点
                    drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10));
                    drawingUnit.code = charForCode + "·";
                } else if (code == valueOfBeat / 8 || code == -valueOfBeat / 8) {
                    //三线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10));
                    drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 20, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 20));
                    drawingUnit.code = charForCode;
                } else if (code == 0) {
                    //独立占拍延音线画法
                    drawingUnit.right = drawingUnit.left + unitWidthChanged;
                    drawingUnit.code = charForCode;
                }

                //均分多连音画法（仅数字部分，顶部圆弧在onDraw中根据弧线结束位置和跨度直接计算）
                //音符跨度处理（跨度数字、跨度数字的绘制位置）
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
                    drawingUnit.mCurveNumBaseY = drawingUnit.top + additionalPointsHeight + curveOrLinesHeight / 3;//【注意，稍后画弧线时，顶部也应留出1/3距离】

                    //下划线处理
                    if (valueOfBeat == 16) {
                        //73~79之间无下划线不处理。
                        if (code > 83 && code < 89) {
                            //一条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                        } else if (code > 93) {//外层if有<99判断
                            //两条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight + 10));
                        }//不支持三条的
                    } else if (valueOfBeat == 8) {//外层if有<99判断
                        if (code > 93) {
                            //一条下划线
                            drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottomNoLyric - additionalPointsHeight - curveOrLinesHeight));
                        }//无下划线的不处理；八分音符下不可能有四分时值的均分多连音，不处理。
                    }
                }

                //在这一层循环的末尾，将本音符的时值累加到本小节的记录上；然后更新“tVBTCIS”记录以备下一音符的使用。
//                totalValueBeforeThisCodeInBeat = addValueToBeatTotalValue(code, valueOfBeat, totalValueBeforeThisCodeInBeat);
                drawingUnitsInSection.add(drawingUnit);//添加本音符对应的绘制信息。
                drawingUnit.checkIsOutOfUi(padding,padding,sizeChangedWidth-padding,sizeChangedHeight-padding);
            }
        }
        return drawingUnitsInSection;//返回本小节对应的绘制信息列表*/

    }

    /* 计算词、音序列的绘制信息（需要在基础dUs列表完成计算后）*/
    void initDrawingUnits_step2(){
        if(useLyric_1){
            initPrimaryLyric();
        }
        if(useLyric_2){
            initSecondLyric();
        }
        if(drawPitches){
            initPitches();
        }
    }

    //用于将本音符转换成正确的时值信息然后加总到节拍时值计数器（进一步用于判断是否完成了一个拍子的时值）
  /*  public int addValueToBeatTotalValue(int thisCode, int valueOfBeat, int lastTotal){
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
    }*/


    public float standardLengthOfSection( ArrayList<Byte> codesInSingleSection) {
        //是按标准单位宽度计算的本节所需宽度，在与控件宽度比较之后，(可能)会进行压缩或扩展

        float requiredSectionLength = 0;
        int totalValue = 0;//还是需要计算时值的，因为需要在节拍后面增加节拍间隔。

        for (byte b : codesInSingleSection) {
            //大于112的是特殊记号，不绘制为实体单元，不记录长度
            if(b>73&&b<112){
                //均分多连音
                requiredSectionLength += (unitWidth /2)*(b%10);
                //时值计算
                totalValue += valueOfBeat;
            }else if(b==16||b==8||b==4||b==2) {
                //是不带附点的音符,占据标准宽度
                requiredSectionLength+= unitWidth;
                //时值计算
                totalValue+=b;
            }else if (b==0){
                //延长音，且不是均分多连音；即
                requiredSectionLength+= unitWidth;
                //时值计算，独占节拍的延长音
                totalValue+=valueOfBeat;
            }else if (b==-2||b==-4||b==-8||b==-16){
                //空拍（不带附点时）也占标准宽
                requiredSectionLength+= unitWidth;
                //时值计算：空拍带时值，时值绝对值与普通音符相同
                totalValue-=b;
            }else if(b==24||b==12||b==6||b==3){
                //带附点，标准宽*1.5
                requiredSectionLength += unitWidth *1.5;
                //时值计算
                totalValue+=b;
            }else if(b==-3||b==-6||b==-12||b==-24){
                //带附点，标准宽*1.5
                requiredSectionLength += unitWidth *1.5;
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

    //在基本dUs信息计算完毕后调用，要根据dUs的已有信息才能处理
    //
    //词序：只对有dU的位置编码：（其中有dU而无字符的情况：）
    //空音符、延音符位置原则不安排字符，%代替；
    //（1个dU多个字符的情况：）
    //均分多连音根据连音数量安排多个字符。
    //*词序在DB、程序中均以String存储、处理，因而不作特别处理，可用%填充。
    //*目前的词序编码解码方式仅适用于“非拼音类”文字（如汉语；日韩等空间上一符基本对应一个音节的语言也可。）

    void initPrimaryLyric(){
        int charAmountAccumulation = 0;
        for(int i=0;i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> currentDuSection = drawingUnits.get(i);
            for (int k=0;k<currentDuSection.size();k++){
                DrawingUnit drawingUnit = currentDuSection.get(k);
                if(drawingUnit.mCurveNumber == 0){
                    //不是均分多连音,安置1个字符即可
                    if(drawingUnit.code.equals("-")||drawingUnit.code.equals("0")){
//                        charAmountAccumulation++;
                        continue;//如果是空拍、延音符则跳过本次。（索引不再同步增加，因为是不再采用填充方案）
                    }
                    drawingUnit.lyricWord_1 = String.valueOf(lyricInString_1.charAt(charAmountAccumulation));
                    drawingUnit.lyricWord_1_BaseY = drawingUnit.bottomNoLyric+unitHeight;
                    drawingUnit.lyricWord_1_CenterX = drawingUnit.codeCenterX;
                    charAmountAccumulation++;
                }else {
                    //均分多连音
                    drawingUnit.lyricWord_1 = lyricInString_1.substring(charAmountAccumulation,charAmountAccumulation+drawingUnit.mCurveNumber);
                    drawingUnit.lyricWord_1_BaseY = drawingUnit.bottomNoLyric+unitHeight;
                    drawingUnit.lyricWord_1_CenterX = drawingUnit.codeCenterX;
                    //【已查API：截取从起坐标本身到终坐标左侧（起坐标字符将算入，终坐标字符不算入，终坐标左侧临字符算入。）】
                    charAmountAccumulation+=drawingUnit.mCurveNumber;
                }
            }
        }
    }

    void initSecondLyric(){
        int charAmountAccumulation = 0;
        for(int i=0;i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> currentDuSection = drawingUnits.get(i);
            for (int k=0;k<currentDuSection.size();k++){
                DrawingUnit drawingUnit = currentDuSection.get(k);
                if(drawingUnit.mCurveNumber == 0){
                    //不是均分多连音,安置1个字符即可
                    if(drawingUnit.code.equals("-")||drawingUnit.code.equals("0")){
//                        charAmountAccumulation++;
                        continue;//如果是空拍、延音符则跳过本次。
                    }
                    drawingUnit.lyricWord_2 = String.valueOf(lyricInString_2.charAt(charAmountAccumulation));
                    drawingUnit.lyricWord_2_BaseY = drawingUnit.bottomNoLyric+unitHeight;
                    drawingUnit.lyricWord_2_CenterX = drawingUnit.codeCenterX;
                    charAmountAccumulation++;
                }else {
                    //均分多连音
                    drawingUnit.lyricWord_2 = lyricInString_2.substring(charAmountAccumulation,charAmountAccumulation+drawingUnit.mCurveNumber);
                    drawingUnit.lyricWord_2_BaseY = drawingUnit.bottomNoLyric+unitHeight;
                    drawingUnit.lyricWord_2_CenterX = drawingUnit.codeCenterX;
                    //【已查API：截取从起坐标本身到终坐标左侧（起坐标字符将算入，终坐标字符不算入，终坐标左侧临字符算入。）】
                    charAmountAccumulation+=drawingUnit.mCurveNumber;
                }
            }
        }
    }


    //只对有dU信息的位置编码
    //延音符、空音符都用0填充；正常位置（包括均分多连音）采用普通音高编码（）
    //音序在程序是byte列表，在DB是String存储。采用byte 0填充。
    void initPitches(){
        int charAmountAccumulation = 0;
        for(int i=0;i<drawingUnits.size();i++){
            ArrayList<DrawingUnit> currentDuSection = drawingUnits.get(i);
            for (int k=0;k<currentDuSection.size();k++){
                DrawingUnit drawingUnit = currentDuSection.get(k);
                if(drawingUnit.code.equals("-")||drawingUnit.code.equals("0")){
//                    charAmountAccumulation++;
                    continue;//如果是空拍、延音符则跳过本次。
                }

                Charset cs = Charset.forName("UTF-8");

                if(drawingUnit.mCurveNumber == 0) {
                    //不是均分多连音
                    ByteBuffer bb = ByteBuffer.allocate(1);
                    bb.put(pitchSerial.get(charAmountAccumulation));
                    bb.flip();
                    CharBuffer cb = cs.decode(bb);
                    drawingUnit.code = cb.toString();

                    charAmountAccumulation++;
                }else {
                    //均分多连音
                    ByteBuffer bb = ByteBuffer.allocate(drawingUnit.mCurveNumber);
                    byte b = pitchSerial.get(charAmountAccumulation);
                    for(int j=0;j<drawingUnit.mCurveNumber;j++){
                        bb.put(b);//添加若干次（但都是取自同一个编码位置）
                    }
                    bb.flip();
                    CharBuffer cb = cs.decode(bb);
                    drawingUnit.code = cb.toString();
                    charAmountAccumulation++;//均分多连音的音高只编码一次，因而仍然是+1（与词序不同）。
                }
            }
        }
    }

    //提供给调用方用于放大显示尺寸
    // 需要重新计算绘制信息
    public void zoomOut(){
        setSizeOfCodeAndUnit(codeSizeDip+=2,unitWidthDip+=2,unitHeightDip+=2);
        initDrawingUnits(false);
    }

    public void zoomIn(){
        setSizeOfCodeAndUnit(codeSizeDip-=2,unitWidthDip-=2,unitHeightDip-=2);
        initDrawingUnits(false);
    }

}
