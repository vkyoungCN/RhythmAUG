package com.vkyoungcn.learningtools.myrhythm.helper;

import android.util.Log;

import java.util.ArrayList;

public class RhythmHelper {

    public static final int RHYTHM_TYPE_24 = 24;
    public static final int RHYTHM_TYPE_34 = 34;
    public static final int RHYTHM_TYPE_44 = 44;
    public static final int RHYTHM_TYPE_38 = 38;
    public static final int RHYTHM_TYPE_68 = 68;

    private static final String TAG = "RhythmHelper";
    public static int calculateValueBeat(int rhythmType) {
        //根据节拍形式确定一拍的时值、一节的时值总量。
        int valueOfBeat = 16;
        switch (rhythmType) {
            case RHYTHM_TYPE_38:
                valueOfBeat = 8;
                break;
            case RHYTHM_TYPE_68:
                valueOfBeat = 8;
                break;
        }//其余不需修改

        return valueOfBeat;
    }


    public static int calculateValueSection(int rhythmType) {
        //根据节拍形式确定一拍的时值、一节的时值总量。
        int valueOfSection = 48;
        switch (rhythmType){
            case RHYTHM_TYPE_24:
                valueOfSection = 32;
                break;
            case RHYTHM_TYPE_44:
                valueOfSection = 64;
                break;
            case RHYTHM_TYPE_38:
                valueOfSection = 24;
                break;
        }//其余不需修改

        return valueOfSection;
    }




    public static ArrayList<ArrayList<Byte>> codeParseIntoSections(ArrayList<Byte> rhythmCodes,int rhythmType){
        //将节奏编码序列按小节组织起来【编码增加126、127后不必再依靠累加时值判断】
        ArrayList<ArrayList<Byte>>codesInSections = new ArrayList<>();//初步初始化
        int startIndex = 0;//用于记录上次添加的末尾索引+1，即本节应添加的音符序列的索引起始值。

        for (int i=0; i<rhythmCodes.size();i++){
            int b = (int)(rhythmCodes.get(i));
            if(b==127){
                ArrayList<Byte> codeInSingleSection = new ArrayList<>(rhythmCodes.subList(startIndex,i+1));//装载单节内的音符【注意指定的右侧索引值是不包含的，仅截取到/包括到到其左邻一个位置。】
                codesInSections.add(codeInSingleSection);//添加到按节管理的总编码表
                startIndex = i+1;
            }
        }
        return codesInSections;
    }


   /* public static ArrayList<ArrayList<Byte>> codeParseIntoSections(ArrayList<Byte> rhythmCodes,int rhythmType){
        //将节奏编码序列按小节组织起来
        int valueOfBeat = calculateValueBeat(rhythmType);
        int valueOfSection = calculateValueSection(rhythmType);

        int totalValue=0;
        ArrayList<ArrayList<Byte>>codesInSections = new ArrayList<>();//初步初始化
        int startIndex = 0;//用于记录上次添加的末尾索引+1，即本节应添加的音符序列的索引起始值。

        for (int i=0; i<rhythmCodes.size();i++){
            int b = (int)(rhythmCodes.get(i));
//            Log.i(TAG, "codeParseIntoSections: b="+String.valueOf(b));
            if(b>112){
                //上弧连音专用符号，不记时值(以及小节结尾、拍子结尾……)
                totalValue += 0;
            }else if(b>92){
                totalValue += 4;//三类均分多连音的时值的定值，不随内容数量改变，也与vb无关。
            }else if(b>82){
                totalValue += 8;
            }else if (b>72) {
                //时值计算
                totalValue += 16;
            }else if(b>0) {
                //时值计算
                totalValue+=b;
            }else if(b==0){
                totalValue+=valueOfBeat;
            }else {//b<0
                //时值计算：空拍带时值，时值绝对值与普通音符相同
                totalValue-=b;
            }
            if(totalValue!=0 && totalValue%valueOfSection==0){
                ArrayList<Byte> codeInSingleSection = new ArrayList<>(rhythmCodes.subList(startIndex,i+1));//装载单节内的音符【注意指定的右侧索引值是不包含的，仅截取到/包括到到其左邻一个位置。】
                codesInSections.add(codeInSingleSection);//添加到按节管理的总编码表
                startIndex = i+1;
            }//这样只有满节的小节（包括最后是0、-等特殊情况）才能处理，最后如果出现不满暂定属于编码错误的情形。
            //【注】乐谱的规则上，小节最后音符似乎不能大于小节剩余值，（比如仅剩一个帕子时不能安排大附点），如果这个规则存在，则本逻辑判断是正确的
            //【可在节奏新增的界面中，对输入规则做类似规定】，否则将混乱出错。
        }

//        Log.i(TAG, "codeParseIntoSections: section_1="+codesInSections.get(0).toString()+"type="+rhythmType);
        return codesInSections;
    }*/

    public static float standardLengthOfSection( ArrayList<Byte> codesInSingleSection, float unitStandardWidth ,int valueOfBeat,float beatGap) {
        //是按标准单位宽度计算的本节所需宽度，在与控件宽度比较之后，(可能)会进行压缩或扩展

        float requiredSectionLength = 0;
        int totalValue = 0;//还是需要计算时值的，因为需要在节拍后面增加节拍间隔。

        for (byte b : codesInSingleSection) {
            //大于112的是特殊记号，不绘制为实体单元，不记录长度
            if(b>72&&b<112){
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

    public static ArrayList<Byte> getStandardEmptySection(int rhythmType){
        ArrayList<Byte> emptySection = new ArrayList<>();

        int valueOfBeat = 16;
        int beatAmount = 4;
        switch (rhythmType) {
            case RHYTHM_TYPE_24:
                //此时beat值==16无需修改
                beatAmount = 2;
                break;
            case RHYTHM_TYPE_34:
                beatAmount = 3;
                break;
            case RHYTHM_TYPE_44:
                break;
            case RHYTHM_TYPE_38:
                valueOfBeat = 8;
                beatAmount = 3;
                break;
            case RHYTHM_TYPE_68:
                valueOfBeat = 8;
                beatAmount = 6;
                break;
        }

        for(int i=0;i<beatAmount;i++){
            emptySection.add((byte)valueOfBeat);
            emptySection.add((byte)126);
        }
        emptySection.add((byte)127);

        return emptySection;
    }


    public static String getStrRhythmType(int rhythmType){
        switch (rhythmType) {
            case RHYTHM_TYPE_24:
                return "2/4";
            case RHYTHM_TYPE_34:
                return "3/4";
            case RHYTHM_TYPE_44:
                return "4/4";
            case RHYTHM_TYPE_38:
                return "3/8";
            case RHYTHM_TYPE_68:
                return "6/8";
        }
        return "";
    }

}
