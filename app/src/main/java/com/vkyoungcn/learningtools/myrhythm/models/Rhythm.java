package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Rhythm extends BaseDualCodeSerialModel {

    //独有字段
    private int rhythmType =0;

    private int primaryLyricId=0;//保留一份“主要歌词”的id【暂时只在旋律下显示一行主歌词，因而可以这样操作】
    private int secondLyricId=0;
    private int pitchesId=0;


    public Rhythm() {
    }

    /* 设置字串编码不设Byte编码，构造器自动设置Byte编码*/
    public Rhythm(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, int rhythmType, int primaryLyricId, int secondLyricId, int pitchesId) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
        this.rhythmType = rhythmType;
        this.primaryLyricId = primaryLyricId;
        this.secondLyricId = secondLyricId;
        this.pitchesId = pitchesId;
    }

    /*完全构造器*/
    public Rhythm(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, ArrayList<Byte> codeSerialByte, int rhythmType, int primaryLyricId, int secondLyricId, int pitchesId) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars, codeSerialByte);
        this.rhythmType = rhythmType;
        this.primaryLyricId = primaryLyricId;
        this.secondLyricId = secondLyricId;
        this.pitchesId = pitchesId;
    }

    public int getSecondLyricId() {
        return secondLyricId;
    }

    public void setSecondLyricId(int secondLyricId) {
        this.secondLyricId = secondLyricId;
    }

    public int getPitchesId() {
        return pitchesId;
    }

    public void setPitchesId(int pitchesId) {
        this.pitchesId = pitchesId;
    }

    public int getRhythmType() {
        return rhythmType;
    }

    public void setRhythmType(int rhythmType) {
        this.rhythmType = rhythmType;
    }

    public int getPrimaryLyricId() {
        return primaryLyricId;
    }

    public void setPrimaryLyricId(int primaryLyricId) {
        this.primaryLyricId = primaryLyricId;
    }

    /*
     * 以下是Parcelable要求的内容
     * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(rhythmType);
        parcel.writeInt(primaryLyricId);
        parcel.writeInt(secondLyricId);
        parcel.writeInt(pitchesId);
    }

    public static final Parcelable.Creator<Rhythm> CREATOR = new Parcelable.Creator<Rhythm>(){
        @Override
        public Rhythm createFromParcel(Parcel parcel) {
            return new Rhythm(parcel);
        }

        @Override
        public Rhythm[] newArray(int size) {
            return new Rhythm[size];
        }
    };

    Rhythm(Parcel in){
        super(in);
        rhythmType = in.readInt();
        primaryLyricId = in.readInt();
        secondLyricId = in.readInt();
        pitchesId = in.readInt();
    }

}
