package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/*
* 对应于数据表直接设计的数据类
* 相对的是“LFR”，后者携带了交叉表中的“所归属的节奏id、是否是该节奏的主要歌词”两额外字段
* */
public class BaseModel implements Parcelable {
    private static final String TAG = "BaseModel";
    int id=0;
    String title="";
    String codeSerialString ="";
    String description="";
    boolean isSelfDesign = false;
    boolean keepTop=false;
    long createTime=0;
    long lastModifyTime=0;//可能需要按最近修改排序
    int stars=0;//这个字段我总觉得可能有更好的替代。暂留。

    public BaseModel() {
    }

    public BaseModel(int id, String title, String codeSerialString, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        this.id = id;
        this.title = title;
        this.codeSerialString = codeSerialString;
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

    public String getCodeSerialString() {
        return codeSerialString;
    }

    public void setCodeSerialString(String codeSerialString) {
        if(codeSerialString==null){//【引用类型需要这样处理一下。（可能多此一举了？）】
            codeSerialString = "";
        }
        this.codeSerialString = codeSerialString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description==null){
            description = "";
        }
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
        if(stars<1){
            this.stars = 1;
        }else if(stars>9){
            this.stars = 9;
        }else {
            this.stars = stars;
        }//对数据进行合理限制。【对应spinner的数据源数组也是1~9的范围】
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
//        Log.i(TAG, "writeToParcel: title = "+title);
        parcel.writeString(codeSerialString);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(stars);
    }

    public static final Creator<BaseModel> CREATOR = new Creator<BaseModel>(){
        @Override
        public BaseModel createFromParcel(Parcel parcel) {
            return new BaseModel(parcel);
        }

        @Override
        public BaseModel[] newArray(int size) {
            return new BaseModel[size];
        }
    };

    BaseModel(Parcel in){
        id = in.readInt();
        title = in.readString();
        codeSerialString = in.readString();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        stars = in.readInt();
    }
}
