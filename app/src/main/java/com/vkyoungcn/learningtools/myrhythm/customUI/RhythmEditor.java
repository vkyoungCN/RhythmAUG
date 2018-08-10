package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;

public class RhythmEditor extends View {
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
//* 在所有小节之后标示一个+号。
//* 单行模式，绘制中的小节位于屏幕中。

    private static final String TAG = "RhythmEditor";

    private Context mContext;

    private ArrayList<Byte> rhythmCodes; //数据源，节奏序列的编码。根据该数据生成各字符单元上的绘制信息。
    private int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
//    private int unitAmount;
    private int valueOfBeat = 16;
    private int valueOfSection = 64;

    private ArrayList<ArrayList<Byte>> codesInSections;//对数据源进行分析处理之后，按小节归纳起来。便于进行按节分行的判断。
    private ArrayList<ArrayList<DrawingUnit>> drawingUnits;//绘制数据也需要按小节组织，以便与按小节组织的代码一并处理。

    /* 设置参数*/
    //设置参数与数据源一并设置
    private boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
    private boolean useMultiLine = false;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）


    //    private boolean isDataInitBeInterruptedBecauseOfNoSize = false;

    /* 画笔组*/
    private Paint bottomLinePaint;
    private Paint codePaint;
    private Paint curveNumPaint;//用于绘制均分多连音弧线中间的小数字，其字符尺寸较小
    private Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。
    private Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
//    private Paint textWaitingPaint;
    private Paint maskPaint;
    private Paint slidingBallPaint;
    private Paint slidingVerticalBarPaint;



    /* 尺寸组 */
    private float padding;
    private float unitStandardWidth;//24dp。单个普通音符的基准宽度。【按此标准宽度计算各节需占宽度；如果单节占宽超屏幕宽度，则需压缩单节内音符的占宽；
    // 如果下节因为超长而移到下一行，且本行剩余了更多空间，则需要对各音符占宽予以增加（但是字符大小不变）】
    private float unitStandardHeight;//24dp。单个普通音符的基准高度。

    private float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），暂定12dp。
    //注意，一个节拍内的音符之间没有额外间隔。

//    private float lineGap;//不同行之间的间隔。暂定12dp；如果有文字行则需额外安排文字空间。
    private float additionalHeight;//用于上下加点绘制的保留区域，暂定6dp
    private float curveOrLinesHeight;//用于绘制上方连音线或下方下划线的空间（上下各一份），暂定8dp

    private float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
    private float curveNumSize;

    private int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    private int sizeChangedWidth = 0;//未获取数据前设置为0

    private float slidingVerticalBarShort;
    private float slidingVerticalBarMedium;
    private float slidingVerticalBarLong;
    private float slidingVerticalBarGap;
    private float slidingBallDiameter;


    /* 色彩组 */
    private int generalColor_Gray;
    private int editBox_blue;
    private int slidingMask_white;
    private int slidingBall_pink;
    private int slidingVerticalBar_black;


    /* 滑动交互所需变量*/
    private boolean isSlidingModeOn =false;
    private float totalRequiredLength = 0;
    private boolean noNeedToSliding = false;

    private ArrayList<VerticalBar> verticalBars;//滑动时的刻度
    private RectF clickingBallRectF;

    //用于描述各字符对应绘制信息的一个内部类
    public class DrawingUnit {
        //在横向上，各字符基本是等宽的；
        // 但是当位于节拍或小节末尾时，右侧会附加上额外的空间
        // 这些额外的空间会影响后续字符的横向位置，因而必须记录到所有受影响的字符中；
        //（另外，小节之间的小节线的绘制信息不记录在DU中，而是由onD方法现场计算绘制。但位于小节末尾
        // 的DU中会持有一个节末标记变量）
        //如果是小节内最后一个音符，需要记录一下，以便在遍历绘制时在后面绘制一条竖线（小节线）
        //拍子之间、小节之间有额外间隔，由设置方法计算出位置后直接存储给相应字段，本类不需持有相应位置信息。
        //最前端的一条小节线由绘制方法默认绘制，不需记录。
//        private byte additionalSpotType = 0;//上下加点类型，默认0（无加点）；下加负值、上加正值。原则上不超正负3。
//        private byte bottomLineAmount = 0;//并不是所有音符都有下划线。

