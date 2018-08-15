package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleModify implements Parcelable {
    public static final int TYPE_ADD_RHYTHM = 1001;
    public static final int TYPE_ADD_PITCH_SERIAL = 1002;
    public static final int TYPE_ADD_LYRIC = 1003;

    public static final int TYPE_DEL_RHYTHM = 1011;
    public static final int TYPE_DEL_PITCH_SERIAL = 1012;
    public static final int TYPE_DEL_LYRIC = 1013;

    public static final int TYPE_MODIFY_RHYTHM = 1021;
    public static final int TYPE_MODIFY_SERIAL = 1022;
    public static final int TYPE_MODIFY_LYRIC = 1023;

    public static final int TYPE_BACK_UP = 1051;


    private int id;
    private long actionTime;
    private int actionType;

    public SingleModify() {
    }

    public SingleModify(int id, long actionTime, int actionType) {
        this.id = id;
        this.actionTime = actionTime;
        this.actionType = actionType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
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
        parcel.writeLong(actionTime);
        parcel.writeInt(actionType);
    }

    public static final Parcelable.Creator<SingleModify> CREATOR = new Parcelable.Creator<SingleModify>(){
        @Override
        public SingleModify createFromParcel(Parcel parcel) {
            return new SingleModify(parcel);
        }

        @Override
        public SingleModify[] newArray(int size) {
            return new SingleModify[size];
        }
    };

    private SingleModify(Parcel in){
        id = in.readInt();
        actionTime = in.readLong();
        actionType = in.readInt();
    }


}
