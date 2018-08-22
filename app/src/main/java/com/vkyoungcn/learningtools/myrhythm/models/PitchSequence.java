package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class PitchSequence extends BaseDualCodeSerialModel {
    //基本都在使用“双编码基类”的字段和逻辑（注意在基类中已经保证了两个编码在设置时：设一则两者俱改。）


    public PitchSequence() {
    }

    public PitchSequence(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
    }

    public PitchSequence(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, ArrayList<Byte> codeSerialByte) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars, codeSerialByte);
    }

}
