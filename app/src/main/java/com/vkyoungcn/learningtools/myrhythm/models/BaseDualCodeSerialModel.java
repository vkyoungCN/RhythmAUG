package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class BaseDualCodeSerialModel extends BaseModel {
    //部分模型在DB中使用字串存放编码，但在程序端需要使用Byte列表。
    //新字段
    private ArrayList<Byte> codeSerialByte =new ArrayList<>();


    /* 构造器*/
    public BaseDualCodeSerialModel() {
    }

    /* 仅使用BassModel基类字段就能完全初始本类，自动设置关联的两个编码字段*/
    public BaseDualCodeSerialModel(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
        setPitchSequenceFromStr(codeSerial);
    }

    /* 完全构造器*/
    public BaseDualCodeSerialModel(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars, ArrayList<Byte> codeSerialByte) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
        this.codeSerialByte = codeSerialByte;
    }



    /* 编码字段改为设一得二的逻辑，保证二者在改动其一时能够联动变化，数据一致*/
    @Override
    public void setCodeSerialString(String codeSerialString) {
        super.setCodeSerialString(codeSerialString);
        setPitchSequenceFromStr(codeSerialString);
    }

    public void setCodeSerialByte(ArrayList<Byte> codeSerialByte) {
        this.codeSerialString = getStrCodeFromByteCode(codeSerialByte);
        this.codeSerialByte = codeSerialByte;
    }

    public ArrayList<Byte> getCodeSerialByte() {
        return codeSerialByte;
    }
    //基类string编码的getter不需覆写。



    /* 为实现两种编码互相转换而需要的一些方法*/
    private void setPitchSequenceFromStr(String pitchesCodeSerialStr){
        this.codeSerialByte.clear();
        for (byte b :pitchesCodeSerialStr.getBytes()) {
            this.codeSerialByte.add(b);
        }//无法利用Arrays.asList()直接转换，基础类型。
    }

    /* 由于两种编码联动式改变，因而直接获取getString即可
    public String getStrPitchSequence() {
        if (codeSerialByte == null || codeSerialByte.isEmpty()){
            return "";
        }else {
            return getStrCodeFromByteCode(codeSerialByte);
        }
    }*/

    private String getStrCodeFromByteCode(ArrayList<Byte> byteCodes) {
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

    /* 错误方式，无法正确的转换，如byte=16将被视作1、6两个字符存入（值49、54）
    public String getStrPitchSequence() {
        if (codeSerialByte == null || codeSerialByte.isEmpty()){
            return "";
        }else {
            StringBuilder sbd = new StringBuilder();
            for (Byte b :codeSerialByte) {
                sbd.append(b);
            }
            return sbd.toString();
        }
    }*/




    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);
        parcel.writeSerializable(codeSerialByte);
    }

    public static final Creator<BaseDualCodeSerialModel> CREATOR = new Creator<BaseDualCodeSerialModel>(){
        @Override
        public BaseDualCodeSerialModel createFromParcel(Parcel parcel) {
            return new BaseDualCodeSerialModel(parcel);
        }

        @Override
        public BaseDualCodeSerialModel[] newArray(int size) {
            return new BaseDualCodeSerialModel[size];
        }
    };

    BaseDualCodeSerialModel(Parcel in){
        super(in);
        codeSerialByte = (ArrayList<Byte>) in.readSerializable();
    }

}
