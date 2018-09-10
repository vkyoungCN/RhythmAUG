package com.vkyoungcn.learningtools.myrhythm.models;

import android.os.Parcel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class BaseDualCodeSerialModel extends BaseModel {
    //在DB中使用字串存放编码，但在程序端需要使用Byte列表。持有上述两个序列。
    //新字段
    private ArrayList<Byte> codeSerialByte =new ArrayList<>();
    /*
    * utf8数字，英文字母占1个字节
    * 汉字：占3个字节的：基本等同于GBK，含21000多个汉字（一说52156 个）
    * 占4个字节的：中日韩超大字符集里面的汉字，有5万多个（一说64029 个）
    * 【因而Lyric无法使用Byte存储的列表进行编码，应使用int】
    * */

    private static final String TAG = "BaseDualCodeSerialModel";

    /* 构造器*/
    public BaseDualCodeSerialModel() {
    }

    /* 仅使用BassModel基类字段就能完全初始本类，自动设置关联的两个编码字段*/
    public BaseDualCodeSerialModel(int id, String title, String codeSerial, String description, boolean isSelfDesign, boolean keepTop, long createTime, long lastModifyTime, int stars) {
        super(id, title, codeSerial, description, isSelfDesign, keepTop, createTime, lastModifyTime, stars);
        setCodeSerialByteFromStr(codeSerial);
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
        setCodeSerialByteFromStr(codeSerialString);
    }

    public void setCodeSerialByte(ArrayList<Byte> codeSerialByte) {
        if(codeSerialByte==null){
            codeSerialByte = new ArrayList<>((byte)0);
        }
        this.codeSerialString = getStrCodeFromByteCode(codeSerialByte);
        this.codeSerialByte = codeSerialByte;
    }

    public ArrayList<Byte> getCodeSerialByte() {
        return codeSerialByte;
    }
    //基类string编码的getter不需覆写。



    /* 为实现两种编码互相转换而需要的一些方法*/
    private void setCodeSerialByteFromStr(String codeSerialStr){
        this.codeSerialByte.clear();
        if(codeSerialStr == null){
            codeSerialStr = "";//排错空指针问题
        }
        for (byte b :codeSerialStr.getBytes()) {
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
            //Byte转成byte
            bytes_2[i] = bytes[i];
        }
        try {
            return new String(bytes_2,"ISO-8859-1");//Latin-1编码，0~127的字符与ASCII码相同，是单字节的编码方式（utf-8是变长，存取后数据有不一致现象）
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";//解码失败

        /*
        如果单个转换，一个byte 16会拆成1、6两个单元处理，从而出错。
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes_2);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);
        return cb.toString();*/
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
//        Log.i(TAG, "writeToParcel: csB="+codeSerialByte.toString());
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
//        Log.i(TAG, "BaseDualCodeSerialModel: parcel in");
        codeSerialByte = (ArrayList<Byte>) in.readSerializable();
//        Log.i(TAG, "BaseDualCodeSerialModel: coSB="+codeSerialByte.toString());

    }

}
