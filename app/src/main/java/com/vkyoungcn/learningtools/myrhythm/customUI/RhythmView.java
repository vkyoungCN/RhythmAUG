package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;


public class RhythmView extends BaseRhythmView {
//* 显示节奏序列
//* 折行模式；字符及字宽采用默认值，可以调节
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。

//* 三种工作模式：“节奏/旋律/有词”
//* 其中有词模式下需要绘制上方连音弧线；旋律模式下需要绘制上下方的加点。

    private static final String TAG = "RhythmView";
    float longestLineWidth = 0;

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
    /*public void setRhythmViewData(RhythmBasedCompound rhythmBasedCompound) {
        super.setRhythmViewData(rhythmBasedCompound,16,20,20);
    }*/


    void initDrawingUnits_step1() {
        //本控件采用折行模式。
        super.initDrawingUnits_step1();//完成部分初始化任务

        initDrawingUnits_step1_rear();//独立设置的原因是便于其子类能不调用基类通用step1的情况下直接调用后续rear。

    }

    void initDrawingUnits_step1_rear(){
        lineCursor = 0;//重置
        ArrayList<Integer> indexesOfThisLine = new ArrayList<>();

        //开始计算绘制信息。以小节为单位进行（根据折行规则具体实现）。
        for (int i = 0; i < codesInSections.size(); i++) {
            //先获取当前小节的长度
            float sectionRequiredLength = standardLengthOfSection(codesInSections.get(i));

            //记录到本行所需总长度
            lineRequiredLength += sectionRequiredLength;
            //本行既有小节数量+1
            sectionAmountInLine++;
            indexesOfThisLine.add(i);
            //针对本小节的宽度/本行既有小节总宽度 与 本行的屏幕宽度进行比较判断。
            /* 折行逻辑（核心算法1）*/
            if (sectionRequiredLength > availableTotalWidth) {
                //单节宽度大于屏幕宽度
                // 如果本行已有其他节则需要压缩本节、扩展其他节；且本节下移
                // 否则（本行只有本节自己）压缩本节，下一节新起一行（行宽计数重置）
                if (sectionAmountInLine == 1) {
//                    Log.i(TAG, "initDrawingUnits_step1: 1 Ex");
                    //①本行只有本节自己，对本节的单位宽度压缩（不需下移）【暂定只压缩宽度；而高度、字号可以一定程度上保持不变】
                    float unitWidthSingleZipped = (availableTotalWidth / sectionRequiredLength) * unitWidth;//与外部使用的uW变量同名
                    //【按调整后的宽度计算本行本节的绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidthSingleZipped );

                    drawingUnits.add(sectionDrawingUnit);//添加到dUs列表
//                    accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();
                    longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidthSingleZipped));
                    //②行需求宽度计数器重置，③本行节索引重置
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;
                    indexesOfThisLine.clear();

                    lineCursor++;//行计数器+1，以备下节正确使用
                } else {
//                    Log.i(TAG, "initDrawingUnits_step1: 1 Ex, line Ex");

                    //本行超过1节（除本节外还有其他节），其他节扩展
                    float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength - sectionRequiredLength)) * unitWidth;
                    extraOthersInLine(availableTotalWidth,lineRequiredLength,sectionRequiredLength,i,sectionAmountInLine,topDrawing_Y,lineCursor,unitWidth_extracted);

                    indexesOfThisLine.remove((Integer)i);
                    longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidth_extracted));
                    indexesOfThisLine.clear();
                    indexesOfThisLine.add(i);

                    //本节计算：本节下移,独占一行，且压缩本节
                    lineCursor++; //行计数器+1（刚才未++前，是原“本行”其他节的高度计算；现在是新行计算）
                    float unitWidth_zipped = (availableTotalWidth / sectionRequiredLength) * unitWidth;
                    //【在此计算本节绘制数据】
                    ArrayList<DrawingUnit> sectionDrawingUnit_3 = initSectionDrawingUnit(codesInSections.get(i), topDrawing_Y, lineCursor, padding, unitWidth_zipped);

                    longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidth_zipped));

                    drawingUnits.add(sectionDrawingUnit_3);//添加到dUs列表

                    //行需求宽度计数器重置(以备下行使用)，本行节索引重置（以备下行使用）
                    //重置前判断是否要更新最大值计数器【无意义，数据已改变】
//                    longestLineWidth = Math.max(longestLineWidth,lineRequiredLength-sectionRequiredLength);
                    lineRequiredLength = 0;
                    sectionAmountInLine=0;
                    indexesOfThisLine.clear();

                    //计算完成后，行计数器要再加1以备下行使用
                    lineCursor++;

                }
            } else if (lineRequiredLength > availableTotalWidth) {
//                Log.i(TAG, "initDrawingUnits_step1: 1 notEx, line Ex");

                //单节宽度不大于控件可用宽度，但是行累加宽度超过了；
                // 本行其他节要放大
                float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength - sectionRequiredLength)) * unitWidth;
                extraOthersInLine(availableTotalWidth,lineRequiredLength,sectionRequiredLength,i,sectionAmountInLine,topDrawing_Y,lineCursor,unitWidth_extracted);

                indexesOfThisLine.remove((Integer)i);
                longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidth_extracted));
                indexesOfThisLine.clear();
                indexesOfThisLine.add(i);

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
                    longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidth_singleExtracted));
                    indexesOfThisLine.clear();
                    drawingUnits.add(sectionDrawingUnit_3);
                }

//                accumulateSizeBeforeThisSection+=drawingUnits.get(i).size();

            } else {
//                Log.i(TAG, "initDrawingUnits_step1: 1 notEx, line notEx");
                //单节宽度不超控件允许宽度、本行总宽也未超总宽。
                // 本节索引已在开头自动加入行索引列表，因而不需在此再次操作。
                //在此，需判断本节是否是最后一节，如果是，则将本行现有所有节进行整体扩展，以占满全行宽度
                if(i == codesInSections.size()-1) {
                    float unitWidth_extracted = (availableTotalWidth / (lineRequiredLength)) * unitWidth;
                    extraAllInLine(availableTotalWidth, lineRequiredLength, i, sectionAmountInLine, topDrawing_Y, lineCursor,unitWidth_extracted);
                    longestLineWidth = Math.max(longestLineWidth,finalLengthOfSections(indexesOfThisLine,unitWidth_extracted));
                    indexesOfThisLine.clear();
                }
                //此分支下仅当到达尾节时才进行计算（本行绘制数据的计算）【否则在后面的节超屏宽时会再次触发对本节的计算，重复负荷】
                //其余情况下只是增加本行的小节数量记录即可，不必有其他处理。
            }
        }
        Log.i(TAG, "initDrawingUnits_step1_rear: longest line ="+longestLineWidth);

        //【这里应根据所需总高对各行进行平移】
        //【如果不处理则默认是顶部对齐的绘制方式】
        doShiftVertically();
    }

    void doShiftVertically(){
        float totalHeightNeeded = (lineCursor+1)* twoLinesTopYBetween;
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
    void extraOthersInLine(float availableTotalWidth,float lineRequiredLength,float sectionRequiredLength,int i,int sectionAmountInLine,float topDrawing_Y,int lineCursor,float unitWidth_extracted){
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

    void extraAllInLine(float availableTotalWidth,float lineRequiredLength,int i,int sectionAmountInLine,float topDrawing_Y,int lineCursor,float unitWidth_extracted ){
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
