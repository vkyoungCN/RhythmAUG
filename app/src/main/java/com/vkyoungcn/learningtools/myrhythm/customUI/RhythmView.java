package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.util.AttributeSet;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;


public class RhythmView extends BaseRhythmView {
//* 显示节奏序列
//* 折行模式；字符及字宽采用默认值，可以调节
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。

//* 三种工作模式：“节奏/旋律/有词”
//* 其中有词模式下需要绘制上方连音弧线；旋律模式下需要绘制上下方的加点。

    private static final String TAG = "RhythmView";


    public RhythmView(Context context) {
        super(context);
    }

    public RhythmView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
    }

    public RhythmView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
    }

    /* 覆写一下默认尺寸的设置方法，其尺寸改小。*/
    @Override
    public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound) {
        super.setRhythmViewData(rhythmBasedCompound,14,18,18);
    }


    /*
     * 目前（已分配的）编码
     * ①0：延音符-
     * ②1、2、3、4、6、8、12、16、24。各种时值的实体节奏符X（带下划线的是基本、非附点的音符）
     * ③上述（②条目中）各值的负值：各种时值的空拍子
     * ④73~79、83~89、93~99，总时值分别为（1/4、1/8、1/16）的均分多连音；
     * 以code%10的余数表示其内含的多连音个数（3~9个）【编辑器暂时只支持3、5、7连音】
     * ⑤112~125：连音弧结束标记，（以code-110代表其跨度，暂时支持2~15跨度）
     * ⑥126：拍尾标记
     * ⑦127：小节尾标记
     * */
    void initDrawingUnits_step1() {
        //【如果本方法是从onSc中触发的调用，则最后不进行invalidate()】
        //本控件采用折行模式。
        super.initDrawingUnits_step1();//完成部分初始化任务


        //开始计算绘制信息。以小节为单位进行计算（根据折行规则具体安排实现）。
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));
            //记录到本行所需总长度
            lineRequiredLength += sectionRequiredLength;
            //本行既有小节数量+1
            sectionAmountInLine++;

            //针对本小节的宽度/本行既有小节总宽度 与 本行的屏幕宽度进行比较判断。
            /* 折行逻辑（核心算法1）*/
            if (sectionRequiredLength > availableTotalWidth) {
                //单节宽度大于屏幕宽度
                // 如果本行已有其他节则需要压缩本节、扩展其他节；且本节下移
                // 否则（本行只有本节自己）压缩本节，下一节新起一行（行宽计数重置）
                if (sectionAmountInLine == 1) {
                    //①本行只有本节自己，对本节的单位宽度压缩（不需下移）【暂定只压缩宽度；而高度、字号可以一定程度上保持不变】
                    float unitWidthSingleZipped = (availableTotalWidth / sectionRequiredLength) * unitWidth;//与外部使用的uW变量同名
                    //【按调整后的宽度计算本行本节的绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidthSingleZipped );

                    drawingUnits.add(sectionDrawingUnit);//添加到dUs列表
//                    accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

                    //②行需求宽度计数器重置，③本行节索引重置
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;

                    lineCursor++;//行计数器+1，以备下节正确使用
                } else {
                    //本行超过1节（除本节外还有其他节），其他节扩展
                    extraOthersInLine(availableTotalWidth,lineRequiredLength,sectionRequiredLength,i,sectionAmountInLine,topDrawing_Y,lineCursor);

                    //本节计算：本节下移,独占一行，且压缩本节
                    lineCursor++; //行计数器+1（刚才未++前，是原“本行”其他节的高度计算；现在是新行计算）
                    float unitWidth_zipped = (availableTotalWidth / sectionRequiredLength) * unitWidth;
                    //【在此计算本节绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit_3 = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidth_zipped);
                    drawingUnits.add(sectionDrawingUnit_3);//添加到dUs列表

                    //行需求宽度计数器重置(以备下行使用)，本行节索引重置（以备下行使用）
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;

                    //计算完成后，行计数器要再加1以备下行使用
                    lineCursor++;

                }
            } else if (lineRequiredLength > availableTotalWidth) {
                //单节宽度不大于控件可用宽度，但是行累加宽度超过了；
                // 本行其他节要放大
                extraOthersInLine(availableTotalWidth,lineRequiredLength,sectionRequiredLength,i,sectionAmountInLine,topDrawing_Y,lineCursor);

                //本节下移，尺寸上暂不做特别处理，但需要将本节加入下一行的宽和索引记录。
                //②行需求宽度计数器重置，③本行节索引重置
                lineCursor++;//本行现处“下一行”，加1预备。
                lineRequiredLength = sectionRequiredLength;//将下一行的宽度重置为本节宽度，以便后续累加。
                sectionAmountInLine =1;//将本节仍然要位于计数内（因为本节单节不超屏宽，移到下行后，后面可能会有多节）
                // 【因为开头的add只能负责将后续小节的计数进行添加，而本项是无法（以该方式）计入；只有在此手动添加一次】

                //但是要判断此节是否是最后一节，如果是，则应扩展本节占据全行宽度
                // （另一种思路是在后方填充空拍小节，但在“本节超宽下移，剩余节只有一节”分支下，剩余节即使很短，也同样会被扩展；
                //且在此计算本节绘制数据。
                if (i == codesInSections.size() - 1) {
                    float unitWidth_singleExtracted = (availableTotalWidth / sectionRequiredLength) * unitWidth;
                    ArrayList<DrawingUnit> sectionDrawingUnit_3 = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidth_singleExtracted);
                    drawingUnits.add(sectionDrawingUnit_3);
                }