        /* 作为一个绘制单位，其整体的左端起始位置
         * 用于使后续单位建立自己的位置*/
        float left;
        float right;
        float top;
        float bottom;

        private String code = "X";//默认是X，当作为旋律绘制时绘制具体音高的数值。
        private float codeStartX;//用于字符绘制（字符底边中点）
        private float codeBaseY;//字符底边【待？基线还是底边？】

        private ArrayList<BottomLine> bottomLines = new ArrayList<>();//已实例化，直接add即可。
        private RectF[] additionalPoints = new RectF[]{};//上下加点

        private int curveNumber = 0;//在均分多连音情况下，顶弧中间有一个小数字；
        private float cNumCenterX;
        private float cNumBaseY;

        private boolean isLastCodeInSection = false;


        //一个字符的空间方案暂定如下：标志尺寸ss,字符区域占宽=ss（字体尺寸本身不足的留空即可），
        // 字符占高=展宽；字符上方预留半ss的顶弧线高度，其中保留一小层的高度作为上加点区域；
        //字符下方半ss空间是下划线区域，下划线下方保留一小层高度作为下加点区域。（小层高度待定，暂定5~8dp）
        //非首尾拍字符之间是没有间隔的，以便令下划线相接。

        //连音线的绘制，将由RhV直接提供方法。程序根据词序缺少位置，指定Rhv在哪些（起止）位置上绘制连音线


        public DrawingUnit() {
        }

        public DrawingUnit(String code, boolean isLastCodeInSection, float codeStartX, float codeBaseY, ArrayList<BottomLine> bottomLines, RectF[] additionalPoints, float left, float right, float top, float bottom) {
            this.code = code;
            this.isLastCodeInSection = isLastCodeInSection;
            this.codeStartX = codeStartX;
            this.codeBaseY = codeBaseY;
            this.bottomLines = bottomLines;
            this.additionalPoints = additionalPoints;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }


        /*public void setBottomLineAmount(byte bottomLineAmount) {
            if(bottomLineAmount>3){
                Toast.makeText(mContext, "音符下划线过多？请检查谱子是否正确。", Toast.LENGTH_SHORT).show();
                return;
            }else if (bottomLineAmount<0){
                Toast.makeText(mContext, "音符下划线数值设置错误。", Toast.LENGTH_SHORT).show();

            }
            this.bottomLineAmount = bottomLineAmount;
        }*/

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            //从编码到code需要转换（即使是音高，也因上下加点而多种不同）
            this.code = code;
        }


/*
        public void setAdditionalSpotType(byte additionalSpotType) {
            if(additionalSpotType>4||additionalSpotType<-4){
                Toast.makeText(mContext, "上下加点异常，请检查输入是否错误。", Toast.LENGTH_SHORT).show();
            }else {
                this.additionalSpotType = additionalSpotType;
                //在不超正负4，但超正负2时，可以设置，但要给出提示。
                if(additionalSpotType>2){
                    Toast.makeText(mContext, "上加点超过2，可能超出可演唱音域。", Toast.LENGTH_SHORT).show();
                }else if(additionalSpotType<-2){
                    Toast.makeText(mContext, "下加点超过2，可能超出可演唱音域。", Toast.LENGTH_SHORT).show();
                }
            }
        }
*/

        public float getCodeStartX() {
            return codeStartX;
        }

        public void setCodeStartX(float codeStartX) {
            this.codeStartX = codeStartX;
        }

        public float getCodeBaseY() {
            return codeBaseY;
        }

        public void setCodeBaseY(float codeBaseY) {
            this.codeBaseY = codeBaseY;
        }


        public boolean isLastCodeInSection() {
            return isLastCodeInSection;
        }

        public void setLastCodeInSection(boolean lastCodeInSection) {
            isLastCodeInSection = lastCodeInSection;
        }

        public ArrayList<BottomLine> getBottomLines() {
            return bottomLines;
        }

        public void setBottomLines(ArrayList<BottomLine> bottomLines) {
            this.bottomLines = bottomLines;
        }

        public RectF[] getAdditionalPoints() {
            return additionalPoints;
        }

