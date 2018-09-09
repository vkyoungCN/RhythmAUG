package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

import java.util.ArrayList;

public class RhythmLiteForGpX extends BaseModel {

    //字段比基类都少，
    //用于向GP-RH交叉表传数据，实际上只需要id
    //在传递之前的选择显示页，实际上也只需要id、title、描述、时间 四字段

    public RhythmLiteForGpX() {
    }

    public RhythmLiteForGpX(RhythmBasedCompound rbc) {
        this.id = rbc.getId();
        this.title = rbc.getTitle();
        this.description = rbc.getDescription();
        this.createTime = rbc.getCreateTime();
    }

    public RhythmLiteForGpX(Rhythm rhythm) {
        this.id = rhythm.getId();
        this.title = rhythm.getTitle();
        this.description = rhythm.getDescription();
        this.createTime = rhythm.getCreateTime();
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
    }

    public static final Creator<RhythmLiteForGpX> CREATOR = new Creator<RhythmLiteForGpX>(){
        @Override
        public RhythmLiteForGpX createFromParcel(Parcel parcel) {
            return new RhythmLiteForGpX(parcel);
        }

        @Override
        public RhythmLiteForGpX[] newArray(int size) {
            return new RhythmLiteForGpX[size];
        }
    };

    RhythmLiteForGpX(Parcel in){
        super(in);

    }

}
