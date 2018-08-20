package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rhythm implements Parcelable {

    public static final int RHYTHM_TYPE_24 = 24;
    public static final int RHYTHM_TYPE_34 = 34;
    public static final int RHYTHM_TYPE_44 = 44;
    public static final int RHYTHM_TYPE_38 = 38;
    public static final int RHYTHM_TYPE_68 = 68;

    private int id = 0;
    private String title = "";
    private String description ="";

    private int rhythmType =0;
    private ArrayList<Byte> rhythmCodeSerial = new ArrayList<>();

    private boolean isSelfDesign = false;
    private boolean keepTop = false;
    private int stars=0;//这个字段我总觉得可能有更好的替代。暂留。

    private long createTime=0;
    private long lastModifyTime=0;//可能需要按最近修改排序
    private int primaryLyricId=0;//保留一份“主要歌词”的id【暂时只在旋律下显示一行主歌词，因而可以这样操作】
    private int secondLyricId=0;
    private int pitchesId=0;


    public Rhythm() {
    }

    public Rhythm(int id, String title, String description, int rhythmType, ArrayList<Byte> rhythmCodeSerial, boolean isSelfDesign, boolean keepTop, int stars, long createTime, long lastModifyTime, int primaryLyricId, int secondLyricId, int pitchesId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rhythmType = rhythmType;
        this.rhythmCodeSerial = rhythmCodeSerial;
        this.isSelfDesign = isSelfDesign;
        this.keepTop = keepTop;

        //星级需要有限制（1~9）
        if(stars>9){this.stars=9;}
        else if(stars<1){this.stars =1;}
        else {this.stars = stars;}

        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.primaryLyricId = primaryLyricId;
        this.secondLyricId = secondLyricId;
        this.pitchesId = pitchesId;
    }

    public int getSecondLyricId() {
        return secondLyricId;
    }

    public void setSecondLyricId(int secondLyricId) {
        this.secondLyricId = secondLyricId;
    }

    public int getPitchesId() {
        return pitchesId;
    }

    public void setPitchesId(int pitchesId) {
        this.pitchesId = pitchesId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        if(stars>0&&stars<9){
            this.stars = stars;
        }//对数据进行合理限制。【对应spinner的数据源数组也是1~9的范围】
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
            return getStrCodeFromListCode(rhythmCodeSerial);
        }
    }

    // char转byte
    private ArrayList<Byte> getArrayListByteFromString (String strCodes) {
        char[] chars = strCodes.toCharArray();
        Charset cs = Charset.forName ("UTF-8");
        CharBuffer cb = CharBuffer.allocate (chars.length);
        cb.put (chars);
        cb.flip ();
        ByteBuffer bb = cs.encode (cb);
        byte[] bytes = bb.array();
        ArrayList<Byte> byteArrayList =new ArrayList<>();
        for (byte b :bytes) {
            byteArrayList.add(b);
        }
        return byteArrayList;

    }

// byte转char

    private String getStrCodeFromListCode (ArrayList<Byte> byteCodes) {
        Byte[] bytes = new Byte[byteCodes.size()];
        byteCodes.toArray(bytes);

        byte[] bytes_2 = new byte[byteCodes.size()];
        for (int i=0;i<byteCodes.size();i++) {
            bytes_2[i] = bytes[i];
        }

        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes_2);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);
        return cb.toString();
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
        parcel.writeString(title);
        parcel.writeInt(rhythmType);
        parcel.writeSerializable(rhythmCodeSerial);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(stars);
        parcel.writeInt(primaryLyricId);
        parcel.writeInt(secondLyricId);
        parcel.writeInt(pitchesId);

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

    Rhythm(Parcel in){
        id = in.readInt();
        title = in.readString();
        rhythmType = in.readInt();
        rhythmCodeSerial = (ArrayList<Byte>) in.readSerializable();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        stars = in.readInt();
        primaryLyricId = in.readInt();
        secondLyricId = in.readInt();
        pitchesId = in.readInt();

    }

}
