package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

public class Group extends BaseModel {
//    基本采用基类字段、逻辑即可；
// 但本类实际上不需要那么多字段，其中如codeSerial字段不需要有setter、getter等


    public Group() {
    }

    public Group(int id, String title,  String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, "",description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
    }


    /* 覆写以下字段的方法，禁用该字段*/
    @Override
    public String getCodeSerialString() {
        return "";
    }

    @Override
    public void setCodeSerialString(String codeSerialString) {
        super.setCodeSerialString("");
    }
}
