package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;

import java.util.ArrayList;

public class RhythmView extends View {
//* 显示节奏序列
//* 有“折行、单行”两种模式；字符及字宽有“大中小三种模式”
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。
//*
//*
//*

    private static final String TAG = "RhythmView";

    /* 常量*/
    public static final int CODE_SMALL = 2001;
    public static final int CODE_MEDIUM = 2002;
    public static final int CODE_LARGE = 2003;

    public static final int RHYTHM_TYPE_24 = 2004;
    public static final int RHYTHM_TYPE_34 = 2005;
    public static final int RHYTHM_TYPE_44 = 2006;
    public static final int RHYTHM_TYPE_38 = 2007;
    public static final int RHYTHM_TYPE_68 = 2008;




    private Context mContext;

    private ArrayList<Byte> rhythmCodes; //数据源，节奏序列的编码。根据该数据生成各字符单元上的绘制信息。
    private int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
//    private String rhythmCodes = "";//用于比较的目标字串

//    private LimitedStack<Character> characters;
    private DrawingUnit drawingUnits[];
//    private int currentPosition = 0;//第一个字母的位置是1。
    // 的输入记录列表；（考虑取消onCode单个改变的监听）②

//    private boolean isDataInitBeInterruptedBecauseOfNoSize = false;

    /* 画笔组*/
    private Paint bottomLinePaint;
    private Paint codePaint;
    private Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。
    private Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
//    private Paint textWaitingPaint;

    /* 尺寸组 */
    private float padding;
    private float unitSizeSmall;//24dp。【暂定长宽相等】
    private float unitSizeMedium;//30
    private float unitSizeLarge;//36

    private float unitSize;//最终选定的单位尺寸
    private float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），这里采取拍间半字符、节间全字符间隔。
    //注意，一个节拍内的音符之间没有额外间隔。

    private float heightAddition_singleSide;//在上方留出连音线位置、上加点位置、下方留出下加点位置。单侧8dp

    //下划线绘制为2dp(或1dp)每条
    private float textSizeSmall;//14sp
    private float textSizeMedium;//16sp
    private float textSizeLarge;//24sp


//    private float sectionGapLarge;//6dp【根据模拟器表现调整】
//    private float sectionGapSmall;//4dp
//    private float bottomLineHeightLarge;//4dp
//    private float bottomLineHeightSmall;//2dp

//    private float maxSectionWidth;//给定一个宽度的最大值；当字符过多总长超出屏幕时，缩小这一宽度（相应的字体也要缩小）
//    private float finalLineWidth;//最终确定的每节宽度（由计算获得，而不是初始化时设定）

    private float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
    private float textBaseLineBottomGap;

    int lines = 1;//控件需要按几行显示，根据当前屏幕下控件最大允许宽度和控件字符数（需要的宽度）计算得到。

    private int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    private int sizeChangedWidth = 0;//未获取数据前设置为0

//    private int bottomLineSectionAmount = DEFAULT_LENGTH;

    /* 色彩组 */
    private int generalColor_Gray;
//    private int bottomErrColor;
//    private int textColor;
//    private int textErrColor;
//    private int backgroundColor;
//    private int backgroundErrColor;
//    private int mInputType;

//    private boolean stopDrawing = false;

    //用于描述各字符对应的下划线的一个内部类
    public class DrawingUnit {

        private String code = "X";//默认是X，当作为旋律绘制时绘制具体音高的数值。
        private boolean isLastCodeInSection = false;//如果是小节内最后一个音符，需要记录一下，以便在遍历绘制时在后面绘制一条竖线（小节线）
        //拍子之间、小节之间有额外间隔，由设置方法负责逻辑计算然后存给本数据类，本类不需持有相应位置信息。
        //最前端的一条小节线由绘制方法默认绘制，不需记录。

        private byte additionalSpotType = 0;//上下加点类型，默认0（无加点）；下加负值、上加正值。原则上不超正负3。
        private byte bottomLineAmount = 0;//并不是所有音符都有下划线。


        private float codeCenterX;//用于字符绘制（字符底边中点）
        private float codeBaseY;//字符底边【待？基线还是底边？】

        private float firstBottomLineFromX;//其实所有下划线的X一致
        private float firstBottomLineFromY;//第2、3线手动加上间隔像素值（暂定间隔4像素）
        private float firstBottomLineToX;
        private float firstBottomLineToY;


