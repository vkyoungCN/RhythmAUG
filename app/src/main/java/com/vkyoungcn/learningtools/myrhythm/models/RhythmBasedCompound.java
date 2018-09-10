package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RhythmBasedCompound extends Rhythm {
// ①是同时包含节奏所有字段，以及两行歌词（的字串字段，可空）、一列音调序列（的编码字段，可空）的
// 混合体资源的简化版（毕竟不是把三种资源类所有字段都包含进来）。
// 基于节奏进行扩展是因为程序在业务设计上仍然是围绕“节奏”为中心展开的，节奏暂时作为主要资源，
// 歌词、音调基于节奏而随附展示（当然其本身也是以独立资源形式保存的，可以独立编辑、处理）

//    ②新增直接将时间字段以字串显示的“字串时间”字段。（本类保证能在设置long时间时自动生成之，不能单独设置）
//    ③歌词、音调、字串时间字段仅用于显示，均不能进行设置。
    /* */
    private static final String TAG = "RhythmBasedCompound";
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* 以下5个特有字段（具备自动联动能力的字段可以没有setter；但其他三个字段要有setter，否则无法设置值）*/
    private ArrayList<String> primaryLyricPhrases = new ArrayList<>();
    private ArrayList<String> secondLyricPhrases = new ArrayList<>();

    private ArrayList<Byte> linkingPitches = new ArrayList<>();

    private String createTimeStr = "";
    private String lastModifyTimeStr = "";


    public RhythmBasedCompound() {
    }


    /* byte编码留空，通过string编码设置后自动生成的构造器*/
    public RhythmBasedCompound(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, int rhythmType, int primaryLyricId, int secondLyricId, int pitchesId, ArrayList<String> primaryLyricPhrases, ArrayList<String> secondLyricPhrases, ArrayList<Byte> pitchesSequence) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars, rhythmType, primaryLyricId, secondLyricId, pitchesId);
        this.primaryLyricPhrases = primaryLyricPhrases;
        this.secondLyricPhrases = secondLyricPhrases;
        this.linkingPitches = pitchesSequence;
        setCreateTimeStrFromLong(createTime);
        setLastModifyTimeStrFromLong(lastModifyTime);
    }

    /* 完全字段构造器*/
    public RhythmBasedCompound(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, ArrayList<Byte> codeSerialByte, int rhythmType, int primaryLyricId, int secondLyricId, int pitchesId, ArrayList<String> primaryLyricPhrases, ArrayList<String> secondLyricPhrases, ArrayList<Byte> pitchesSequence, String createTimeStr, String lastModifyTimeStr) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars, codeSerialByte, rhythmType, primaryLyricId, secondLyricId, pitchesId);
        this.primaryLyricPhrases = primaryLyricPhrases;
        this.secondLyricPhrases = secondLyricPhrases;
        this.linkingPitches = pitchesSequence;
        setCreateTimeStrFromLong(createTime);
        setLastModifyTimeStrFromLong(lastModifyTime);
    }

    //形参与目标字段同型版本
    public void setPrimaryLyricPhrases(ArrayList<String> primaryLyricPhrases) {
        if (primaryLyricPhrases ==null){
            return;
        }
        this.primaryLyricPhrases = primaryLyricPhrases;
    }

    public void setSecondLyricPhrases(ArrayList<String> secondLyricPhrases) {
        if (secondLyricPhrases ==null){
            return;
        }
        this.secondLyricPhrases = secondLyricPhrases;
    }

    //不同型版本
    public void setPrimaryLyricPhrases(String primaryLyricSerial) {
        if (primaryLyricSerial==null){
            return;
        }
        this.primaryLyricPhrases = Lyric.toPhrasesByCodeSerialString(primaryLyricSerial);
    }

    public void setSecondLyricPhrases(String secondLyricSerial) {
        if (secondLyricSerial==null){
            return;
        }
        this.secondLyricPhrases = Lyric.toPhrasesByCodeSerialString(secondLyricSerial);
    }

    public void setLinkingPitches(ArrayList<Byte> linkingPitches) {
        if (linkingPitches==null){
            linkingPitches = new ArrayList<>((byte)0);
        }
        this.linkingPitches = linkingPitches;
    }

    public ArrayList<String> getPrimaryLyricPhrases() {
        return primaryLyricPhrases;
    }

    public ArrayList<String> getSecondLyricPhrases() {
        return secondLyricPhrases;
    }

    public ArrayList<Byte> getLinkingPitches() {
        return linkingPitches;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public String getLastModifyTimeStr() {
        return lastModifyTimeStr;
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


    public void setCreateTimeStrFromLong(long createTimeLong) {
        Date createTimeDate = new Date(createTimeLong);
        this.createTimeStr = sdFormat.format(createTimeDate);
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

        parcel.writeSerializable(primaryLyricPhrases);
        parcel.writeSerializable(secondLyricPhrases);
        parcel.writeSerializable(linkingPitches);

        parcel.writeString(createTimeStr);
        parcel.writeString(lastModifyTimeStr);

    }

    public static final Creator<RhythmBasedCompound> CREATOR = new Creator<RhythmBasedCompound>(){
        @Override
        public RhythmBasedCompound createFromParcel(Parcel parcel) {
            return new RhythmBasedCompound(parcel);
        }

        @Override
        public RhythmBasedCompound[] newArray(int size) {
            return new RhythmBasedCompound[size];
        }
    };

    private RhythmBasedCompound(Parcel in){
        super(in);
//        Log.i(TAG, "RhythmBasedCompound: parcel constructor");
        primaryLyricPhrases = (ArrayList<String>) in.readSerializable();
        secondLyricPhrases = (ArrayList<String>) in.readSerializable();
        linkingPitches = (ArrayList<Byte>) in.readSerializable();
        createTimeStr = in.readString();
        lastModifyTimeStr = in.readString();
    }

}