        public void setAdditionalPoints(RectF[] additionalPoints) {
            this.additionalPoints = additionalPoints;
        }
    }

    //用于描述各音符下划线绘制信息的类，用在DrawingUnit中
    private class BottomLine {
        float startX;
        float startY;
        float toX;
        float toY;

        public BottomLine(float startX, float startY, float toX, float toY) {
            this.startX = startX;
            this.startY = startY;
            this.toX = toX;
            this.toY = toY;
        }
    }


    //用于进入滑动模式时的刻度短线绘制
    private class VerticalBar{
        float x;
        float top;
        float bottom;

        public VerticalBar(float x, float top, float bottom) {
            this.x = x;
            this.top = top;
            this.bottom = bottom;
        }
    }


    public RhythmEditor(Context context) {
        super(context);
        mContext = context;
        init(null);
//        this.listener = null;
    }

    public RhythmEditor(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    public RhythmEditor(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    private void init(AttributeSet attributeset) {
        initSizeAndColor();
        initPaint();
        initViewOptions();
    }


    private void initSizeAndColor() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        unitStandardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        beatGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
//        lineGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        additionalHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        curveOrLinesHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        curveNumSize =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());

        generalColor_Gray = ContextCompat.getColor(mContext, R.color.rhythmView_generalGray);
        editBox_blue = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxBlue);

        //与滑动有关的
        slidingVerticalBarShort = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        slidingVerticalBarMedium = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        slidingVerticalBarLong = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        slidingVerticalBarGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        slidingBallDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, getResources().getDisplayMetrics());

        slidingMask_white = ContextCompat.getColor(mContext, R.color.rhythmView_sdMaskWhite);
        slidingBall_pink = ContextCompat.getColor(mContext, R.color.rhythmView_sdBallPink);
        slidingVerticalBar_black =  ContextCompat.getColor(mContext, R.color.rhythmView_sdVBarBlack);
    }

    private void initPaint() {
        bottomLinePaint = new Paint();
        bottomLinePaint.setColor(generalColor_Gray);
        bottomLinePaint.setStrokeWidth(2);//
        bottomLinePaint.setStyle(Paint.Style.STROKE);


        codePaint = new Paint();
        codePaint.setTextSize(textSize);
//        codePaint.setStrokeWidth(4);
        codePaint.setColor(generalColor_Gray);
        codePaint.setAntiAlias(true);
//        codePaint.setTextAlign(Paint.Align.CENTER);【改为指定起始点方式】

        curveNumPaint = new Paint();
        curveNumPaint.setTextSize(curveNumSize);
        curveNumPaint.setStrokeWidth(2);
        curveNumPaint.setColor(generalColor_Gray);
        curveNumPaint.setAntiAlias(true);
        curveNumPaint.setTextAlign(Paint.Align.CENTER);


        grayEmptyPaint = new Paint();
        grayEmptyPaint.setStyle(Paint.Style.FILL);
        grayEmptyPaint.setStrokeWidth(4);
        grayEmptyPaint.setAntiAlias(true);
        grayEmptyPaint.setColor(generalColor_Gray);

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

    }


    private void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    //【据说是系统计算好控件的实际尺寸后以本方法通知用户】
    // 【调用顺序：M(多次)-S(单次)-D】。
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        sizeChangedHeight = h;
        sizeChangedWidth = w;

        if(!rhythmCodes.isEmpty()){
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
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

    }


    @Override
    protected void onDraw(Canvas canvas) {
//        Log.i(TAG, "onDraw: characters="+characters.toString());
        if(rhythmCodes.isEmpty()) {
            //【待修改】在编辑、新增模式下，数据源为空时应当绘制一个标准空小节
            //此时节奏数据还未设置，只在中间高度绘制一条背景
            float fromX = padding;
            float fromY = sizeChangedHeight/2-20;
            float toX = sizeChangedWidth - padding;
            float toY = sizeChangedHeight/2+20;
            //条带总宽40像素。

            canvas.drawRect(fromX,fromY,toX,toY, grayEmptyPaint);
            return;
        }
        //注意，小节线绘制规则：起端没有小节线，小节线只存在于末尾


        //【注意】即使是进入了滑动模式，下层的内容仍然要绘制，显示。
        //逐小节逐音符绘制
        for (ArrayList<DrawingUnit> sectionDrawingUnits :drawingUnits) {
            int unitCursor = 0;//用于判断是否到达小节末尾。
            for (DrawingUnit drawingUnit :sectionDrawingUnits) {

                //字符
                canvas.drawText(drawingUnit.getCode(),drawingUnit.getCodeStartX(),drawingUnit.getCodeBaseY(), codePaint);

                //下划线
                for (BottomLine bottomLine :drawingUnit.bottomLines) {
                    canvas.drawLine(bottomLine.startX,bottomLine.startY,bottomLine.toX,bottomLine.toY,bottomLinePaint);
                }

                //绘制上下加点（旋律模式下）（*如果没有加点则不会进入）
                for (RectF point : drawingUnit.additionalPoints){
                    canvas.drawOval(point,bottomLinePaint);
                }

                //均分多连音的弧线和弧线内数字
                if(drawingUnit.curveNumber!=0){
                    //弧中数字
                    canvas.drawText(String.valueOf(drawingUnit.curveNumber),drawingUnit.cNumCenterX,drawingUnit.cNumBaseY, curveNumPaint);

                    //弧线
                    canvas.drawArc(drawingUnit.left,drawingUnit.top+additionalHeight+curveOrLinesHeight/3,
                            drawingUnit.right,drawingUnit.top+additionalHeight+curveOrLinesHeight,0,180,false,bottomLinePaint);//是角度数
                    //【椭圆划过的角度待调整】

                }

                //如果到达了小节末尾
                if(unitCursor == sectionDrawingUnits.size()-1){
                    //则要绘制小节末的竖线
                    canvas.drawLine(drawingUnit.right+beatGap/2,drawingUnit.top+additionalHeight+2*curveOrLinesHeight/3,
                            drawingUnit.right+beatGap/2,drawingUnit.bottom-additionalHeight-curveOrLinesHeight/3,bottomLinePaint);
                }

                //在有词模式下绘制上方跨音符的连音线【待】
                //在旋律模式下，可能需要绘制上下加点。【待】
            }
        }

        if(isSlidingModeOn){
            //滑动模式下，额外复制一层遮罩；以及遮罩上方的小球、刻度
            //【当滑动开始后（滑动了一定程度），设置新的滑动中绘制参数即可；（而且下方各小节的绘制位置信息也产生了改变）】
            canvas.drawRect(0,0,sizeChangedWidth,sizeChangedHeight,maskPaint);
            canvas.drawArc(clickingBallRectF,0,360,true,slidingBallPaint);
            for (VerticalBar vb: verticalBars) {
                canvas.drawLine(vb.x, vb.top, vb.x, vb.bottom, slidingVerticalBarPaint);
            }
        }


//            invalidate();
    }






    /*
     * 方法由程序调用，动态设置目标字串
     * 设置节拍类型（4/4等）
     * 设置字符大小
     * */
    public void setRhythm(Rhythm rcs, int codeSize, int unitWidth){
        this.rhythmCodes = rcs.getRhythmCodeSerial();
        this.rhythmType = rcs.getRhythmType();

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

        int unitMinSize = 16;
        int unitMaxSize = 32;
        if(unitWidth<unitMinSize){
            //不允许小于18【暂定】
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, unitMinSize, getResources().getDisplayMetrics());
        }else if(unitWidth>unitMaxSize){
            //不允许大于36【暂定】
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, unitMaxSize, getResources().getDisplayMetrics());
        }else {
            this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, codeSize, getResources().getDisplayMetrics());
        }

        /*switch (codeSize){
            case CODE_SMALL:
                this.textSize = textSizeSmall;
                break;
            case CODE_MEDIUM:
                this.textSize = textSizeMedium;
                break;
            case CODE_LARGE:
                this.textSize = textSizeLarge;
                break;
        }*/

        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        initData();
    }


    //onSizeC方法中会调用initData，该时点可能尚未设置必要的数据，所以需要判断。??
    //事实上，onS和setRC谁先谁后可能没有定数。两种错误都遇到过。??
    private void initData() {
        //根据节拍形式确定一拍的时值、一节的时值总量。
        switch (rhythmType){
            case RHYTHM_TYPE_24:
                valueOfSection = 32;
                //此时beat值==16无需修改
                break;
            case RHYTHM_TYPE_34:
                valueOfSection = 48;
                break;
            case RHYTHM_TYPE_44:
                valueOfSection = 64;
                break;
            case RHYTHM_TYPE_38:
                valueOfSection = 24;
                valueOfBeat = 8;
                break;
            case RHYTHM_TYPE_68:
                valueOfSection = 48;
                valueOfBeat = 8;
                break;
        }
        //先将所有音符的序列按小节拆组成若干个列表的集合列表
        codeParseIntoSections();

        //根据初始化了的数据源初始化绘制信息数组
        //本方法在设置数据源的方法内调用，因而必然非空不需判断

        //如果这个时候还没有获取到尺寸信息则终止操作【即暂不进行绘制信息的初始】
        // 在onSizeChanged()中由于尺寸可能发生了改变，因而需要重新计算绘制信息，因而onS中必须调用initDrawingUnits；
        // 发现终止记录时将再次恢复（直接进入下一级方法）。
        if(sizeChangedWidth == 0){
//            isDataInitBeInterruptedBecauseOfNoSize = true;
            return;
        }

        initDrawingUnits(false);

    }


    private void codeParseIntoSections(){
        //将节奏编码序列按小节组织起来

        int totalValue=0;
        codesInSections = new ArrayList<>();//初步初始化
        int startIndex = 0;//用于记录上次添加的末尾索引+1，即本节应添加的音符序列的索引起始值。

        for (int i=0; i<rhythmCodes.size();i++){
            byte b = rhythmCodes.get(i);
            if(b>77 || b==0){
                //时值计算
                totalValue += valueOfBeat;
            }else if(b>0) {
                //时值计算
                totalValue+=b;
            }else {//b<0
                //时值计算：空拍带时值，时值绝对值与普通音符相同
                totalValue-=b;
            }
            if(totalValue!=0 && totalValue%valueOfSection==0){
                ArrayList<Byte> codeInSingleSection = new ArrayList<>(rhythmCodes.subList(startIndex,i));//装载单节内的音符
                codesInSections.add(codeInSingleSection);//添加到按节管理的总编码表
                startIndex = i+1;
            }//这样只有满节的小节（包括最后是0、-等特殊情况）才能处理，最后如果出现不满暂定属于编码错误的情形。
        }
    }

    private void initDrawingUnits(boolean isTriggerFromOnSC) {
        totalRequiredLength = 0;//每次重新计算绘制信息前要清空。

        //本方法计算了大部分所需的绘制坐标，但是还有如下未处理：
        // 未处理：①如果是旋律：上下加点画法；②如果是带词旋律，则加入弧线画法；
        // (注意，均分多连音的顶部弧线直接按drawingU的宽度绘制即可，且均分多连音弧线中的数字坐标已经进行了计算)
        //【其他方法的调整；然后是自定义音符与节奏输入法；】

        // 新增编辑采用单行滑动模式，这样就暂时不必处理复杂的编辑中小节换行+扩展、压缩逻辑。

        //【注意】如果是由onSc中触发，则最后不调用invalid。

        //可用总长（控件宽扣除两侧缩进）
        float availableTotalWidth = sizeChangedWidth - padding * 2;

        //装载绘制信息的总列表（按小节区分子列表管理）
        drawingUnits = new ArrayList<ArrayList<DrawingUnit>>();//初步初始（后面采用add方式，因而不需彻底初始）

//        int sectionAmount = 0;//在本行一共有多少小节（最后一节已经移到下一行，不算）。（使用这个代替索引列表）
        int lineCursor = 0;//当前位于第几行，从0起。（用于该行Y值的计算）
        float bottomDrawing_Y = sizeChangedHeight/2;

        //开始执行绘制。以小节为单位进行计算。
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));

            //如果不换行，则不需复杂的计算逻辑，直接向后扩展即可
            if(i == 0){
                //第一小节
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit_singleLineMode(codesInSections.get(i), bottomDrawing_Y, 0, padding, unitStandardWidth);
                drawingUnits.add(sectionDrawingUnit);
            }else {
                float startX = drawingUnits.get(i-1).get(drawingUnits.get(i-1).size()-1).right+beatGap;
                ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit_singleLineMode(codesInSections.get(i), bottomDrawing_Y, 0, startX, unitStandardWidth);

            }

            //记录到所需总长度
            totalRequiredLength += sectionRequiredLength;//【仍然需要，最终滑动时需要】
        }

        if(!isTriggerFromOnSC){
            invalidate();
        }//onSC方法返回后会自动调用onD因而没必要调用invalidate方法。
    }

    //按小节计算（小节内各音符的）绘制数据
    private ArrayList<DrawingUnit> initSectionDrawingUnit_singleLineMode(ArrayList<Byte> codesInThisSection, float bottomDrawing_Y, int lineCursor, float sectionStartX, float unitWidthChanged) {
        // *注意，sectionStartX要传入“上一小节末尾+节间隔”（非首节时）或者传入padding（是首节时）

        int totalValueBeforeThisCodeInsideSection = 0;//用于计算拍子【要在循环的末尾添加，因为要使用的是“本音符之前”的总和】
        ArrayList<DrawingUnit> drawingUnitsInSection = new ArrayList<>();

        for (int j = 0; j < codesInThisSection.size(); j++) {
            byte code = codesInThisSection.get(j);

            DrawingUnit drawingUnit = new DrawingUnit();
            //绘制在同一行内
            drawingUnit.bottom = bottomDrawing_Y;
            drawingUnit.top = drawingUnit.bottom- (unitStandardHeight + additionalHeight * 2 + curveOrLinesHeight * 2);


            if (j == 0) {
                drawingUnit.left = sectionStartX;//首个音符
            } else {
                //非首位音符,根据前一音符是否是拍尾，而紧靠或有拍间隔。
                if (totalValueBeforeThisCodeInsideSection % valueOfBeat == 0) {
                    //前一音符为拍尾
                    //如果不是首音符，则前面必然是有音符的，所以下句可行
                    drawingUnit.left = drawingUnitsInSection.get(j-1).right + beatGap;//要加入拍间隔
                    //【注意间隔是不能计算在du之内的，要在外面。因为下划线布满du内的宽度】
                } else {
                    drawingUnit.left = drawingUnitsInSection.get(j-1).right;//紧靠即可
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
            drawingUnit.codeStartX = drawingUnit.left + unitWidthChanged / 3;//。
            drawingUnit.codeBaseY = drawingUnit.bottom - additionalHeight - curveOrLinesHeight - 8;//暂定减8像素。
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
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
            } else if (code == valueOfBeat / 2 + valueOfBeat / 4 || code == -valueOfBeat / 2 - valueOfBeat / 4) {
                //一线、一附点的画法
                drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                drawingUnit.code = charForCode + "·";
            } else if (code == valueOfBeat / 4 || code == -valueOfBeat / 4) {
                //两线画法
                drawingUnit.right = drawingUnit.left + unitWidthChanged;
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10));
                drawingUnit.code = charForCode;

            } else if (code == valueOfBeat / 4 + valueOfBeat / 8 || code == -valueOfBeat / 4 - valueOfBeat / 8) {
                //两线、一附点
                drawingUnit.right = drawingUnit.left + unitWidthChanged * 1.5f;
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10));
                drawingUnit.code = charForCode + "·";
            } else if (code == valueOfBeat / 8 || code == -valueOfBeat / 8) {
                //三线画法
                drawingUnit.right = drawingUnit.left + unitWidthChanged;
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10));
                drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 20, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 20));
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
                drawingUnit.curveNumber = codeNum;
                drawingUnit.cNumCenterX = (drawingUnit.right - drawingUnit.left) / 2 + drawingUnit.left;
                drawingUnit.cNumBaseY = drawingUnit.top + additionalHeight + curveOrLinesHeight / 3;//【注意，稍后画弧线时，顶部也应留出1/3距离】

                //下划线处理
                if (valueOfBeat == 16) {
                    //73~79之间无下划线不处理。
                    if (code > 83 && code < 89) {
                        //一条下划线
                        drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                    } else if (code > 93) {//外层if有<99判断
                        //两条下划线
                        drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                        drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight + 10));
                    }//不支持三条的
                } else if (valueOfBeat == 8) {//外层if有<99判断
                    if (code > 93) {
                        //一条下划线
                        drawingUnit.bottomLines.add(new BottomLine(drawingUnit.left, drawingUnit.bottom - additionalHeight - curveOrLinesHeight, drawingUnit.right, drawingUnit.bottom - additionalHeight - curveOrLinesHeight));
                    }//无下划线的不处理；八分音符下不可能有四分时值的均分多连音，不处理。
                }
            }

            //在这一层循环的末尾，将本音符的时值累加到本小节的记录上；然后更新“tVBTCIS”记录以备下一音符的使用。
            totalValueBeforeThisCodeInsideSection = addValueToSectionValue(code, valueOfBeat, totalValueBeforeThisCodeInsideSection);
            drawingUnitsInSection.add(drawingUnit);//添加本音符对应的绘制信息。
        }

        return drawingUnitsInSection;//返回本小节对应的绘制信息列表
    }

    //用于将本音符转换成正确的时值信息然后加总到（小节的）时值计数器（进一步用于判断是否完成了一个拍子的时值）
    private int addValueToSectionValue(int thisCode,int valueOfBeat,int lastTotal){
        if(thisCode>73 || thisCode==0){
            //时值计算
            lastTotal += valueOfBeat;
        }else if(thisCode>0) {
            //时值计算
            lastTotal+=thisCode;
        }else {//b<0
            //时值计算：空拍带时值，时值绝对值与普通音符相同
            lastTotal-=thisCode;
        }
        return lastTotal;
    }


    private float standardLengthOfSection( ArrayList<Byte> codesInSingleSection) {
        //是按标准单位宽度计算的本节所需宽度，在与控件宽度比较之后，(可能)会进行压缩或扩展

        float requiredSectionLength = 0;
        int totalValue = 0;//还是需要计算时值的，因为需要在节拍后面增加节拍间隔。

        for (byte b : codesInSingleSection) {
            if(b>73){
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

    //与调用方的交互
    //在刚刚按下时调用本方法，绘制出①半透明白色遮罩；②按压位置处的粉红小圆；③上方刻度；（下方节奏照样绘制）
    public void slidingStart(float x, float y){
        if(totalRequiredLength < (sizeChangedWidth-2*padding)){
            //没有滑动的必要，不再执行后续动作
            noNeedToSliding = true;
        }else {
            noNeedToSliding = false;
            //要绘制出相应的状态
            //在此计算各数据

            clickingBallRectF = new RectF(x-slidingBallDiameter/2,y-slidingBallDiameter/2,x+slidingBallDiameter/2,y+slidingBallDiameter/2);

            verticalBars = new ArrayList<>(19);
            //计划绘制①中央长线*1；②两侧中长线*2；③两侧短线共四组*16
            //从左向右添加
            float middleX = sizeChangedWidth/2;
            verticalBars.set(9,new VerticalBar(middleX,padding,padding+slidingVerticalBarLong));
            for(int i=0;i<19;i++){
                if(i!=9) {
                    if(i==4||i==14) {
                        //中长线
                        new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding+slidingVerticalBarMedium);
                    } else {
                        //短线
                        new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding+slidingVerticalBarShort);
                    }
                }//==9的中间竖线已计算
            }
        }
    }


    //滑动到达一定程度后，更新绘制（相当于滑动到了一个新刻度）
    public void slidingChange(boolean toLeft){
        //暂定只传递向左or向右滑动【每次滑动一个标准drawingUnit宽度】（暂不设计得太精确，毕竟自己用且急用）

        if(noNeedToSliding) {
            return;
        }

        //滑动后，更新：①底层的节奏数据各X坐标；②上方刻度（每touch一次重置一回中心线，否则（如多次调用本方法）只是改变其余各线的状态，模拟滑动刻度）

        【到此，下方暂未修改】
            //如果还是不需滑动则直接无反应
            //设置新的绘制参数
            clickingBallRectF = new RectF(x-slidingBallDiameter/2,y-slidingBallDiameter/2,x+slidingBallDiameter/2,y+slidingBallDiameter/2);

            verticalBars = new ArrayList<>(19);
            //计划绘制①中央长线*1；②两侧中长线*2；③两侧短线共四组*16
            //从左向右添加
            float middleX = sizeChangedWidth/2;
            verticalBars.set(9,new VerticalBar(middleX,padding,padding+slidingVerticalBarLong));
            for(int i=0;i<19;i++){
                if(i!=9) {
                    if(i==4||i==14) {
                        //中长线
                        new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding+slidingVerticalBarMedium);
                    } else {
                        //短线
                        new VerticalBar(middleX + (i - 9) * slidingVerticalBarGap, padding, padding+slidingVerticalBarShort);
                    }
                }//==9的中间竖线已计算
            }
    }



}
