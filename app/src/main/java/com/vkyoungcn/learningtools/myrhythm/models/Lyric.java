package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;


/*
* 对应于数据表直接设计的数据类
* 相对的是“LFR”，后者携带了交叉表中的“所归属的节奏id、是否是该节奏的主要歌词”两额外字段
* */
public class Lyric extends BaseModel {
//可用完全使用基类字段和getter、setter。
// 基类已经实现了Parcel接口

    public Lyric() {
    }

    public Lyric(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
    }

}
