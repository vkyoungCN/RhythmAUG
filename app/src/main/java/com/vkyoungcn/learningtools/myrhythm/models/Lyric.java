package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;


/*
* 对应于数据表直接设计的数据类
* 相对的是“LFR”，后者携带了交叉表中的“所归属的节奏id、是否是该节奏的主要歌词”两额外字段
* */
public class Lyric implements Parcelable {
    private int id=0;
    private String title="";
    private String lyricSerial="";

    private String description="";
    private boolean isSelfDesign = false;
    private boolean keepTop=false;

    private long createTime=0;
    private long lastModifyTime=0;//可能需要按最近修改排序

    private int stars=0;//这个字段我总觉得可能有更好的替代。暂留。

    public Lyric() {
    }

    public Lyric(int id, String title, String lyricSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        this.id = id;
        this.title = title;
        this.lyricSerial = lyricSerial;
        this.description = description;
        this.isSelfDesign = isSelfDesign;
        this.keepTop = keepTop;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.stars = stars;
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

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isKeepTop() {
        return keepTop;
    }

    public void setKeepTop(boolean keepTop) {
        this.keepTop = keepTop;
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
        parcel.writeString(title);
        parcel.writeString(lyricSerial);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(stars);
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
        title = in.readString();
        lyricSerial = in.readString();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        stars = in.readInt();
    }
}
