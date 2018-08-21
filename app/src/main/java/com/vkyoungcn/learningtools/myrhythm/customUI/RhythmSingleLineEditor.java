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
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;

public class RhythmSingleLineEditor extends RhythmSingleLineView{
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
//* 在所有小节之后标示一个+号。
//* 单行模式，绘制中的小节位于屏幕中。

    private static final String TAG = "RhythmEditor";

//    private Context mContext;

//    private boolean ready = false;//由于“未设置”和“已设置但空数据”时数据源列表都是空，因而需要另设字段用于判断数据是否设置完毕。
    //当设置了数据后，置true，可以开始正式绘制。

//    private ArrayList<Byte> rhythmCodes; //数据源，节奏序列的编码。根据该数据生成各字符单元上的绘制信息。
//    private int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
//    private int unitAmount;
//    private int valueOfBeat = 16;
//    private int valueOfSection = 64;

    /*特有*/
    //逻辑
    private int blueBoxSectionIndex = 0;//蓝框位置，小节的索引【注意，是针对dU列表而言的索引，由于code中多一个延音弧尾端标记，所以无法对应。】
    private int blueBoxUnitIndex = 0;//蓝框位置(小节内du的索引)

//    private ArrayList<ArrayList<Byte>> codesInSections;//对数据源进行分析处理之后，按小节归纳起来。便于进行按节分行的判断。
//    private ArrayList<ArrayList<DrawingUnit>> drawingUnits;//绘制数据也需要按小节组织，以便与按小节组织的代码一并处理。
//    private Lyric lyric_1;
//    private Lyric lyric_2;

    /* 设置参数*/
    //设置参数与数据源一并设置
    private boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
    private boolean useMultiLine = false;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）


    //    private boolean isDataInitBeInterruptedBecauseOfNoSize = false;

    /* 画笔组*/
//    private Paint bottomLinePaint;
//    private Paint codePaint;
//    private Paint curveNumPaint;//用于绘制均分多连音弧线中间的小数字，其字符尺寸较小
//    private Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。
    private Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
//    private Paint textWaitingPaint;
//    private Paint maskPaint;
//    private Paint slidingBallPaint;
//    private Paint slidingVerticalBarPaint;
//    private Paint slidingVerticalBarCenterBoxPaint;

    private Paint blueBoxPaint;



    /* 尺寸组 */
//    private float padding;
//    private float unitStandardWidth;//24dp。单个普通音符的基准宽度。【按此标准宽度计算各节需占宽度；如果单节占宽超屏幕宽度，则需压缩单节内音符的占宽；
    // 如果下节因为超长而移到下一行，且本行剩余了更多空间，则需要对各音符占宽予以增加（但是字符大小不变）】
    private float unitStandardHeight;//24dp。单个普通音符的基准高度。

//    private float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），暂定12dp。
    //注意，一个节拍内的音符之间没有额外间隔。

//    private float lineGap;//不同行之间的间隔。暂定12dp；如果有文字行则需额外安排文字空间。
//    private float additionalHeight;//用于上下加点绘制的保留区域，暂定6dp
//    private float curveOrLinesHeight;//用于绘制上方连音线或下方下划线的空间（上下各一份），暂定8dp

//    private float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
//    private float curveNumSize;

//    private int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
//    private int sizeChangedWidth = 0;//未获取数据前设置为0

//    private float slidingVerticalBarShort;
//    private float slidingVerticalBarMedium;
//    private float slidingVerticalBarLong;
//    private float slidingVerticalBarGap;
//    private float slidingBallDiameter;


    /* 色彩组 */
//    private int generalColor_Gray;
    private int editBox_blue;
//    private int slidingMask_white;
//    private int slidingBall_pink;
//    private int slidingVerticalBar_black;


    /* 滑动交互所需变量*/
//    private boolean isSlidingModeOn =false;
//    private float totalRequiredLength = 0;
//    private boolean noNeedToSliding = false;

//    private ArrayList<VerticalBar> verticalBars;//滑动时的刻度
//    private RectF clickingBallRectF;
//    private RectF slidingBarCenterBox;
//    private int leftEndAddedAmount = 0;//判断刻度位置（以便绘制长线的移动后位置）


    public static final int MOVE_NEXT_UNIT = 2901;
    public static final int MOVE_NEXT_SECTION = 2902;
    public static final int MOVE_LAST_UNIT = 2903;
    public static final int MOVE_LAST_SECTION = 2904;
    public static final int MOVE_FINAL_SECTION = 2905;
    public static final int DELETE_MOVE_LAST_SECTION = 2906;



    //用于进入滑动模式时的刻度短线绘制
/*    private class VerticalBar{
        float x;
        float top;
        float bottom;

        public VerticalBar(float x, float top, float bottom) {
            this.x = x;
            this.top = top;
            this.bottom = bottom;
        }
    }*/


    public RhythmSingleLineEditor(Context context) {
        super(context);
        mContext = context;
//        init(null);
//        this.listener = null;
    }

    public RhythmSingleLineEditor(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
//        init(attributeset);
//        this.listener = null;
    }


    public RhythmSingleLineEditor(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
//        init(attributeset);
//        this.listener = null;
    }


   /* void init(AttributeSet attributeset) {
        initSizeAndColor();
        initPaint();
        initViewOptions();
    }*/


    void initSizeAndColor() {
        super.initSizeAndColor();
        //本子类特有的（蓝色框的颜色）
        editBox_blue = ContextCompat.getColor(mContext, R.color.rhythmView_edBoxBlue);

    }

    void initPaint() {
        super.initPaint();

        //本类特有
        blueBoxPaint = new Paint();
        blueBoxPaint.setStyle(Paint.Style.STROKE);
        blueBoxPaint.setStrokeWidth(2);
        blueBoxPaint.setColor(editBox_blue);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw: 1st DU.code="+drawingUnits.get(0).get(0).code);
        super.onDraw(canvas);

        //本类特有：蓝框
        DrawingUnit blue_dU = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
            canvas.drawRect(blue_dU.left, blue_dU.top, blue_dU.right, blue_dU.bottomNoLyric, blueBoxPaint);

//            invalidate();
    }



    /* 设置方法使用基类的*/
    /* initDrawingUnits及后续的initSectionDrawingUnit_singleLineMode也使用直接基类（单行View）的。
    因为特有的蓝框特征不需在du中存储，只是额外持有两个索引坐标而已。*/

    //编码数据改变，但位置不改变
    public void codeChangedReDraw(ArrayList<ArrayList<Byte>> newCodes2Dimension){
        codesInSections =newCodes2Dimension;//不传不行啊……并不能更新绘制结果（测试发现dus还改变了）
        initDrawingUnits(false);
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
