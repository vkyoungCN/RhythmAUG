package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

public class Group extends BaseModel {
//    基本采用基类字段、逻辑即可；
// 但本类实际上不需要那么多字段，其中如codeSerial字段不需要有setter、getter等


    public Group() {
    }

    public Group(int id, String title,  String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, "",description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
    }


    /* 覆写以下字段的方法，禁用该字段*/
    @Override
    public String getCodeSerialString() {
        return "";
    }

    @Override
    public void setCodeSerialString(String codeSerialString) {
        super.setCodeSerialString("");
    }


    /*
     * 以下是Parcelable要求的内容【如果子类不覆写Parcel部分，则传递时会借用基类的；然后传递结果成为基类类型！】
     * （虽然不需要所有字段，但仍然传递）
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

    public static final Creator<Group> CREATOR = new Creator<Group>(){
        @Override
        public Group createFromParcel(Parcel parcel) {
            return new Group(parcel);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    Group(Parcel in){
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