//                accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

            } else {
                //单节宽度不超控件允许宽度、本行总宽也未超总宽。
                // 本节索引已在开头自动加入行索引列表，因而不需在此再次操作。
                //在此，需判断本节是否是最后一节，如果是，则将本行现有所有节进行整体扩展，以占满全行宽度
                extraAllInLine(availableTotalWidth,lineRequiredLength,i,sectionAmountInLine,topDrawing_Y,lineCursor);
                //此分支下仅当到达尾节时才进行计算（本行绘制数据的计算）【否则在后面的节超屏宽时会再次触发对本节的计算，重复负荷】
                //其余情况下只是增加本行的小节数量记录即可，不必有其他处理。
            }
        }

        //【这里应根据所需总高对各行进行平移】
        //【如果不处理则默认是顶部对齐的绘制方式】
        float totalHeightNeeded = lineCursor* twoLinesTopYBetween;
        float totalAvailableHeight = sizeChangedHeight-2*padding;
        if(totalHeightNeeded<totalAvailableHeight){
            float shiftAmount = (totalAvailableHeight-totalHeightNeeded)/2;
            for (ArrayList<DrawingUnit> drawingUnitSection: drawingUnits){
                for (DrawingUnit du :drawingUnitSection) {
                    du.shiftEntirely(0, shiftAmount, padding,padding,sizeChangedWidth-padding, sizeChangedHeight-padding);
                }
            }
        }



    }


    //本行内的其他各节扩展宽度
    void extraOthersInLine(float availableTotalWidth,float lineRequiredLength,float sectionRequiredLength,int i,int sectionAmountInLine,float topDrawing_Y,int lineCursor){
        float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength - sectionRequiredLength)) * unitWidth;
        //计算其他节的绘制数据
        float sectionStartX = padding;

        //先对行内首节进行计算
        ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i-sectionAmountInLine+1), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted);
        //并添加到总记录
        drawingUnits.add(sectionDrawingUnit);//添加到dUs列表

        //其余各节需要依靠前一节的末尾坐标进行自身坐标的计算【仅当本行内（算上下移的那个）的小节数量≥3时才进入循环】
        //（否则，如果==2，则是一个下移，剩余一个独占整行在之前的“首节处理”逻辑中已完成计算，不需进循环。）
        for (int k=2;k<sectionAmountInLine;k++) {
            //计算（剩余在本行的）后续各节。
            int indexBeingCalculate = i-sectionAmountInLine+k;
            int indexBeforeCalculate = i-sectionAmountInLine+k-1;
            //先计算起始X，需要依赖同line中前一节最末音符的右边缘坐标。
            sectionStartX = drawingUnits.get(indexBeforeCalculate).get(drawingUnits.get(indexBeforeCalculate).size()-1).right+beatGap;
            ArrayList<DrawingUnit> sectionDrawingUnit_2 = initSectionDrawingUnit(codesInSections.get(indexBeingCalculate), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted);
            drawingUnits.add(sectionDrawingUnit_2);//添加到dUs列表

        }
    }

    void extraAllInLine(float availableTotalWidth,float lineRequiredLength,int i,int sectionAmountInLine,float topDrawing_Y,int lineCursor){
        float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength)) * unitWidth;
        //计算其他节的绘制数据
        float sectionStartX = padding;

        //先对行内首节进行计算
        ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i-sectionAmountInLine+1), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted);
        //并添加到总记录
        drawingUnits.add(sectionDrawingUnit);//添加到dUs列表

        //其余各节需要依靠前一节的末尾坐标进行自身坐标的计算【仅当本行内（算上下移的那个）的小节数量≥3时才进入循环】
        //（否则，如果==2，则是一个下移，剩余一个独占整行在之前的“首节处理”逻辑中已完成计算，不需进循环。）
        for (int k=2;k<=sectionAmountInLine;k++) { //【注意有等号】此情景下包含最后一节即索引为i的节，与另一方法的循环不同
            //计算（剩余在本行的）后续各节。
            int indexBeingCalculate = i-sectionAmountInLine+k;
            int indexBeforeCalculate = i-sectionAmountInLine+k-1;
            //先计算起始X，需要依赖同line中前一节最末音符的右边缘坐标。
            sectionStartX = drawingUnits.get(indexBeforeCalculate).get(drawingUnits.get(indexBeforeCalculate).size()-1).right+beatGap;
            ArrayList<DrawingUnit> sectionDrawingUnit_2 = initSectionDrawingUnit(codesInSections.get(indexBeingCalculate), topDrawing_Y, lineCursor, sectionStartX, unitWidth_extracted);
            drawingUnits.add(sectionDrawingUnit_2);//添加到dUs列表

        }
    }





    }
