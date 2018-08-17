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


    int ADD_RHYTHM = 2201;

    int DELETE_RHYTHM =2211;

    void onButtonClickingDfgInteraction(int dfgType, Bundle data);
}
