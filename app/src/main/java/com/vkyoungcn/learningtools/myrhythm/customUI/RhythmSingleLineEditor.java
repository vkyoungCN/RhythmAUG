package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;

import java.util.ArrayList;

public class RhythmSingleLineEditor extends RhythmSingleLineWithTwoTypeBoxBaseView{
//* 如果数据源为空，自动显示一个空的小节；如果有数据显示数据，并将第一音符标蓝框；
    private static final String TAG = "RhythmSingleLineEditor";


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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //本类特有：蓝框、绿框。【各种模式下，蓝框均绘制】
        if(!selectionAreaMode){
            //单点选择模式，绘制蓝框
            DrawingUnit drawingUnit = drawingUnits.get(blueBoxSectionIndex).get(blueBoxUnitIndex);
//            Log.i(TAG, "onDraw: this du isOutOfUi = "+drawingUnit.isOutOfUi);
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
        }*/
//            invalidate();
    }
}
