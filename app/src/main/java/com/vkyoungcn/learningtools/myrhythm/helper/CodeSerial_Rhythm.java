package com.vkyoungcn.learningtools.myrhythm.helper;

import com.vkyoungcn.learningtools.myrhythm.models.RhythmHelper;

import java.util.ArrayList;

public class CodeSerial_Rhythm {
    //为编码工作提供规则、校验、功能封装。
    // 改动（单个位置字符替换、添加、删除；连续位置上的字符替换、添加、）提交到本类，由本类负责判断提交是否合法，合法则按规则进行修改，否则拒绝。

    /*
     * 返回值解释:
     * 0，成功
     * 3001;//未能成功。下标越界（小于0）
     * 3002;//未能成功，主数据源空
     * 3003;//未成功，下标越界（大于）（指超出实际音符可占据的最大位置，最后两个是126/127不允许）
     * 3004;//不允许的编码
     *
     * 3009，其他未能预料到的情况。
     *
     * 3011，目标位置位于列首，不能成为延音符
     * 3012，并非独占整拍【调用方DFG】
     * 3013，前一音符并非独占整拍【调用方DFG】
     * 3014，未知错误+尝试对位于列尾的126/127修改。
     * 3015，被修改的位置上是连音弧结束标记，或126/127，不允许修改。
     * 3016，位于连音弧下，（暂时）拒绝修改。
     *
     * 3019，附点音符不允许改为均分多连音
     * 3020，原有时值太小，不允许再做多分
     * 3021，无意义操作，改变前后一致
     * 3022;//过小+附点不能拆分两种情况。
     *
     * 3023:连音弧不允许拆分,延音不许拆分。
     *
     *
     * 3077 一个缺省的错误、未改动等。
     * 3099 序列末尾。
     *
     * */

    private volatile static CodeSerial_Rhythm sCodeSerialRhythm = null;


    /* 数据*/
    static ArrayList<Byte> codeSerial;
    static int beatType = 44;
    static int valueOfBeat = 16;//可以通过bT设置自动生成



    //DCL模式单例获取方法【双检查锁定？】
    public static CodeSerial_Rhythm getInstance(ArrayList<Byte> codeSerial,int beatType,int valueOfBeat){
        if(sCodeSerialRhythm == null){
            synchronized (CodeSerial_Rhythm.class){
                if(sCodeSerialRhythm == null){
                    sCodeSerialRhythm = new CodeSerial_Rhythm(codeSerial,beatType,valueOfBeat);
                }
            }
        }
        return sCodeSerialRhythm;
    }


 /*   public CodeSerial_Rhythm() {
    }

    public CodeSerial_Rhythm(ArrayList<Byte> codeSerial, int beatType) {
        codeSerial = codeSerial;
        beatType = beatType;
        valueOfBeat = RhythmHelper.calculateValueBeat(beatType);
    }*/

    public CodeSerial_Rhythm(ArrayList<Byte> codeSerial, int beatType, int valueOfBeat) {
        CodeSerial_Rhythm.codeSerial = codeSerial;//静态的，直接赋给类。
        CodeSerial_Rhythm.beatType = beatType;
        CodeSerial_Rhythm.valueOfBeat = valueOfBeat;
    }

    public ArrayList<Byte> getCodeSerial() {
        return codeSerial;
    }

    /* setter方法用于将“从DB获取的编码数据”存入*/
    public void setCodeSerial(ArrayList<Byte> codeSerial) {
        if(serialValidationCheck()) {
            codeSerial = codeSerial;
        }
    }

    public int getBeatType() {
        return beatType;
    }

    public void setBeatType(int beatType) {
        this.beatType = beatType;
        this.valueOfBeat = RhythmHelper.calculateValueBeat(beatType);
    }



    /* 业务方法*/

    /* 区域合并*/
    public int mergeArea(int startIndex, int endIndex){
        //先进行各种“不合法”检测
        if(startIndex==endIndex){
            return 3300;//单个符号，没必要“合并”
        }
        if(startIndex>endIndex){
            return 3033;//起止反序。
        }
        for(int i=startIndex;i<=endIndex;i++){
            if(codeSerial.get(i)==126){
                //不允许跨拍合并
                return 3301;//不允许跨拍合并
            }
        }//通过了检测，没有跨拍（选定的是1拍或不足1拍）

        int areaTotalValue = getAreaValue(startIndex,endIndex);

        if(!isValueValidForSingleCode(areaTotalValue)){
            return 3202;//选定区域的值无法以单个符号替换。【逻辑版本v1。后期可能会调整逻辑】
        }else {
            //可以调整
            //原位置的删掉
            for(int k=startIndex;k<=endIndex;k++){
                codeSerial.remove(startIndex);//游标不动，循环数动
            }
            //添加
            codeSerial.add(startIndex,(byte)areaTotalValue);
            return areaTotalValue;
        }
    }

