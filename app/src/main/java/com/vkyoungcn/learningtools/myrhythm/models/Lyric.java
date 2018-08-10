package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Lyric implements Parcelable {
    private int id;
    private String lyricSerial;

    private String description;
    private boolean isSelfDesign = false;

    private long createTime;
    private long lastModifyTime;//可能需要按最近修改排序

    private int starts;//这个字段我总觉得可能有更好的替代。暂留。

    public Lyric(int id, String lyricSerial, String description, boolean isSelfDesign, long createTime, long lastModifyTime, int starts) {
        this.id = id;
        this.lyricSerial = lyricSerial;
        this.description = description;
        this.isSelfDesign = isSelfDesign;
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

    public String getLyricSerial() {
        return lyricSerial;
    }

    public void setLyricSerial(String lyricSerial) {
        this.lyricSerial = lyricSerial;
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

    /*
     * 以下是Parcelable要求的内容
     * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(lyricSerial);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(starts);
    }

    public static final Parcelable.Creator<Lyric> CREATOR = new Parcelable.Creator<Lyric>(){
        @Override
        public Lyric createFromParcel(Parcel parcel) {
            return new Lyric(parcel);
        }

        @Override
        public Lyric[] newArray(int size) {
            return new Lyric[size];
        }
    };

    private Lyric(Parcel in){
        id = in.readInt();
        lyricSerial = in.readString();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        starts = in.readInt();
    }
}
