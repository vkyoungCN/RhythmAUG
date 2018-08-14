package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class Rhythm implements Parcelable {

    public static final int RHYTHM_TYPE_24 = 24;
    public static final int RHYTHM_TYPE_34 = 34;
    public static final int RHYTHM_TYPE_44 = 44;
    public static final int RHYTHM_TYPE_38 = 38;
    public static final int RHYTHM_TYPE_68 = 68;

    private int id;
    private int rhythmType;
    private ArrayList<Byte> rhythmCodeSerial;

    private String description;
    private boolean isSelfDesign = false;

    private boolean keepTop = false;
    private long createTime;
    private long lastModifyTime;//可能需要按最近修改排序

    private int starts;//这个字段我总觉得可能有更好的替代。暂留。

    private int primaryLyricId;//保留一份“主要歌词”的id【暂时只在旋律下显示一行主歌词，因而可以这样操作】

    public Rhythm() {
    }

    public Rhythm(int id, int rhythmType, ArrayList<Byte> rhythmCodeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int starts, int primaryLyricId) {
        this.id = id;
        this.rhythmType = rhythmType;
        this.rhythmCodeSerial = rhythmCodeSerial;
        this.description = description;
        this.isSelfDesign = isSelfDesign;
        this.keepTop = keepTop;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.starts = starts;
        this.primaryLyricId = primaryLyricId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRhythmType() {
        return rhythmType;
    }

    public void setRhythmType(int rhythmType) {
        this.rhythmType = rhythmType;
    }

    public ArrayList<Byte> getRhythmCodeSerial() {
        return rhythmCodeSerial;
    }

    public void setRhythmCodeSerial(ArrayList<Byte> rhythmCodeSerial) {
        this.rhythmCodeSerial = rhythmCodeSerial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelfDesign() {
        return isSelfDesign;
    }

    public void setSelfDesign(boolean selfDesign) {
        isSelfDesign = selfDesign;
    }

    public boolean isKeepTop() {
        return keepTop;
    }

    public void setKeepTop(boolean keepTop) {
        this.keepTop = keepTop;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public int getStarts() {
        return starts;
    }

    public void setStarts(int starts) {
        this.starts = starts;
    }

    public void setRhythmCodeSerialFromStr(String rhythmCodeSerialStr){
        this.rhythmCodeSerial.clear();
        for (byte b :rhythmCodeSerialStr.getBytes()) {
            this.rhythmCodeSerial.add(b);
        }//无法利用Arrays.asList()直接转换，基础类型。
    }

    public String getStrRhythmCodeSerial() {
        if (rhythmCodeSerial == null || rhythmCodeSerial.isEmpty()){
            return "";
        }else {
            StringBuilder sbd = new StringBuilder();
            for (Byte b :rhythmCodeSerial) {
                sbd.append(b);
            }
            return sbd.toString();
        }
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
        parcel.writeInt(id);
        parcel.writeInt(rhythmType);
        parcel.writeSerializable(rhythmCodeSerial);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(starts);
        parcel.writeInt(primaryLyricId);
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

    private Rhythm(Parcel in){
        id = in.readInt();
        rhythmType = in.readInt();
        rhythmCodeSerial = (ArrayList<Byte>) in.readSerializable();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        starts = in.readInt();
        primaryLyricId = in.readInt();
    }

}