    public int getAreaValue(int startIndex,int endIndex){
        int areaTotalValue = 0;
        for(int i=startIndex;i<=endIndex;i++) {
            areaTotalValue += getCodeValue(codeSerial.get(i),valueOfBeat);
        }
        return areaTotalValue;
    }
    //获取选定编码的值
    private int getCodeValue(byte code,int valueOfBeat){
        if(code>111){
            return 0;
        }
        if(code>73||code==0){
            return valueOfBeat;
        }

        if(code>0){
            return code;
        }else {
            return -code;
        }
    }

    /* 检测选定区域的值是否能替换成单个合法符号*/
    private boolean isValueValidForSingleCode(int value){
        //如果单个X的值（包括X·）符合如下的值，则合法
        return (value==valueOfBeat)||(value==valueOfBeat/2)||(value==valueOfBeat/4)||(value==valueOfBeat/8)|| (value==valueOfBeat+valueOfBeat/2)||(value==valueOfBeat/2+valueOfBeat/4)||(value==valueOfBeat/4+valueOfBeat/8);
        //否则均是false.

    }


    /* 单点替换*/
    public int replaceCodeAt(int index, byte newCode){
        //目标位置和主数据列表检查
       int checkNum = checkIndexAndList(index);
       if(checkNum!=2000){
           return checkNum;
       }//=2000时成功，则继续。

        /* 目标编码单元检查 */

        //暂不允许替换为拍结尾126、节结尾127（那样可能会导致编码结构改变）
        // 也不允许直接替换为连音弧结束标记（只能新增，且要符合一定条件）
        if(newCode>111){
            return 3004;//不允许的编码
        }

        /* 以下，是（在一定条件下）可以替换的情形；
        * 既可以（由外部调用方）直接调用具体的分支方法，
        * 也可以委托给本方法按newCode的分类来自动选择分支方法的调用*/
        if(newCode>73&&newCode<110){
            //如果要替换为均分多连音
            return replaceCodeWithMultiSingleAt(index,newCode%10);
        }

        if(newCode>0){
            //替换为正常音符
            //（有音高状态下，可以同时值下替换音高（其他类的方法处理））
            //在本类情形下，只负责对节奏（时值）进行处理，而一个位置上音符的时值是既定的，
            //所以判断原符号，然乎按情况修改
            return replaceCodeWithNormalAt(index);
        }

        //如果要替换为延音符，要求：①独占整拍，②前一音符需要也是独占整拍
        // 专用方法处理
        if(newCode == 0){
           return replaceCodeWithZeroAt(index);
        }

        //剩余情况只有一种（nC<0），即替换为带时值的空【这里再加if则编译器提示多余】
        return replaceCodeWithEmptyAt(index);//此逻辑设计下，自动根据所框定的单个音符的时值大小转换成对应时值的空拍；
        //如果传入的新值（空拍值）不符合要求，相当于舍弃要求的值，按合理值替代处理。

    }


    //如果要替换为均分多连音，要求：
    // ①原拍子必须是整16分、整8分、整4分之一（即不允许对附点执行Mc替换，不允许对特别小的时值（32分）替换）
    // 并不一定要求独占整拍，也不要求在Mc后方添加间隔（实际记谱原则中似乎是有间隔的，但考虑不做复杂处理）
    //可位于任意实际音符的位置上。
    /*
     * 传入的multiType是均分的类型（3/5/7三种均分类型），
     * 由当前选定位置上音符的原有时值决定新生成的均分多连音的时值大小。
     * */
    public int replaceCodeWithMultiSingleAt(int index, int multiType) {
        byte currentCode = codeSerial.get(index);
        if (currentCode > 111) {
            //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
            return 3015;
        } else {
            //不能位于连音弧下方
            if (checkCurveCovering(index)) {
                return 3016;
            }


            //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
            int multiCode = 103;//备用（稍后根据具体情况生成）

            //原本是正常音符，要判断后面一个是不是延音符，如果是则要修改为X
            if (currentCode > 0 && currentCode < 73) {
                if (currentCode == 24 || currentCode == 12 || currentCode == 6) {
                    return 3019;//附点音符不允许改为均分多连音
                }

                if (currentCode < 4) {
                    return 3020;//原有时值太小，不允许再做多分（小于1/16）
                }

                //不是附点音符，可以继续
                checkAndChangeNextWhenBar(index);
                //然后获取本音符原有时值，生成正确的多连音单个编码【备用】
                multiCode = ((currentCode / 4) + 6) * 10 + multiType;

            }

            //原本是延音符，要判断后面是否延音符，是则修改。
            if (currentCode == 0) {
                checkAndChangeNextWhenBar(index);
                multiCode = ((valueOfBeat / 4) + 6) * 10 + multiType;
            }


            //原本是空拍，生成正确mC，待直接修改
            if (currentCode < 0) {
                if (currentCode > -4) {
                    return 3020;//时值过小，无法多分
                }
                multiCode = -((((currentCode / 4) - 6) * 10) - multiType);
            }

            //原本是均分多连音，如果无变化则无实际动作
            if (currentCode > 72 && currentCode < 110) {
                if (currentCode % 10 == multiType) {
                    return 3021;//无意义操作，改变前后一致
                }
                multiCode = currentCode / 10 + multiType;
            }


            //千辛万苦的判断之后，终于可以修改了
            codeSerial.set(index, (byte) multiCode);
            return multiCode;
        }

    }

