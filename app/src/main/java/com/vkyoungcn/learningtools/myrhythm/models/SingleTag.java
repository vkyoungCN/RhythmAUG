package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;


/*
* 对应于数据表直接设计的数据类
* 相对的是“LFR”，后者携带了交叉表中的“所归属的节奏id、是否是该节奏的主要歌词”两额外字段
* */
public class SingleTag implements Parcelable {
    int id=0;
    String title="";
    String description="";

    public SingleTag() {
    }

    public SingleTag(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        parcel.writeString(description);
    }

    public static final Creator<SingleTag> CREATOR = new Creator<SingleTag>(){
        @Override
        public SingleTag createFromParcel(Parcel parcel) {
            return new SingleTag(parcel);
        }

        @Override
        public SingleTag[] newArray(int size) {
            return new SingleTag[size];
        }
    };

    private SingleTag(Parcel in){
        id = in.readInt();
        title = in.readString();
        description = in.readString();
    }
}
