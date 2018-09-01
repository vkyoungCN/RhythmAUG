package com.vkyoungcn.learningtools.myrhythm.models;

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
     * */


    /* 数据*/
    ArrayList<Byte> codeSerial = new ArrayList<>();
    int valueOfBeat = 16;

    public CodeSerial_Rhythm() {
    }

    public CodeSerial_Rhythm(ArrayList<Byte> codeSerial, int valueOfBeat) {
        this.codeSerial = codeSerial;
        this.valueOfBeat = valueOfBeat;
    }

    public ArrayList<Byte> getCodeSerial() {
        return codeSerial;
    }

    /* setter方法用于将“从DB获取的编码数据”存入*/
    public void setCodeSerial(ArrayList<Byte> codeSerial) {
        if(serialValidationCheck()) {
            this.codeSerial = codeSerial;
        }
    }

    public int getValueOfBeat() {
        return valueOfBeat;
    }

    public void setValueOfBeat(int valueOfBeat) {
        this.valueOfBeat = valueOfBeat;
    }

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
                checkAndChangeNextWhenZero(index);
                //然后获取本音符原有时值，生成正确的多连音单个编码【备用】
                multiCode = ((currentCode / 4) + 6) * 10 + multiType;

            }

            //原本是延音符，要判断后面是否延音符，是则修改。
            if (currentCode == 0) {
                checkAndChangeNextWhenZero(index);
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


    /* 指定位置上改为“正常”音符*/
    public int replaceCodeWithNormalAt(int index) {
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
                checkAndChangeNextWhenZero(index);
            }


            if(currentCode == 0){
                //延音符改X
                //nC默认值
                emptyCode = -16;

                //后续-检测，修改
                checkAndChangeNextWhenZero(index);
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



    /* 一些辅助方法*/
    //检测当前符号是否位于连音弧覆盖之下
    public boolean checkCurveCovering(int index){
        byte currentCode = codeSerial.get(index);
        int span = 0;
        int curveEndIndex = -1; //初始值采用不可能值
        int curveStartIndex = -1;
        for(int i=index;i<codeSerial.size();i++){
            //从当前（准备修改的目标位置）开始，向后遍历查找连音弧结尾
            if(currentCode>111&&currentCode<126){
                //112~125是连音弧结束标记
                span = currentCode-110;
                curveEndIndex = i;//结尾index是curve结束标记所在位置。
                curveStartIndex = i-span;
                break;//一定不要忘记，找到就终止循环
            }
        }
        return ((curveStartIndex<index)&&(curveEndIndex>index));
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
    public int checkAndChangeNextWhenZero(int currentIndex){
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
             }else if((codeSerial.get(currentIndex+2)==127)&&(codeSerial.get(currentIndex+3)==0)){
                 //再后一个是节尾127，在后一个是延音符【由于简化设计，暂时允许节首（非全列第一时）出现 - 】
                 codeSerial.set(currentIndex+3,(byte)valueOfBeat);//改为一个X
                 return valueOfBeat;
             }
         }
         return 3018;//未知错误，所有条件无一符合
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
                 checkAndChangeNextWhenZero(index);
             } else if (currentCode == 8) {
                 codeDivided = 2;
                 checkAndChangeNextWhenZero(index);
             } else {
                 return 3022;//过小+附点不能拆分两种情况。             }
             }
         }


         if(currentCode == 0){
             //延音
             codeDivided = valueOfBeat/4;

             checkAndChangeNextWhenZero(index);

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
                checkAndChangeNextWhenZero(index);
            } else if (currentCode == 8) {
                codeDivided = 4;
                checkAndChangeNextWhenZero(index);
            } else if(currentCode == 4){
                codeDivided = 2;
//                checkAndChangeNextWhenZero(index);【当前音符是1/16时后续必然不是延音符】
            }else {
                return 3022;//过小+附点不能拆分两种情况。(不可对1/32再做拆分)             }
            }
        }


        if(currentCode == 0){
            //延音
            codeDivided = valueOfBeat/2;

            checkAndChangeNextWhenZero(index);

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
















}














}
