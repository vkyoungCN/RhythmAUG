package com.vkyoungcn.learningtools.myrhythm.customUI;

//用于描述各音符下划线绘制信息的类，用在DrawingUnit中
public class BottomLine {
    float startX;
    float startY;
    float toX;
    float toY;

    public BottomLine(float startX, float startY, float toX, float toY) {
        this.startX = startX;
        this.startY = startY;
        this.toX = toX;
        this.toY = toY;
    }
}