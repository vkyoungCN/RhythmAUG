package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/*
* 对应于数据表直接设计的数据类
* 相对的是“LFR”，后者携带了交叉表中的“所归属的节奏id、是否是该节奏的主要歌词”两额外字段
* */
public class Lyric extends BaseModel {
// 由于汉字占3~4字节，向Byte的转换（再利用byte判断乐句标记等）意义不大（主数据是错误的而且还要转回去）
// 考虑直接使用字串，乐句结束标记使用特殊字符（#）表示；拆分后本类内新设一个Array<String>用于按分句管理。
// （基类中的主存储字段是单个String。）
// 完全使用基类字段和getter、setter。
// 基类已经实现了Parcel接口

    /* 特有字段*/
    //方案①：本字段自动转化生成（采用）；②手动执行转化/同步方法后生成
    //由于自动转换，所以原存取逻辑可以不变；某些特殊场景下使用phrase（RhV中可以换成phr）。
    private ArrayList<String> phrases = new ArrayList<>();

    public Lyric() {
    }

    public Lyric(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
        updatePhrasesByCodeSerialStr();
    }


    //从存储String到二维String
    private void updatePhrasesByCodeSerialStr(){
        String[] phrasesInArray = codeSerialString.split("#");
        if(phrasesInArray.length==0){
            return;
        }
        this.phrases.addAll(Arrays.asList(phrasesInArray));
    }

    //从二维Str同步到一维
    private void updateCodeSerialStrByPhrases(){
        if(this.phrases==null||this.phrases.isEmpty()){
            this.codeSerialString = "";
            return;
        }

        StringBuilder sbd = new StringBuilder();
        for (String phrase:this.phrases) {
            sbd.append(phrase);
            sbd.append("#");
        }
        sbd.deleteCharAt(sbd.length()-1);
        this.codeSerialString = sbd.toString();
    }


    /* 对外提供一套*/
    public static ArrayList<String> toPhrasesByCodeSerialString(String codeSerialString){
        String[] phrasesInArray = codeSerialString.split("#");
        /* ArrayList<String> phrases = new ArrayList<>();
        if(phrasesInArray.length==0){
            return phrases;
        }*/
        return  new ArrayList<>(Arrays.asList(phrasesInArray));
    }

    //从二维Str同步到一维(对外)
    public static String toCodeSerialStringByPhrases(ArrayList<String> phrases){
        if(phrases==null||phrases.isEmpty()){
            return "";
        }

        StringBuilder sbd = new StringBuilder();
        for (String phrase:phrases) {
            sbd.append(phrase);
            sbd.append("#");
        }
        sbd.deleteCharAt(sbd.length()-1);
        return sbd.toString();
    }


    /* 覆写，增加新的行为（同步到乐句List）*/
    public void setCodeSerialString(String codeSerialString) {
        if(codeSerialString==null){//【引用类型需要这样处理一下。（可能多此一举了？）】
            codeSerialString = "";
        }
        this.codeSerialString = codeSerialString;
        updatePhrasesByCodeSerialStr();
    }


    public ArrayList<String> getPhrases() {
        return phrases;
    }

    public void setPhrases(ArrayList<String> phrases) {
        this.phrases = phrases;
        updateCodeSerialStrByPhrases();
    }
}
