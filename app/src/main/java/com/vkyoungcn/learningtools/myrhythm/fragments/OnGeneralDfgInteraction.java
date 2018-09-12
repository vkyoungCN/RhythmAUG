package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.os.Bundle;
/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
public interface OnGeneralDfgInteraction {
//* DialogFragment与调用方Activity之间交互（从DFG向ACT回送数据）的通用（自定义）接口
//* 回调方法中传回两个数据：①本操作的类型（是下列定义的操作类型常量之一）；②本操作附带的数据（
// 以Bundle形式发回。）


    int CREATE_RHYTHM = 2201;

    int CHOOSE_RHYTHM_FOR_GROUP = 2201;


    int MODEL_TYPE_RH =201;
    int MODEL_TYPE_LY =202;
    int MODEL_TYPE_PT =203;


    int DELETE_RHYTHM =2211;
    int DELETE_GROUP =2212;
    int DELETE_LYRIC = 2213;

    int REMOVE_RHYTHM =2291;
    int REMOVE_GROUP =2292;
    int REMOVE_LYRIC = 2293;


    int RHYTHM_CREATE_EDITED = 2215;
    int RHYTHM_CREATE_DONE = 2216;

    int RHYTHM_PURE_EDIT_DONE = 2217;


    int SELECT_FILE = 2231;

    void onButtonClickingDfgInteraction(int dfgType, Bundle data);
}