        //连音线的绘制，由RhV直接提供方法。程序根据词序缺少位置，指定Rhv在哪些（起止）位置上绘制连音线


        public DrawingUnit() {
        }

        public DrawingUnit(String code, byte additionalSpotType, byte bottomLineAmount, float codeCenterX, float codeBaseY, float firstBottomLineFromX, float firstBottomLineFromY, float firstBottomLineToX, float firstBottomLineToY) {
            this.code = code;
            this.additionalSpotType = additionalSpotType;
            this.bottomLineAmount = bottomLineAmount;
            this.codeCenterX = codeCenterX;
            this.codeBaseY = codeBaseY;
            this.firstBottomLineFromX = firstBottomLineFromX;
            this.firstBottomLineFromY = firstBottomLineFromY;
            this.firstBottomLineToX = firstBottomLineToX;
            this.firstBottomLineToY = firstBottomLineToY;
        }

        public byte getBottomLineAmount() {
            return bottomLineAmount;
        }

        public void setBottomLineAmount(byte bottomLineAmount) {
            if(bottomLineAmount>3){
                Toast.makeText(mContext, "音符下划线过多？请检查谱子是否正确。", Toast.LENGTH_SHORT).show();
                return;
            }else if (bottomLineAmount<0){
                Toast.makeText(mContext, "音符下划线数值设置错误。", Toast.LENGTH_SHORT).show();

            }
            this.bottomLineAmount = bottomLineAmount;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            //从编码到code需要转换（即使是音高，也因上下加点而多种不同）
            this.code = code;
        }

        public byte getAdditionalSpotType() {
            return additionalSpotType;
        }

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

        public float getCodeCenterX() {
            return codeCenterX;
        }

        public void setCodeCenterX(float codeCenterX) {
            this.codeCenterX = codeCenterX;
        }

        public float getCodeBaseY() {
            return codeBaseY;
        }

        public void setCodeBaseY(float codeBaseY) {
            this.codeBaseY = codeBaseY;
        }

        public float getFirstBottomLineFromX() {
            return firstBottomLineFromX;
        }

        public void setFirstBottomLineFromX(float firstBottomLineFromX) {
            this.firstBottomLineFromX = firstBottomLineFromX;
        }

        public float getFirstBottomLineFromY() {
            return firstBottomLineFromY;
        }

        public void setFirstBottomLineFromY(float firstBottomLineFromY) {
            this.firstBottomLineFromY = firstBottomLineFromY;
        }

        public float getFirstBottomLineToX() {
            return firstBottomLineToX;
        }

        public void setFirstBottomLineToX(float firstBottomLineToX) {
            this.firstBottomLineToX = firstBottomLineToX;
        }

        public float getFirstBottomLineToY() {
            return firstBottomLineToY;
        }

        public void setFirstBottomLineToY(float firstBottomLineToY) {
            this.firstBottomLineToY = firstBottomLineToY;
        }
    }

    //用于装载字符数据的栈
    /*public class LimitedStack<T> extends Stack<T> {

        private int topLimitSize = 0;

        @Override
        public T push(T object) {
            if (topLimitSize > size()) {
                return super.push(object);
            }

            return object;
        }

        public int getTopLimitSize() {
            return topLimitSize;
        }

        public void setTopLimitSize(int topLimitSize) {
            this.topLimitSize = topLimitSize;
        }
    }*/

//    private OnValidatingEditorInputListener listener;

    public RhythmView(Context context) {
        super(context);
        mContext = context;
        init(null);
//        this.listener = null;
    }

    public RhythmView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    public RhythmView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }

 /*   public void setCodeReadyListener(OnValidatingEditorInputListener listener) {
        this.listener = listener;
    }*/

    private void init(AttributeSet attributeset) {
        initSize();
        initColor();
        initPaint();
        initViewOptions();
    }

    //调用方（activity）实现本接口。VE中获取对调用方Activity的引用，然后调用这两个方法进行通信
 /*   public interface OnValidatingEditorInputListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered

        *//* 所有字符输入完毕且正确时触发 *//*
        void onCodeCorrectAndReady();

        *//* 当输入一个（有效）字符，使VE的显示发生变化时触发 *//*
        void onCodeChanged(String newStr);

    }*/

    private void initSize() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        unitSizeSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        unitSizeMedium = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        unitSizeLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());

        heightAddition_singleSide = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        textBaseLineBottomGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

