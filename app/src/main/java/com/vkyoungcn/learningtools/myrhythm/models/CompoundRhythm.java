package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CompoundRhythm extends Rhythm implements Parcelable {
//    *将节奏所属的Lyric字串（如果有的话）同节奏类本身存储到一起。
// 【这个类设计目前仍然是有必要的。】
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //比基类多出如下字段
    private String primaryLyricSerial = "";
    private String secondLyricSerial = "";

    private String createTimeStr = "";
    private String lastModifyTimeStr = "";


    public CompoundRhythm() {
        super();
    }

    public CompoundRhythm(Rhythm rhythm,Lyric primaryLyric, Lyric secondLyric) {
        super(rhythm.getId(),rhythm.getTitle(),rhythm.getDescription(), rhythm.getRhythmType(),
                rhythm.getRhythmCodeSerial(),rhythm.isSelfDesign(),rhythm.isKeepTop(),rhythm.getStars(),
                rhythm.getCreateTime(),rhythm.getLastModifyTime(),rhythm.getPrimaryLyricId(),rhythm.getSecondLyricId(),rhythm.getPitchesId());
        this.primaryLyricSerial = primaryLyricSerial;
        this.secondLyricSerial = secondLyricSerial;

        Date createTimeDate = new Date(rhythm.getCreateTime());
        Date lastModifyTimeDate = new Date(rhythm.getLastModifyTime());
//        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.createTimeStr = sdFormat.format(createTimeDate);
        this.lastModifyTimeStr = sdFormat.format(lastModifyTimeDate);


    }

    public CompoundRhythm(int id, String title, String description, int rhythmType, ArrayList<Byte> rhythmCodeSerial, boolean isSelfDesign, boolean keepTop, int stars, long createTime, long lastModifyTime, int primaryLyricId, int secondLyricId, int pitchesId, String primaryLyricSerial, String secondLyricSerial, String createTimeStr, String lastModifyTimeStr) {
        super(id, title, description, rhythmType, rhythmCodeSerial, isSelfDesign, keepTop, stars, createTime, lastModifyTime, primaryLyricId, secondLyricId, pitchesId);
        this.primaryLyricSerial = primaryLyricSerial;
        this.secondLyricSerial = secondLyricSerial;
        this.createTimeStr = createTimeStr;
        this.lastModifyTimeStr = lastModifyTimeStr;
    }

    public String getPrimaryLyricSerial() {
        return primaryLyricSerial;
    }

    public void setPrimaryLyricSerial(String primaryLyricSerial) {
        this.primaryLyricSerial = primaryLyricSerial;
    }

    public String getSecondLyricSerial() {
        return secondLyricSerial;
    }

    public void setSecondLyricSerial(String secondLyricSerial) {
        this.secondLyricSerial = secondLyricSerial;
    }

    //以下两方法覆写，以保证在设置long字段的同时，同步设置字串字段
    @Override
    public void setCreateTime(long createTime) {
        super.setCreateTime(createTime);
        setCreateTimeStrFromLong(createTime);
    }

    @Override
    public void setLastModifyTime(long lastModifyTime) {
        super.setLastModifyTime(lastModifyTime);
        setLastModifyTimeStrFromLong(lastModifyTime);
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStrFromLong(long createTimeLong) {
        Date createTimeDate = new Date(createTimeLong);
        this.createTimeStr = sdFormat.format(createTimeDate);
    }

    public String getLastModifyTimeStr() {
        return lastModifyTimeStr;
    }

    public void setLastModifyTimeStrFromLong(long lastModifyTimeLong) {
        Date lastModifyTimeDate = new Date(lastModifyTimeLong);
        this.lastModifyTimeStr = sdFormat.format(lastModifyTimeDate);
    }

    /*
     * 以下是Parcelable要求的内容
     * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);

        parcel.writeString(primaryLyricSerial);
        parcel.writeString(secondLyricSerial);
    }

    public static final Creator<CompoundRhythm> CREATOR = new Creator<CompoundRhythm>(){
        @Override
        public CompoundRhythm createFromParcel(Parcel parcel) {
            return new CompoundRhythm(parcel);
        }

        @Override
        public CompoundRhythm[] newArray(int size) {
            return new CompoundRhythm[size];
        }
    };

    private CompoundRhythm(Parcel in){
        super(in);

        primaryLyricSerial = in.readString();
        secondLyricSerial = in.readString();
    }

}