    public int changeAreaToDvd(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }
        //不必检测区域时值是否符合，调用方限定1整拍、2整拍，单点单拍三种情形
        //仅检测逻辑
        int areaValue = getAreaValue(startIndex,endIndex);
        ArrayList<Byte> codePiece = new ArrayList<>();
        if(areaValue>valueOfBeat){
            //双拍
            codePiece.add((byte)(valueOfBeat/2));
            codePiece.add((byte)(valueOfBeat/2));
            codePiece.add((byte)126);
            codePiece.add((byte)(valueOfBeat/2));
            codePiece.add((byte)112);
            codePiece.add((byte)(valueOfBeat/2));
//            codePiece.add((byte)126);【因为现行规则中，单拍双拍选区末的126不计入，所以添加时也不必再予增加】
        }else if(areaValue == valueOfBeat){
            //单拍
            codePiece.add((byte)(valueOfBeat/4));
            codePiece.add((byte)(valueOfBeat/2));
            codePiece.add((byte)(valueOfBeat/4));

        }

        //逐个删除
        for(int i=startIndex;i<=endIndex;i++){
            codeSerial.remove(startIndex);
        }

        //添加序列
        codeSerial.addAll(startIndex,codePiece);
        return areaValue;
    }

    public int changeAreaToHaveSpot(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }
        //不必检测区域时值是否符合，调用方限定1整拍、2整拍，单点单拍三种情形
        //仅检测逻辑
        int areaValue = getAreaValue(startIndex,endIndex);
        ArrayList<Byte> codePiece = new ArrayList<>();
        if(areaValue>valueOfBeat){
            //双拍
            codePiece.add((byte)(valueOfBeat/2+valueOfBeat));
            codePiece.add((byte)126);
            codePiece.add((byte)(valueOfBeat/2));
//            codePiece.add((byte)126);【因为现行规则中，单拍双拍选区末的126不计入，所以添加时也不必再予增加】
        }else if(areaValue == valueOfBeat){
            //单拍
            codePiece.add((byte)(valueOfBeat/4+valueOfBeat/2));
            codePiece.add((byte)(valueOfBeat/4));
        }

        //逐个删除
        for(int i=startIndex;i<=endIndex;i++){
            codeSerial.remove(startIndex);
        }

        //添加序列
        codeSerial.addAll(startIndex,codePiece);
        return areaValue;
    }


    public int changeAreaToRwd16(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }
        //不必检测区域时值是否符合，调用方限定1整拍、单点单拍情形
        //仅检测逻辑
        ArrayList<Byte> codePiece = new ArrayList<>();
            codePiece.add((byte)(valueOfBeat/2));
            codePiece.add((byte)(valueOfBeat/4));
            codePiece.add((byte)(valueOfBeat/4));

        //逐个删除
        for(int i=startIndex;i<=endIndex;i++){
            codeSerial.remove(startIndex);
        }

        //添加序列
        codeSerial.addAll(startIndex,codePiece);
        return valueOfBeat;
    }

    public int changeAreaToFwd16(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }
        //不必检测区域时值是否符合，调用方限定1整拍、单点单拍情形
        //仅检测逻辑
        ArrayList<Byte> codePiece = new ArrayList<>();
        codePiece.add((byte)(valueOfBeat/4));
        codePiece.add((byte)(valueOfBeat/4));
        codePiece.add((byte)(valueOfBeat/2));

        //逐个删除
        for(int i=startIndex;i<=endIndex;i++){
            codeSerial.remove(startIndex);
        }

        //添加序列
        codeSerial.addAll(startIndex,codePiece);
        return valueOfBeat;

    }


    public int changeAreaToBar(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }
        //不必检测区域时值是否符合，调用方限定1整拍、单点单拍情形
        //逐个删除
        for(int i=startIndex;i<=endIndex;i++){
            codeSerial.remove(startIndex);
        }

        //添加
        codeSerial.add(startIndex,(byte)0);
        return 0;

    }




    public int replaceAreaToMultiDivided(int startIndex,int endIndex){
        if(checkAreaUnderCurve(startIndex,endIndex)){
            return 3077;//被连音弧覆盖，退出。
        }

        int areaValue = 0;
        areaValue = getAreaValue(startIndex,endIndex);
        if(areaValue == valueOfBeat ||areaValue==valueOfBeat/2||areaValue==valueOfBeat/4){
            //几种可以替换的情形
            int multiCode = (areaValue/4+6)*10+3;

            //逐个删除
            for(int i=startIndex;i<=endIndex;i++){
                codeSerial.remove(startIndex);
            }

            //添加一个均分多连音
            codeSerial.add(startIndex,(byte)multiCode);
        }
        return areaValue;
    }

    public boolean checkAreaUnderCurve(int startIndex,int endIndex){
        //检测内部
        for(int i=startIndex;i<=endIndex;i++){
            if(codeSerial.get(i)>111&&codeSerial.get(i)<126){
                return true;
            }
        }

        //检测后方
        return checkCurveCovering(endIndex);

    }




    //如果要替换为延音符，要求：①独占整拍(不能位于序列的首位，前后紧邻的应该是拍尾（前方紧邻节尾也合理）)，
    // ②前一音符需要也是独占整拍
    // ③不能位于连音弧内（这条规则1是出于简化实现；2是“允许在连音弧内放置-某些情形下产生错误的记谱”且
    // “不允许在连音弧下放置-并不会产生实质性的不良影响”，因而决定如此）
    public int replaceCodeWithZeroAt(int index){
        if(index==0){
            return 3011;//目标位置位于列首，不能成为延音符
        }else if(index<codeSerial.size()-2){//【实际上由于最后两符号（按规则应当）是126/127,所以这里应该-2】
            //（非列首，非最后）这个范围内可以如下检测
            //*注：暂定允许-位于小节的首位（只要不是整列首位）
            // （实际中，节首-多记为常规音符然后用连音弧和之前的正常音符连起来，
            // 在此考虑到实现的复杂性，决定允许以简化方式——节首-来实现）
            if(checkOccupyingFullBeat(index)){
                //条件①满足，准备检测条件②。
                int lastRealCodeIndex = index-2;//如果未经下方if改写，也即-1是126，-2就是realCode
                if(codeSerial.get(index-1)==127){
                    lastRealCodeIndex = index-3;//-1是127，则-2是126，-3才是RealCode
                }
               if(checkOccupyingFullBeat(lastRealCodeIndex)){
                    //条件②满足，前一音符也是独占整拍

                   //最后一项条件：不允许位于连音弧内
                   if(checkCurveCovering(index)){
                       return 3016;//位于连音弧下，按【当前原则】不允许直接修改请先删除连音弧（后期可能做更精细的改进）
                   }

                codeSerial.set(index,(byte)0);//修改编码
                return 0;
               }else {
                    //前一音符并非独占整拍（条件②失败）
                   return 3013;//【应在调用方给出提示：前一音符并非独占整拍，请先合并前音符，或者改为将本音符置为独立音符的方案】
               }
            }else {
                //并非独占整拍（条件①失败）
                return 3012;//【计划在调用方弹出DFG，用户同意的话可以通过另一方法强制替换本拍子的全部内容为一个 - 】
            }
        }//否则，目标位置位于序列的最后两个位置（这里应该是126、127，【原则上不能被选定】。从略）
        return 3014;//未知错误（可能是选定了对126/127进行修改）

    }


    /*
    * 强制将当前蓝框所在的拍子整体变为一个延音符（用于蓝框所圈定的只是当前拍子的一小部分
    * 而用户决意要将所做的整拍改作-（另外-符号必须独占一拍））
    */
    public boolean forceReplaceCodeWithZero(int index,int forceNumber){
        if(forceNumber!=3012){
            return false;
            //必须传入3012（之前判定当前音符不独占拍子的结果码）以确保是强制替换（主要避免手误）
        }

        //寻找拍首索引值
        int byteStartIndex = 0;
        for(int i=index;i>0;i--){
            if(codeSerial.get(i)==126||codeSerial.get(i)==127){
                byteStartIndex = i;
                break;
            }
        }//若没找到，已到头则拍首实际=0；

        //寻找pai尾
        int byteEndIndex = codeSerial.size()-2;
        for(int i=index;i<codeSerial.size()-2;i++){
            if(codeSerial.get(i)==126){
                byteEndIndex = i;
                break;
            }
        }//若没找到，拍尾就是size-3(-2是126符号，按List风格，右端采用跨+1方式标识)

        for(int k = byteStartIndex;k<byteEndIndex;k++) {
            codeSerial.remove(byteStartIndex);//【在指定索引不变的情况下不知是否能正确执行】
            //多次删除同一位置
        }
        //最后在该位置添加一个0
        codeSerial.add(byteStartIndex,(byte)0);
        return true;
    }


    /* 指定位置上改为“正常”音符【旧版】*/
    public int replaceCodeWithNormalAt(int index) {
        //主要是判断原音符的值
        byte currentCode = codeSerial.get(index);
        if (currentCode > 111) {
            //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
            return 3015;
        }
        //不能位于连音弧下方
        if (checkCurveCovering(index)) {
            return 3016;
        }

            //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
            int normalCode = valueOfBeat;//备用（稍后根据具体情况生成）

            //原来是均分多连音，修改成同时值的正常音符
            if(currentCode>103&&currentCode<110){
                normalCode = 16;
            }else if(normalCode>83){
                normalCode = 8;
            }else if(normalCode>73){
                normalCode = 4;
            }

            //（说明）在修改为多连音时，由于均分多连音不能作为延音的首位音符，故而其后如果是延音符则要
            // 预先做出修改；（其中在当前音符是延音符、正常非附点音符时存在这种可能）。但是在修改为正常音符的情况下
            // 由于正常音符（非附点）本身可以作为延音的首位音符，同时如果原本后面有-则本符必然不带附点。
            // 综上可免于检测处理。

            //原本是正常音符，不予修改（如果新旧时值一致则无意义；如果不一致根本不能改）
            if (currentCode > 0 && currentCode < 73) {
                return 3021;//修改前后一致，无意义。

            }


            if(currentCode == 0){
                //延音符改X
                //nC默认值
                normalCode = 16;
            }

            if(currentCode<0){
                //空拍，取反为同时值的正常音符
                normalCode = -currentCode;
            }

            //修改
            codeSerial.set(index,(byte)normalCode);
            return normalCode;
    }

    /* fg中直接调用的新版方法，基于旧版改造*/
    public int changeCodeToXAt(int index) {
        //主要是判断原音符的值
        byte currentCode = codeSerial.get(index);

        //不能位于连音弧下方
        if (checkCurveCovering(index)) {
            return 3077;
        }

        //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
        int normalCode = valueOfBeat;//备用（稍后根据具体情况生成）

        //原来是均分多连音，修改成同时值的正常音符
        if(currentCode>103&&currentCode<110){
            normalCode = 16;
        }else if(normalCode>83){
            normalCode = 8;
        }else if(normalCode>73){
            normalCode = 4;
        }

        //（说明）在修改为多连音时，由于均分多连音不能作为延音的首位音符，故而其后如果是延音符则要
        // 预先做出修改；（其中在当前音符是延音符、正常非附点音符时存在这种可能）。但是在修改为正常音符的情况下
        // 由于正常音符（非附点）本身可以作为延音的首位音符，同时如果原本后面有-则本符必然不带附点。
        // 综上可免于检测处理。

        //原本是正常音符，不予修改（如果新旧时值一致则无意义；如果不一致根本不能改）
        if (currentCode > 0 && currentCode < 25) {
            return 3077;//修改前后一致，无意义。

        }


        if(currentCode == 0){
            //延音符改X
            //nC默认值
            normalCode = valueOfBeat;
        }

        if(currentCode<0){
            //空拍，取反为同时值的正常音符
            normalCode = -currentCode;
        }

        //修改
        codeSerial.set(index,(byte)normalCode);
        return normalCode;
    }

    /* 指定位置上改为空拍（和原来等值（如果可以的话））*/
    public int replaceCodeWithEmptyAt(int index) {
        //主要是判断原音符的值
        byte currentCode = codeSerial.get(index);
        if (currentCode > 111) {
            //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
            return 3015;
        } else {
            //不能位于连音弧下方
            if (checkCurveCovering(index)) {
                return 3016;
            }

            //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
            int emptyCode = -valueOfBeat;//备用（稍后根据具体情况生成）

            //原来是均分多连音，修改成同时值的空拍
            if(currentCode>103&&currentCode<110){
                emptyCode = -16;
            }else if(emptyCode>83){
                emptyCode = -8;
            }else if(emptyCode>73){
                emptyCode = -4;
            }

            //（说明）在修改为多连音时，由于均分多连音不能作为延音的首位音符，故而其后如果是延音符则要
            // 预先做出修改；（其中在当前音符是延音符、正常非附点音符时存在这种可能）。但是在修改为正常音符的情况下
            // 由于正常音符（非附点）本身可以作为延音的首位音符，同时如果原本后面有-则本符必然不带附点。
            // 综上可免于检测处理。

            //原本是正常音符，不予修改（如果新旧时值一致则无意义；如果不一致根本不能改）
            if (currentCode > 0 && currentCode < 73) {
                emptyCode = -currentCode;//改为同时值空拍

                //原本后续如果是-，要修改（0不能后接-）
                checkAndChangeNextWhenBar(index);
            }


            if(currentCode == 0){
                //延音符改X
                //nC默认值
                emptyCode = -16;

                //后续-检测，修改
                checkAndChangeNextWhenBar(index);
            }

            if(currentCode<0){
                //空拍，无动作
                return 3021;//无意义。
            }

            //修改
            codeSerial.set(index,(byte)emptyCode);
            return emptyCode;
        }
    }

    public int changeCodeToZeroAt(int index) {
        //主要是判断原音符的值
        byte currentCode = codeSerial.get(index);
            //不能位于连音弧下方
            if (checkCurveCovering(index)) {
                return 3077;
            }

            //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
            int emptyCode = -valueOfBeat;//备用（稍后根据具体情况生成）

            //原来是均分多连音，修改成同时值的空拍
            if(currentCode>103&&currentCode<110){
                emptyCode = -16;
            }else if(emptyCode>83){
                emptyCode = -8;
            }else if(emptyCode>73){
                emptyCode = -4;
            }

            //（说明）在修改为多连音时，由于均分多连音不能作为延音的首位音符，故而其后如果是延音符则要
            // 预先做出修改；（其中在当前音符是延音符、正常非附点音符时存在这种可能）。但是在修改为正常音符的情况下
            // 由于正常音符（非附点）本身可以作为延音的首位音符，同时如果原本后面有-则本符必然不带附点。
            // 综上可免于检测处理。

            //原本是正常音符
            if (currentCode > 0 && currentCode < 25) {
                emptyCode = -currentCode;//改为同时值空拍

                //原本后续如果是-，要修改（0不能后接-）
                checkAndChangeNextWhenBar(index);
            }


            if(currentCode == 0){
                //延音符改X
                //nC默认值
                emptyCode = -16;

                //后续-检测，修改
                checkAndChangeNextWhenBar(index);
            }

            if(currentCode<0){
                //空拍，无动作
                return 3077;//无意义。
            }

            //修改
            codeSerial.set(index,(byte)emptyCode);
            return emptyCode;
    }


    /* 辅助方法*/
    //检测当前符号是否位于连音弧覆盖之下
    public boolean checkCurveCovering(int index){
        int span = 0;//【这个span是codeSerial的跨度，126等也要计入其内！】
        int curveEndIndex = -1; //初始值采用不可能值
        int curveStartIndex = -1;
        for(int i=index;i<codeSerial.size();i++){
            //从当前（准备修改的目标位置）开始，向后遍历查找连音弧结尾
            byte currentCode = codeSerial.get(i);
            if(currentCode>111&&currentCode<126){
                //112~125是连音弧结束标记
                span = currentCode-110;
                curveEndIndex = i;//结尾index是curve结束标记所在位置。
                curveStartIndex = i-span+1;

                return ((curveStartIndex<=index)&&(curveEndIndex>index));
                //前提是不允许多层连音弧。
            }
        }
        return false;
    }

    public int addCurveForArea(int startIndex,int endIndex){
        int span = endIndex-startIndex+1;
        byte curveCode = (byte)(110+span);
        if(curveCode>125){
            return 3077;
        }
        codeSerial.add(endIndex+1,curveCode);//根据要求，弧结束标记要紧邻音符（如果有126/127，则在126/127前）
        return curveCode;
    }

    public int removeCurveOver(int index){
        int span = 0;//【这个span是codeSerial的跨度，126等也要计入其内！】
        int curveEndIndex = -1; //初始值采用不可能值
        int curveStartIndex = -1;
        for(int i=index;i<codeSerial.size();i++){
            //从当前（准备修改的目标位置）开始，向后遍历查找连音弧结尾
            byte currentCode = codeSerial.get(i);
            if(currentCode>111&&currentCode<126){
                //112~125是连音弧结束标记
                span = currentCode-110;
                curveEndIndex = i;//结尾index是curve结束标记所在位置。
                curveStartIndex = i-span+1;

                if ((curveStartIndex<=index)&&(curveEndIndex>index)){
                    //上方有弧
                    codeSerial.remove(curveEndIndex);//移除
                    return curveEndIndex;
                }
            }
        }
        return 3077;
    }


    /* 检测指定位置上的代码是否独占整拍*/
    public boolean checkOccupyingFullBeat(int index){
        //后面必须是126，前面可以是126或127
        byte last = codeSerial.get(index-1);
        byte next = codeSerial.get(index+1);
        return (last==126||last==127)&&(next==126);

        //前一音符是拍尾、节尾。（即本符位于新拍拍头或新节节头）
        // 同时后一音符是拍尾（其他规则要求了即使位于节尾部，在127之前也必须是有一个126的）
        // 这种条件下本音符独占整拍，第一条件达成

        }


    /* 检查后续的实际音符是否为延音符，如果是，修改为X【而后应由外部调用方将其重新设置为实际音高】
     * 应当在调用方给出说明文字（毕竟做了可能多于用户预期的操作）
      * 此方法多用在“将原本能够引导-的音符（X,-）修改为不能引导-的音符时，需要检测其后原本是否跟随有-”
      * */
    public int checkAndChangeNextWhenBar(int currentIndex){
        int nextRealIndex = -1;
        for(int i=currentIndex+1;i<codeSerial.size();i++){
            if(codeSerial.get(i)<110){
                nextRealIndex = i;
                break;
            }
        }
        if(codeSerial.get(nextRealIndex)==0){
            codeSerial.set(nextRealIndex,(byte)valueOfBeat);//改为一个X
            return valueOfBeat;
        }
         /*【原方案太复杂。】
         byte next_1 = codeSerial.get(currentIndex+1);
         if(next_1 < 111 ){
             return 3017;//是其他实际音符
         }else if(next_1<126){
             return 3015;//连音弧结束标记（其实在调用前已经被其他分支排除了该可能）
         }else if(next_1==126){
             //后续编码位要求必须为126，然后分两种情况
             if((codeSerial.get(currentIndex+2)==0)){
                 //再后一个是0，延音符，符合
                 codeSerial.set(currentIndex+2,(byte)valueOfBeat);//改为一个X
                 return valueOfBeat;
             }else if((codeSerial.get(currentIndex+2)==127)){
                 if(currentIndex+2==codeSerial.size()-1){
                     //已到末尾
                     return 3099;
                 }else if(codeSerial.get(currentIndex+3)==0){
                         //节尾127后是延音符【由于简化设计，暂时允许节首（非全列第一时）出现 - 】
                         codeSerial.set(currentIndex + 3, (byte) valueOfBeat);//改为一个X
                         return valueOfBeat;
                     }
                 }

         }*/
         return 3018;//未知错误，所有条件无一符合
     }

    public boolean checkLeftRealIsXOrBar(int currentIndex){
        int leftRealIndex = -1;
        for(int i=currentIndex;i>=0;i--){
            if(codeSerial.get(i)<110){
                leftRealIndex = i;
                break;
            }
        }
        if(leftRealIndex == -1){
            return false;
        }
        byte leftReal = codeSerial.get(leftRealIndex);
        boolean resultBool = false;
        if(leftReal==0||leftReal==valueOfBeat){
            resultBool = true;
        }
        return resultBool;
    }


    private int  checkIndexAndList(int index){
        //目标位置和主数据列表检查
        if(index<0){
            return 3001;//未能成功。下标越界（小于0）
        }
        if(codeSerial.isEmpty()){
            return 3002;//未能成功，主数据源空
        }
        if(index>=codeSerial.size()-2){
            return 3003;//未成功，下标越界（大于）（指超出实际音符可占据的最大位置，最后两个是126/127不允许）
        }
        else return 2000;
    }



    /* 单点拆分*/

    /* 四等分某音符，原音符可以是（空拍、延音符、正常非附点且大于等于8分（值8），均分多连音（大于等于8分，要用户确认））
    * 其中原符是正常、-时需要判定后续是否-。
    * */
    public int quarterDividingAt(int index){
         //目标位置和主数据列表检查
         int checkNum = checkIndexAndList(index);
         if(checkNum!=2000){
             return checkNum;
         }//=2000时成功，则继续。

         byte currentCode = codeSerial.get(index);

         if (currentCode > 111) {
             //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
             return 3015;
         }
         //不能位于连音弧下方
         if (checkCurveCovering(index)) {
             return 3016;
         }


        //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
         int codeDivided = valueOfBeat/4;//备用（稍后根据具体情况生成）

         //原来是均分多连音，检测其时值，合适则可修改
         if(currentCode>103&&currentCode<110){
             codeDivided = 4;//原本是个1/4（值16），准备拆分为4个1/16（4个值4的音符）
         }else if(codeDivided>83){
             codeDivided = 2;
         }else if(codeDivided>73){
             return 3020;//过小
         }

         //（说明）在拆分时，拆分后的音符也不能作为延音的首位音符，故而其后如果是延音符也要处理。
         //原本是正常音符，判定时值
         // 修改后续-。
         if (currentCode > 0 && currentCode < 73) {
             if (currentCode == 16) {
                 codeDivided = 4;
                 checkAndChangeNextWhenBar(index);
             } else if (currentCode == 8) {
                 codeDivided = 2;
                 checkAndChangeNextWhenBar(index);
             } else {
                 return 3022;//过小+附点不能拆分两种情况。             }
             }
         }


         if(currentCode == 0){
             //延音
             codeDivided = valueOfBeat/4;

             checkAndChangeNextWhenBar(index);

         }

         if(currentCode<0){
             //空拍，【要拆成4个空拍（用于进一步将细分的空拍改为正常或其他多种操作）】
             codeDivided = currentCode/4;//仍然是空拍
         }

         //修改
         codeSerial.set(index,(byte)codeDivided);

         //添加3次就不循环了吧……
         codeSerial.add(index,(byte)codeDivided);
         codeSerial.add(index,(byte)codeDivided);
         codeSerial.add(index,(byte)codeDivided);

         return codeDivided;
     }

    /* 即使选定的位置上是均分多连音、延音符也能修改*/
    public int forceBinaryDividingAt(int index){
        //目标位置和主数据列表检查
        int checkNum = checkIndexAndList(index);
        if(checkNum!=2000){
            return checkNum;
        }//=2000时成功，则继续。

        byte currentCode = codeSerial.get(index);

        if (currentCode > 111) {
            //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
            return 3015;
        }
        //不能位于连音弧下方
        if (checkCurveCovering(index)) {
            return 3016;
        }

        //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
        int codeDivided = valueOfBeat/2;//备用（稍后根据具体情况生成）

        //原来是均分多连音，检测其时值，合适则可修改
        if(currentCode>103&&currentCode<110){
            codeDivided = 8;//原本是个1/4（值16），准备拆分为2个1/8
        }else if(codeDivided>83){
            codeDivided = 4;
        }else if(codeDivided>73){
            codeDivided = 2;
        }

        //（说明）在拆分时，拆分后的音符也不能作为延音的首位音符，故而其后如果是延音符也要处理。
        //原本是正常音符，判定时值
        // 修改后续-。
        if (currentCode > 0 && currentCode < 73) {
            if (currentCode == 16) {
                codeDivided = 8;
                checkAndChangeNextWhenBar(index);
            } else if (currentCode == 8) {
                codeDivided = 4;
                checkAndChangeNextWhenBar(index);
            } else if(currentCode == 4){
                codeDivided = 2;
//                checkAndChangeNextWhenBar(index);【当前音符是1/16时后续必然不是延音符】
            }else {
                return 3022;//过小+附点不能拆分两种情况。(不可对1/32再做拆分)             }
            }
        }


        if(currentCode == 0){
            //延音
            codeDivided = valueOfBeat/2;

            checkAndChangeNextWhenBar(index);

        }

        if(currentCode<0){
            //空拍，【要拆成2个空拍（用于进一步将细分的空拍改为正常或其他多种操作）】
            codeDivided = currentCode/2;//仍然是空拍
        }

        //修改
        codeSerial.set(index,(byte)codeDivided);
        //再添加1次
        codeSerial.add(index,(byte)codeDivided);

        return codeDivided;
    }

    /* 只有在选定处是音符、空拍两种情形下可以修改，且不能位于连音弧下*/
    public int binaryDividingAt(int index){
        //目标位置和主数据列表检查
        int checkNum = checkIndexAndList(index);
        if(checkNum!=2000){
            return checkNum;
        }//=2000时成功，则继续。

        byte currentCode = codeSerial.get(index);

        if (currentCode > 111) {
            //如果要修改的位置原本是112+(连音弧结束标记),126，127，则不允许修改
            return 3015;
        }
        //不能位于连音弧下方
        if (checkCurveCovering(index)) {
            return 3016;
        }

        //均分多连音
        if(currentCode>73&&currentCode<110){
            return 3023;
        }

        if(currentCode == 0){
            //延音
            return 3023;
        }

        //以下几种情况，都可以对音符做出修改，但是要分别做一些其他操作
        int codeDivided = valueOfBeat/2;//备用（稍后根据具体情况生成）


        //（说明）在拆分时，拆分后的音符也不能作为延音的首位音符，故而其后如果是延音符也要处理。
        //原本是正常音符，判定时值
        // 修改后续-。
        if (currentCode > 0 && currentCode < 25) {
            if (currentCode == 16) {
                codeDivided = 8;
                checkAndChangeNextWhenBar(index);
            } else if (currentCode == 8) {
                codeDivided = 4;
                checkAndChangeNextWhenBar(index);
            } else if(currentCode == 4){
                codeDivided = 2;
//                checkAndChangeNextWhenBar(index);【当前音符是1/16时后续必然不是延音符】
            }else {
                return 3022;//过小+附点不能拆分两种情况。(不可对1/32再做拆分)             }
            }
        }


        if(currentCode<0){
            //空拍，【要拆成2个空拍（用于进一步将细分的空拍改为正常或其他多种操作）】
            codeDivided = currentCode/2;//仍然是空拍
        }

        //修改
        codeSerial.set(index,(byte)codeDivided);
        //再添加1次
        codeSerial.add(index,(byte)codeDivided);

        return codeDivided;
    }


    /* 添加 */
    /* 在最后添加一个空拍小节*/
    public boolean addSection(){
        return codeSerial.addAll(generateEmptySection());//成功返回true
    }

    /* 按当前的节拍类型生成一个空拍小节编码*/
    private ArrayList<Byte> generateEmptySection(){
        int beatAmount = beatType/10;
        ArrayList<Byte> newSection = new ArrayList<>(beatAmount*2+1);
        for(int i=0;i<beatAmount;i++){
            newSection.add((byte)(-valueOfBeat));
            newSection.add((byte)126);
        }
        newSection.add((byte)127);
        return newSection;
    }

    /*
    * 在指定的位置添加一个新的空拍小节
    * 传入的参数代表“全序列中的第N个小节”，1代表在开头插入，若传入0则代表从最后附加。
    * */
    public boolean addSection(int sectionOrderNumber){
        if(sectionOrderNumber == 1){
            //在开头插入
            return codeSerial.addAll(0,generateEmptySection());//【index参数没问题，顺序待：doc指，iterator顺序】
        }else if(sectionOrderNumber == 0){
            //末尾追加
            return codeSerial.addAll(generateEmptySection());
        }

        //不在开头,不在（手动指定的）末尾
        int sectionAmount =1;//默认（初始）即有1节
        int totalCodeIndex = -1;
        for(byte b:codeSerial){
            totalCodeIndex++;//进循环即开始累加（这样第一个b位置是0）
            if(b==127){
                sectionAmount++;
            }
            if(sectionAmount == sectionOrderNumber){
                return codeSerial.addAll(totalCodeIndex+1,generateEmptySection());//注意要在该索引位的后面添加（因为该位置是127）
            }
        }

        //能到此说明循环跑完了
        return false;//指定的位置不对，可能超过了小节数量。【另一种方法是强行附加在最后，待】
    }




























}