//        maxSectionWidth =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
//        maxSectionWidth = getContext().getResources().getDimension(R.dimen.bottomLine_stroke_width);//【旧方法？】查API知此方法自动处理单位转换。
//        sectionGapLarge = getContext().getResources().getDimension(R.dimen.bottomLine_horizontal_margin);
//        sectionGapLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
//        sectionGapSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
//        bottomLineHeightLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
//        bottomLineHeightSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

        textSizeSmall =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        textSizeMedium =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        textSizeLarge =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());
//        viewHeight = getContext().getResources().getDimension(R.dimen.view_height);
    }

    private void initColor(){
        generalColor_Gray = ContextCompat.getColor(mContext, R.color.rhythmView_generalGray);

//        bottomErrColor = ContextCompat.getColor(mContext,R.color.ve_bottomLine_nonCorrect_color);
//        textColor = ContextCompat.getColor(mContext,R.color.ve_textColor);
//        textErrColor = ContextCompat.getColor(mContext,R.color.ve_text_err_color);
//        backgroundColor = ContextCompat.getColor(mContext,R.color.ve_background);
//        backgroundErrColor = ContextCompat.getColor(mContext,R.color.ve_background_not_correct);
    }



    private void initPaint() {
        bottomLinePaint = new Paint();
        bottomLinePaint.setColor(generalColor_Gray);
        bottomLinePaint.setStrokeWidth(2);//
        bottomLinePaint.setStyle(android.graphics.Paint.Style.STROKE);



        codePaint = new Paint();
        codePaint.setTextSize(textSize);
//        codePaint.setStrokeWidth(4);
        codePaint.setColor(generalColor_Gray);
        codePaint.setAntiAlias(true);
        codePaint.setTextAlign(Paint.Align.CENTER);

        grayEmptyPaint = new Paint();
        grayEmptyPaint.setStyle(Paint.Style.FILL);
        grayEmptyPaint.setStrokeWidth(4);
        grayEmptyPaint.setAntiAlias(true);
        grayEmptyPaint.setColor(generalColor_Gray);

        //        bottomLineErrPaint = new Paint();
//        bottomLineErrPaint.setColor(bottomErrColor);
//        bottomLineErrPaint.setStrokeWidth(bottomLineHeightLarge);
//        bottomLineErrPaint.setStyle(android.graphics.Paint.Style.STROKE);

//        textErrPaint = new Paint();
//        textErrPaint.setTextSize(textSize);
//        textErrPaint.setStrokeWidth(4);
//        textErrPaint.setColor(textErrColor);
//        textErrPaint.setAntiAlias(true);
//        textErrPaint.setTextAlign(Paint.Align.CENTER);//如果开启了这个，x坐标就不再是左端起点而是横向上的中点。

//        textWaitingPaint = new Paint();
//        textWaitingPaint.setTextSize(textSize);
//        textWaitingPaint.setStrokeWidth(4);
//        textWaitingPaint.setColor(textErrColor);
//        textWaitingPaint.setAntiAlias(true);
//        textWaitingPaint.setTextAlign(Paint.Align.CENTER);//如果开启了这个，x坐标就不再是左端起点而是横向上的中点。

//        backgroundErrPaint = new Paint();
//        backgroundErrPaint.setStyle(Paint.Style.FILL);
//        backgroundErrPaint.setStrokeWidth(4);
//        backgroundErrPaint.setAntiAlias(true);
//        backgroundErrPaint.setColor(backgroundErrColor);

    }


    private void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    //【学：据说是系统计算好控件的实际尺寸后以本方法通知用户】
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
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));

    }

   /* @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        return new VeInputConnection(this,false);
    }

    //    此方法是参照网帖学习而来，暂时不太懂其设置的必要性。
    private class VeInputConnection extends BaseInputConnection {
        public VeInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            CharSequence firstCharText = String.valueOf(text.charAt(0));
            //只传出其第一个字符
            return super.commitText(firstCharText, newCursorPosition);
        }
    }*/

   /* @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }*/



    /**
     * Detects the del key and delete characters
     */
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && characters.size() != 0) {
            characters.pop();
            currentPosition--;
            if(currentPosition<leastWrongPosition){
                leastWrongPosition = 0;
            }
//            Log.i(TAG, "onKeyDown: currentPos="+currentPosition);
            invalidate();//字符改变，重绘

            if(!hasCorrectOnce) {
                //尚未完整正确输入过一次，改变字符的监听仍然在
                listener.onCodeChanged(getCurrentString());
            }
        }
        return super.onKeyDown(keyCode, keyevent);
    }*/

    /**
     * Capture the keyboard events, for inputs
     */
    /*@Override
    public boolean onKeyUp(int keyCode, KeyEvent keyevent) {

        String text = KeyEvent.keyCodeToString(keyCode);//返回的一定是KEYCODE_开头（已知字符）或数字1001（未知字符）

        if(!keyevent.isCapsLockOn()) {
            return inputText(text,false);
        }
        return inputText(text,true);
    }*/

    /**
     * String text
     * Pass empty string to remove text
     */
    /*private boolean inputText(String text, boolean capsOn) {
        Matcher matcher = KEYCODE_PATTERN.matcher(text);
        if (matcher.matches()) {
            String matched = matcher.group(1);
            char character;
            if(!capsOn){
                character = matched.toLowerCase().charAt(0);
            }else {
                character = matched.charAt(0);
            }
            characters.push(character);

            if (characters.size() >= rhythmCodes.length() ) {//满了【重绘必须在回调之前，且两分支都要有！（排错小结）】
                currentPosition = rhythmCodes.length();//【这里既不能继续++，也不能保持数字不变，所以Z直接设置为最大值】
                invalidate();//字符改变，重绘
//                Log.i(TAG, "inputText: currentPos inside VE ="+currentPosition);

                if(getCurrentString().compareTo(rhythmCodes) == 0 && !hasCorrectOnce) {
                    //必须是尚未正确输入过的状态才能触发两个监听方法
                    hasCorrectOnce = true;//修改标记为已经（有过一次）完整正确输入。

                    if(listener != null) {
                        listener.onCodeChanged(getCurrentString());//需要在onCCA方法前调用，
                        // 实测如果放在下一方法后，则最后一个字符无法传出。可能原因如下，
                        listener.onCodeCorrectAndReady();//【本方法之后卡片自动滑动，相应组件可能已销毁，后续方法无效？】
                        //【目前的设计逻辑下，满了以后，再继续键入，VE显示上仍然是原词，characters也不变，
                        // 但由于本块代码仍然满足触发条件，因而可能会产生“滑动到某已满卡片后，任意敲入一字符则卡片向后滑动”效果】
                    }
                }

            }else {//还没满（但显然也必须是输入开始后，每次输入（且已成功输入到了characters中后）才会触发）
                currentPosition++;
//                Log.i(TAG, "inputText: currentPos inside VE ="+currentPosition);
                //记录输入的字符之出错的各字符中，索引值最小的一个。
                if(Character.compare(character,rhythmCodes.charAt(currentPosition-1))!=0){//此位置上字符输入不正确

                    if(leastWrongPosition==0){
                        leastWrongPosition = currentPosition;//只需记录一次（最小索引位置）即可
                    }

                }
                invalidate();//字符改变，重绘
                if(!hasCorrectOnce) {
                    //有这个监听的地方必须先判断当前的状态是“初次（正确之前）的填写”还是“已正确后的无记录加练”
                    listener.onCodeChanged(getCurrentString());
                }
            }

            return true;
        } else {
            return false;
        }
    }*/



    @Override
    protected void onDraw(Canvas canvas) {
//        Log.i(TAG, "onDraw: characters="+characters.toString());
        if(rhythmCodes.isEmpty()) {
            //此时节奏数据还未设置，只在中间高度绘制一条背景
            float fromX = padding;
            float fromY = sizeChangedHeight/2-20;
            float toX = sizeChangedWidth - padding;
            float toY = sizeChangedHeight/2+20;
            //条带总宽40像素。

            canvas.drawRect(fromX,fromY,toX,toY, grayEmptyPaint);
            return;
        }

/*        if(leastWrongPosition!=0) {
            //存在错误的字符
            grayEmptyPaint.setColor(backgroundErrColor);
        }else {
            grayEmptyPaint.setColor(backgroundColor);
        }*/

        //绘制背景
      /*  float fromX = padding-8;
        float fromY = padding;
        float toX = sizeChangedWidth - padding+8;
        float toY = sizeChangedHeight - padding;*/
//        canvas.drawRect(0,0,600,600,grayEmptyPaint);
//        canvas.drawRect(fromX,fromY,toX,toY, grayEmptyPaint);
//        Log.i(TAG, "onDraw: background done");

        //起端没有小节线，小节线只存在于末尾

        //绘制下划线和符号
        for (int i = 0; i < drawingUnits.length; i++) {
            DrawingUnit drawingUnit = drawingUnits[i];

            //下划线绘制
            int bottomLineAmount = drawingUnit.getBottomLineAmount();
            if(bottomLineAmount>0){
                //>0时先绘制第一条
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY(), drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY(), bottomLinePaint);
            }
            if(bottomLineAmount>1){
                //>1，再绘制第二条(此时>0也满足因而第一条已经绘制，以下同理)
                //【各条线线宽2像素，线间隔2像素。】
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY()+4, drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY()+4, bottomLinePaint);
            }
            if(bottomLineAmount>2){
                //>2，再绘制第3条
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY()+8, drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY()+8, bottomLinePaint);
            }//最多绘制三条；如果==0则不绘制下划线

            //字符
            canvas.drawText(drawingUnit.getCode(),drawingUnit.getCodeCenterX(),drawingUnit.getCodeBaseY(), codePaint);


            //如果是小节末尾，要绘制尾端小节竖线
            if(drawingUnit.isLastCodeInSection){
                float fromX = drawingUnit.firstBottomLineToX+beatGap/2;
                float fromY = drawingUnit.codeBaseY-textSize;//【目标是与字符同高，但不知是否如此实现】
                float toX = drawingUnit.firstBottomLineToX+beatGap/2;//竖线，x不变。
                float toY = drawingUnit.firstBottomLineToY+8;//与下方第三条线高度约同。
                canvas.drawLine(fromX, fromY, toX, toY, bottomLinePaint);
            }
        }
