package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PitchSequence implements Parcelable {
    private int id;
    private ArrayList<Byte> pitchSequence;

    private boolean isSelfDesign = false;
    private boolean keepTop = false;
    private int starts;//这个字段我总觉得可能有更好的替代。暂留。

    private String description;
    private long createTime;
    private long lastModifyTime;//可能需要按最近修改排序


    public PitchSequence(int id, ArrayList<Byte> pitchSequence, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int starts) {
        this.id = id;
        this.pitchSequence = pitchSequence;
        this.description = description;
        this.isSelfDesign = isSelfDesign;
        this.keepTop = keepTop;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.starts = starts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Byte> getPitchSequence() {
        return pitchSequence;
    }

    public void setPitchSequence(ArrayList<Byte> pitchSequence) {
        this.pitchSequence = pitchSequence;
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

    public void setPitchSequenceFromStr(String rhythmCodeSerialStr){
        this.pitchSequence.clear();
        for (byte b :rhythmCodeSerialStr.getBytes()) {
            this.pitchSequence.add(b);
        }//无法利用Arrays.asList()直接转换，基础类型。
    }

    public String getStrPitchSequence() {
        if (pitchSequence == null || pitchSequence.isEmpty()){
            return "";
        }else {
            StringBuilder sbd = new StringBuilder();
            for (Byte b : pitchSequence) {
                sbd.append(b);
            }
            return sbd.toString();
        }
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
        parcel.writeSerializable(pitchSequence);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(starts);
    }

    public static final Parcelable.Creator<PitchSequence> CREATOR = new Parcelable.Creator<PitchSequence>(){
        @Override
        public PitchSequence createFromParcel(Parcel parcel) {
            return new PitchSequence(parcel);
        }

        @Override
        public PitchSequence[] newArray(int size) {
            return new PitchSequence[size];
        }
    };

    private PitchSequence(Parcel in){
        id = in.readInt();
        pitchSequence = (ArrayList<Byte>) in.readSerializable();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        starts = in.readInt();
    }

}
