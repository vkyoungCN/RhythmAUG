package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.graphics.RectF;

import java.util.ArrayList;

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
//        public byte additionalSpotType = 0;//上下加点类型，默认0（无加点）；下加负值、上加正值。原则上不超正负3。
//        public byte bottomLineAmount = 0;//并不是所有音符都有下划线。

    /* 作为一个绘制单位，其整体的左端起始位置
     * 用于使后续单位建立自己的位置*/

    //【由于各字段采用public可直接设置、访问；因而不需要getter和setter也行】
    public boolean isOutOfUi = false;//（单行模式中）如果本单元超出了UI的绘制区域，标true，不绘制。
    // （含超出scw值、超出0（负值）两类，水平+垂直方向上共4种情况）
    public float shiftAmountToCenterX = 0;//暂时仅在超出绘制区域后才生效，否则置0。
    public float shiftAmountToCenterY = 0;

    public float left;
    public float right;
    public float top;
    public float bottomNoLyric;//没有歌词时的绘制底边位置。【几乎一半的Du组件的纵向要依赖本字段完成。】
    public float bottomWithLyric;//有歌词时（可能一行，最多两行）

    public String code = "X";//默认是X，当作为旋律绘制时绘制具体音高的数值。
    public float codeCenterX;//用于字符绘制（字符底边中点）//计算时保证按中点计算就可行。
    public float codeBaseY;//字符底边【待？基线还是底边？】

    /*public String word_1 = "";//歌词。注意顺序和位置，没有词的位置上留空
    public float word_1_CenterX;
    public float word_1_BaseY;

    public String word_2 = "";//歌词。注意顺序和位置，没有词的位置上留空【暂时只绘制一行歌词，暂时位于bottom下方两个标准单位】]
    public float word_2_CenterX;
    public float word_2_BaseY;
*/
    public ArrayList<BottomLine> bottomLines = new ArrayList<>();//已实例化，直接add即可。
    public RectF[] additionalPoints = new RectF[]{};//上下加点

    public int mCurveNumber = 0;//在均分多连音情况下，顶弧中间有一个小数字；
    public float mCurveNumCenterX;
    public float mCurveNumBaseY;

    public String lyricWord_1 = "";//普通dU为一个汉字，如果是均分多连音，安排若干个汉字；
    public float lyricWord_1_CenterX;
    public float lyricWord_1_BaseY;

    public String lyricWord_2 = "";//普通dU为一个汉字，如果是均分多连音，安排若干个汉字；
    public float lyricWord_2_CenterX;
    public float lyricWord_2_BaseY;

    public boolean isEndCodeOfLongCurve = false;//当编码遇到112~127数值时，需要在将前一个音符的du中的本字段设true，且记录弧线跨度。
    public int curveLength = 0;//如果要绘制上方连音弧线（且不是均分多连音），则需记录弧线向前跨越多少个音符。

    public boolean isLastCodeInSection = false;
    public int indexInCodeSerial = 0;//某些功能下要求能从指定的dU快速/直接获取所对应的在原始编码中的索引位置。

    //一个字符的空间方案暂定如下：标志尺寸ss,字符区域占宽=ss（字体尺寸本身不足的留空即可），
    // 字符占高=展宽；字符上方预留半ss的顶弧线高度，其中保留一小层的高度作为上加点区域；
    //字符下方半ss空间是下划线区域，下划线下方保留一小层高度作为下加点区域。（小层高度待定，暂定5~8dp）
    //非首尾拍字符之间是没有间隔的，以便令下划线相接。

    //连音线的绘制，将由RhV直接提供方法。程序根据词序缺少位置，指定Rhv在哪些（起止）位置上绘制连音线


    public DrawingUnit() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        //从编码到code需要转换（即使是音高，也因上下加点而多种不同）
        this.code = code;
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

    public boolean checkIsOutOfUi( float availableStart, float availableTop,float availableEnd, float availableBottom){
//        this.isOutOfUi = (right<availableStart||left>availableWidth|| bottomNoLyric <availableTop||top>availableHeight);
        if(right<availableStart){
            isOutOfUi =true;
            shiftAmountToCenterX = (availableStart-left)+(availableEnd-availableStart)/2;//需要向右移动才能到中心，是正值。
        }else if(left>availableEnd){
            isOutOfUi = true;
            shiftAmountToCenterX = -((right-availableEnd)+(availableEnd-availableStart)/2);//需向左
        }else {
            shiftAmountToCenterX =0;//不超，置0。
        }

        if(bottomNoLyric<availableTop){
            isOutOfUi=true;
            shiftAmountToCenterY = (availableTop-top)+(availableBottom-availableTop)/2;
        }else if(top>availableBottom){
            isOutOfUi=true;
            shiftAmountToCenterY = -((bottomNoLyric-availableBottom)+(availableBottom-availableTop)/2);
        }else {
            shiftAmountToCenterY = 0;//不超，置0。
        }

        return isOutOfUi;
    }

    public void shiftEntirely(float h_shiftAmount, float v_shiftAmount, float availableStart, float availableTop,float availableEnd, float availableBottom){
        //【*注意：绘制时，如果小于0，原则上不予绘制。】

        this.left +=h_shiftAmount;
        this.right+=h_shiftAmount;
        this.top+=v_shiftAmount;
        this.bottomNoLyric +=v_shiftAmount;

        //（注意判断条件，完全遮蔽时）【牛X的写法！】
        //【但是注意，这个条件没有考虑内边距问题！】
        this.isOutOfUi = checkIsOutOfUi(availableStart, availableTop,availableEnd, availableBottom);
//        isOutOfUi = (right<0||left>availableWidth|| bottomNoLyric <0||top>availableHeight);

        /*if(right<0||left>sizeChangedWidth||bottomNoLyric<0||top>sizeChangedHeight){
            //【注意这个判断条件，可能有些别扭；目的是在完全遮蔽时才不绘制】
            isOutOfUi = true;
        }else {
            //四项中有一项不满足（即仍然可以看到一点）则仍然绘制
            isOutOfUi = false;
        }【这种写法，编辑器提示可简化为上方目前采用的写法】*/
        //注意，虽然标记为不绘制，但数据仍然必须修改。

        this.codeCenterX +=h_shiftAmount;//用于字符绘制（字符底边中点）
        this.codeBaseY+=v_shiftAmount;//字符底边【待？基线还是底边？】

        for (BottomLine bl :this.bottomLines) {
            bl.startX+=h_shiftAmount;
            bl.startY+=v_shiftAmount;
            bl.toX+=h_shiftAmount;
            bl.toY+=v_shiftAmount;
        }

        for (RectF rf :additionalPoints) {
            rf.left+=h_shiftAmount;
            rf.right+=h_shiftAmount;
            rf.top+=v_shiftAmount;
            rf.bottom+=v_shiftAmount;
        }

        this.mCurveNumCenterX +=h_shiftAmount;
        this.mCurveNumBaseY +=v_shiftAmount;

        this.lyricWord_1_CenterX +=h_shiftAmount;
        this.lyricWord_1_BaseY +=v_shiftAmount;

        this.lyricWord_2_CenterX +=h_shiftAmount;
        this.lyricWord_2_BaseY +=v_shiftAmount;

    }


}