//            invalidate();

    }


/*    public String getCurrentString() {
        StringBuilder sbd = new StringBuilder();
        for (Character c :
                characters) {
            sbd.append(c);
        }

        return sbd.toString();
    }*/




    /*
     * 方法由程序调用，动态设置目标字串
     * 设置节拍类型（4/4等）
     * 设置字符大小
     * */
    public void setRhythmViewInfo(ArrayList<Byte> rhythmCodes,byte rhythmType, int codeSize){
        this.rhythmCodes = rhythmCodes;
        this.rhythmType = rhythmType;
        switch (codeSize){
            case CODE_SMALL:
                this.textSize = textSizeSmall;
                break;
            case CODE_MEDIUM:
                this.textSize = textSizeMedium;
                break;
            case CODE_LARGE:
                this.textSize = textSizeLarge;
                break;
        }

        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        initData();
    }

    //onSizeC方法中会调用initData，该时点可能尚未设置必要的数据，所以需要判断。??
    //事实上，onS和setRC谁先谁后可能没有定数。两种错误都遇到过。??


    private void initData() {
        //根据初始化了的数据源初始化绘制信息数组
        //本方法在设置数据源的方法内调用，因而必然非空不需判断
        drawingUnits = new DrawingUnit[rhythmCodes.size()];

        //如果这个时候还没有获取到尺寸信息则终止操作
        // 在onSizeChanged()中会对isDataInitBeInterruptedBecauseOfNoSize变量进行判断；
        // 发现终止记录时将再次恢复（直接进入下一级方法）。
        if(sizeChangedWidth == 0){
//            isDataInitBeInterruptedBecauseOfNoSize = true;
            return;
        }
        initDrawingUnits(false);

    }

    private void initDrawingUnits(boolean isTriggerByOnSC) {

        int unitAmount = rhythmCodes.size();

        drawingUnits = new DrawingUnit[unitAmount];
        for(int i=0;i<rhythmCodes.size();i++){//必须得这样彻底初始化，如果只有上一句而不进行for循环初始则崩溃。
            drawingUnits[i] = new DrawingUnit();
        }

        //可用总长（控件宽扣除两侧缩进）
        float availableTotalWidth = sizeChangedWidth - padding*2;

        int valueOfSection = 64;//默认64，4/4。
        int valueOfBeat = 16;//默认一拍16值。
        switch (rhythmType){
            case RHYTHM_TYPE_24:
                valueOfSection = 32;
            break;
            case RHYTHM_TYPE_34:
                valueOfSection = 48;
                break;
            case RHYTHM_TYPE_44:
                valueOfSection = 64;
                break;
            case RHYTHM_TYPE_38:
                valueOfSection = 24;
                break;
            case RHYTHM_TYPE_68:
                valueOfSection = 48;
                break;

        }
        float currentTotalWidth = 0;//累加长度；
        int currentSectionValue = 0;//从本小节首开始计算的时值长度；跨节后重置。
        //计算能否按指定尺寸在一行内容纳所有内容，否则计算出所要占据的行数和何处换行
        for(int i =0; i<unitAmount;i++){
            byte currentCode = rhythmCodes.get(i);
            if(currentCode>0&&currentCode<=24){
                //这个范围内的编码，代表正常音符，可以正常计算时值
                currentSectionValue += currentCode;
            }else {
                //其他情形处理
                【待】
            }
            if()


        }
        if((unitAmount*maxSectionWidth+(unitAmount-1)*sectionGapLarge<=availableTotalWidth)){
            //可以容纳，使用大尺寸间隔和既定的大尺寸每节长度、大尺寸字符
            for (int i = 0; i < unitAmount; i++) {
                drawingUnits[i].firstBottomLineFromX =padding +(maxSectionWidth+sectionGapLarge)*i;
                //注意，先确定下方位置再确定上方位置。说明控件是靠下的gravity。
                drawingUnits[i].firstBottomLineFromY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToX = drawingUnits[i].firstBottomLineFromX +maxSectionWidth;
//                Log.i(TAG, "initDrawingUnits: fx="+drawingUnits[i].firstBottomLineFromX);
//                Log.i(TAG, "initDrawingUnits: fy="+drawingUnits[i].firstBottomLineFromY);

            }
        }else {
            //使用小尺寸间隔以及动态确定的每节长度【字符大小还需另行确定】
            float totalWidthPureForLines =  availableTotalWidth-((unitAmount-1)*sectionGapSmall);
            finalLineWidth = totalWidthPureForLines/unitAmount;

            for (int i = 0; i < unitAmount; i++) {
                drawingUnits[i].firstBottomLineFromX =padding +(finalLineWidth+sectionGapSmall)*i;
                //注意，先确定下方位置再确定上方位置。说明控件是靠下的gravity。
                drawingUnits[i].firstBottomLineToY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineFromY =sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToX = drawingUnits[i].firstBottomLineFromX +finalLineWidth;
            }

            //改文字画笔的字号大小
            float smallerTextSize = (textSize/maxSectionWidth)*finalLineWidth;
            codePaint.setTextSize(smallerTextSize);
            textWaitingPaint.setTextSize(smallerTextSize);
            textErrPaint.setTextSize(smallerTextSize);

        }
//        Log.i(TAG, "initDrawingUnits: invalidate, init="+initText);

        if(!isTriggerByOnSC) {
            //如果是在onSizeChanged方法内被触发，则不应调用刷新方法（因为会自动继续调用onDraw()）
            invalidate();//完成了目标数据、下划线的初始化后刷新控件。
        }
/*
        if(lines ==1) {
            for (int i = 0; i < bottomLinesAmount; i++) {
                drawingUnits[i] = createPath(i, 1,1, finalSectionWidth);
            }
        }else {
            int sectionsMaxAmountPerLine = (int)(viewMaxWidth/ finalSectionWidth);
            for (int i = 0; i < bottomLinesAmount; i++) {
                int currentLine = (i/sectionsMaxAmountPerLine)+1;
                int positionInLine = i%sectionsMaxAmountPerLine;
                drawingUnits[i] = createPath(positionInLine, lines,currentLine, finalSectionWidth);
            }
        }
*/
    }

    /*
     * 有时候VE带有初始数据（比如卡片再次滑回时）
     * */
    public void setInitText(String initText) {
        this.initText = initText;
//        Log.i(TAG, "setInitText: String Received in Ve = "+initText);

        //存入数据数组
        if(initText!=null){
            //先对旧数据清空
            characters.clear();

            //存入数据数组
            char[] chars = initText.toCharArray();
            for (char c: chars){
                characters.push(c);
            }
        }

        invalidate();//刷新显示。
    }
}
