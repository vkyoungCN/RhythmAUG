package com.vkyoungcn.learningtools.myrhythm.sqlite;

import android.util.Log;

import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import java.util.ArrayList;

public class TestDataHelper {
    private static final String TAG = "TestDataHelper";
//    ArrayList<Rhythm> rhythmsForTest = new ArrayList<>();


    public Rhythm populateRhythm(int multiply){
        Rhythm rhythm = new Rhythm();
//        Log.i(TAG, "populateRhythm: rhm:"+rhythm.toString());
        rhythm.setTitle(String.valueOf(System.currentTimeMillis()%1000000));
        rhythm.setDescription(String.valueOf(System.currentTimeMillis()));

        long currentTime = System.currentTimeMillis();
        rhythm.setCreateTime(currentTime);
        rhythm.setLastModifyTime(currentTime);

        rhythm.setSelfDesign(true);
        rhythm.setKeepTop(true);
        rhythm.setStars(3);

        rhythm.setRhythmType(RhythmHelper.RHYTHM_TYPE_44);

        rhythm.setCodeSerialByte(getCodeSerial(multiply));
        Log.i(TAG, "populateRhythm: rhythm.SIZE="+rhythm.getCodeSerialByte().size());

        return rhythm;
    }

    private ArrayList<Byte> getCodeSerial(int multiply){
        ArrayList<Byte> codes_1 = new ArrayList<>();
        Log.i(TAG, "getCodeSerial: list:"+codes_1.toString());
        int realNum = 1;
        if(multiply>1){
            realNum = multiply;
        }
        for(int i=0;i<multiply;i++){
            codes_1.add((byte)16);
            codes_1.add((byte)126);
            codes_1.add((byte)8);
            codes_1.add((byte)8);
            codes_1.add((byte)126);
            codes_1.add((byte)16);
            codes_1.add((byte)126);
            codes_1.add((byte)16);
            codes_1.add((byte)126);
            codes_1.add((byte)127);

            codes_1.add((byte)8);
            codes_1.add((byte)8);
            codes_1.add((byte)126);
            codes_1.add((byte)8);
            codes_1.add((byte)8);
            codes_1.add((byte)126);
            codes_1.add((byte)4);
            codes_1.add((byte)4);
            codes_1.add((byte)8);
            codes_1.add((byte)126);
            codes_1.add((byte)16);
            codes_1.add((byte)126);
            codes_1.add((byte)127);
        }
        return codes_1;
    }



}
