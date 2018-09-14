package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;


public class RhythmToBitmap extends RhythmView {
    private static final String TAG = "RhythmToBitmap";
    //用于以折行模式计算绘制信息，并直接生成
// 以便绘制到BitMap；

    public static final int SIZE_1080 = 1080;
    public static final int SIZE_720 = 720;


    /* 绘制标题、描述的画笔*/
    Paint titlePaint;
    TextPaint descriptionPaint;
    Paint whitePaint;

    String strTitle = "";
    String strDescription = "";

    StaticLayout myStaticLayout;
    int whiteBackground;

    float totalDrawingHeight = 0;
    /* 构造器*/
    public RhythmToBitmap(Context context) {
        super(context);
        mContext = context;
        init(null);
//        this.listener = null;
    }

    public RhythmToBitmap(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    public RhythmToBitmap(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }

    //改写
    void initViewOptions() {
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    //重写一些尺寸
    void initSize() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        beatGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        lineGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        additionalPointsHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        curveOrLinesHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        textBaseLineBottomGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        curveNumSize =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());

        whiteBackground = ContextCompat.getColor(mContext, R.color.rhythmView_sdMaskWhite);

    }

    @Override
    void initPaint() {
        super.initPaint();

        titlePaint = new Paint();
        titlePaint.setTextSize(36);
        titlePaint.setStrokeWidth(2);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint.setColor(generalCharGray);

        descriptionPaint = new TextPaint();
        descriptionPaint.setTextSize(36);
        descriptionPaint.setStrokeWidth(2);
        descriptionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        descriptionPaint.setColor(generalCharGray);

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(2);
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(whiteBackground);

    }

    //与基类不一致的行为必须覆写
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
//        sizeChangedHeight = h;
//        sizeChangedWidth = 1080;//宽度使用1080，高度（计划是）自由使用
//        Log.i(TAG, "onSizeChanged: ="+sizeChangedWidth+",scH="+sizeChangedHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);//自由使用宽度
        int measureHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        setMeasuredDimension(measureWidth,measureHeight);
    }

    //原有的设置方法关闭，避免其invalidate()调用。
    @Override
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound) {

    }

    @Override
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound, int codeSize, int unitWidth, int unitHeight) {
    }

    //新设置方法
    public void setDataAndParams(RhythmBasedCompound rhythmBasedCompound,int size) {
        this.bcRhythm = rhythmBasedCompound;
        this.rhythmType = rhythmBasedCompound.getRhythmType();
        this.codesInSections = RhythmHelper.codeParseIntoSections(rhythmBasedCompound.getCodeSerialByte(), rhythmType);

        this.primaryPhrases = rhythmBasedCompound.getPrimaryLyricPhrases();//【在计算时，会改为按节管理版本，才能正确放置位置】
        this.secondPhrases = rhythmBasedCompound.getSecondLyricPhrases();
        this.pitchSerial = rhythmBasedCompound.getLinkingPitches();

        this.valueOfBeat = RhythmHelper.calculateValueBeat(rhythmType);

        checkAndSetThreeStates();
        setSizeOfCodeAndUnit(18,20,20);


        //关键是初始就要设置宽度1080
        switch (size){
            case SIZE_720:
                sizeChangedWidth = SIZE_720;
                break;
            case SIZE_1080:
                sizeChangedWidth = SIZE_1080;
                break;
            default:
                sizeChangedWidth = SIZE_1080;
                break;
        }
        // 以及此处改为true（强行关闭其刷新绘制）
        initDrawingUnits(true);
        initStrings(rhythmBasedCompound);

        //重设部分尺寸
        titlePaint.setTextSize(textSize);
        descriptionPaint.setTextSize(textSize);
    }

    public void initStrings(RhythmBasedCompound rhythmBasedCompound){
        strTitle = rhythmBasedCompound.getTitle()==null?"":rhythmBasedCompound.getTitle();
        strDescription = rhythmBasedCompound.getDescription()==null?"":rhythmBasedCompound.getDescription();

        if(!strDescription.isEmpty()){
//            Log.i(TAG, "initStrings: (sizeChangedWidth-2*padding)="+(sizeChangedWidth-2*padding));
            myStaticLayout = new StaticLayout(strDescription,descriptionPaint,900,
                    Layout.Alignment.ALIGN_NORMAL,1f,0f,true);
        }

    }


    //有额外多绘制的内容
    //后面的draw方法会产生对本方法的调用，在此控制实际绘制内容
    @Override
    protected void onDraw(Canvas canvas) {
        //提供给外部显示时需要先绘制背景，否则背景是黑的。
        canvas.drawRect(0,0,1080, totalDrawingHeight,whitePaint);
        //绘制上方标题
        canvas.drawText(strTitle,padding,padding+2*unitHeight,titlePaint);

//        canvas.save();

        canvas.translate(0,unitHeight*2.5f);//向下移动，绘制节奏数据
        //补充背景
        canvas.drawRect(0, totalDrawingHeight-unitHeight*2.5f,1080, totalDrawingHeight,whitePaint);


        //绘制节奏区域
        super.onDraw(canvas);

        //继续移动画布，在下方绘制描述文本
        canvas.translate(0,padding+rhythmPartHeight);
        //补充背景
        canvas.drawRect(0, totalDrawingHeight-(padding-rhythmPartHeight),1080, totalDrawingHeight,whitePaint);

        myStaticLayout.draw(canvas);

//        canvas.restore();
//        canvas.drawLine(sizeChangedWidth-30,0,sizeChangedWidth-10,shiftAmount,bottomLinePaint);

    }

    //不同于基类，所有du都要绘制。
    void drawByEachUnit(Canvas canvas){
        for (int i = 0;i<drawingUnits.size();i++) {
            ArrayList<DrawingUnit> sectionDrawingUnits = drawingUnits.get(i);

            for (int k=0;k<sectionDrawingUnits.size();k++) {
                DrawingUnit drawingUnit = sectionDrawingUnits.get(k);
//                if (!drawingUnit.isOutOfUi) {//本字符在可显示区域

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
                            curveStart = sectionDrawingUnits.get(k - span+1).left;
                            canvas.drawArc(curveStart, drawingUnit.top + additionalPointsHeight, curveEnd,
                                    drawingUnit.top + additionalPointsHeight + curveOrLinesHeight,
                                    0, -180, false, bottomLinePaint);//绘制
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
                                    curveStart = duCurveStart.left;
                                    canvas.drawArc(curveStart, drawingUnit.top + additionalPointsHeight, curveEnd,
                                            drawingUnit.top + additionalPointsHeight + curveOrLinesHeight,
                                            0, 179, false, bottomLinePaint);

                                }
                            }
                        }
                    }
//                }
            }
        }
    }





    public void callWhenReMeasureIsNeeded(){
        //do nothing,纯为绕过基类。
    }


    public Bitmap makeBitmap() {
        /*if(drawingUnits==null||drawingUnits.isEmpty()){
            Toast.makeText(mContext, "无数据，终止绘制。", Toast.LENGTH_SHORT).show();
            return null;
        } 【无数据绘制背景图，没错】*/

        //这里绘制成一个bitmap
        float descriptionHeight = myStaticLayout.getHeight();

        totalDrawingHeight = 2*padding+unitHeight*2.5f+rhythmPartHeight+descriptionHeight;

        Bitmap bitmap = Bitmap.createBitmap(sizeChangedWidth, (int) totalDrawingHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);

        return bitmap;


    }
}
