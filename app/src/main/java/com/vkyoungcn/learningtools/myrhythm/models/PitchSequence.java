package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class PitchSequence implements Parcelable {
    private int id=0;
    private String title="";
    private ArrayList<Byte> pitchSequence=new ArrayList<>();

    private boolean isSelfDesign = false;
    private boolean keepTop = false;
    private int stars=0;//这个字段我总觉得可能有更好的替代。暂留。

    private String description="";
    private long createTime=0;
    private long lastModifyTime=0;//可能需要按最近修改排序


    public PitchSequence() {
    }

    public PitchSequence(int id, String title, ArrayList<Byte> pitchSequence, boolean isSelfDesign, boolean keepTop, int stars, String description, long createTime, long lastModifyTime) {
        this.id = id;
        this.title = title;
        this.pitchSequence = pitchSequence;
        this.isSelfDesign = isSelfDesign;
        this.keepTop = keepTop;
        this.stars = stars;
        this.description = description;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
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

    public void setPitchesSerialFromStr(String pitchesCodeSerialStr){
        this.pitchSequence.clear();
        for (byte b :pitchesCodeSerialStr.getBytes()) {
            this.pitchSequence.add(b);
        }//无法利用Arrays.asList()直接转换，基础类型。
    }

    public String getStrRhythmCodeSerial() {
        if (pitchSequence == null || pitchSequence.isEmpty()){
            return "";
        }else {
            StringBuilder sbd = new StringBuilder();
            for (Byte b :pitchSequence) {
                sbd.append(b);
            }
            return sbd.toString();
        }
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
        this.stars = stars;
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
            return getStrCodeFromListCode(pitchSequence);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


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
        parcel.writeSerializable(pitchSequence);
        parcel.writeString(description);
        parcel.writeByte(isSelfDesign?(byte) 1:(byte) 0);
        parcel.writeByte(keepTop?(byte) 1:(byte) 0);
        parcel.writeLong(createTime);
        parcel.writeLong(lastModifyTime);
        parcel.writeInt(stars);
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
        title = in.readString();
        pitchSequence = (ArrayList<Byte>) in.readSerializable();
        description = in.readString();
        isSelfDesign = in.readByte()==1;
        keepTop = in.readByte() == 1;
        createTime = in.readLong();
        lastModifyTime = in.readLong();
        stars = in.readInt();
    }

}
