package com.vkyoungcn.learningtools.myrhythm.helper;

import android.content.Context;
import android.graphics.Point;
import android.view.ViewGroup;
import android.view.WindowManager;

public class DiaFragmentReSize<T extends ViewGroup> {

    public void reSize(T rootView, Context context){
        WindowManager appWm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        try {
            appWm.getDefaultDisplay().getSize(point);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        T.LayoutParams vpLp = rootView.getLayoutParams();
        vpLp.height=(int)(point.x*0.9);
        vpLp.width = (int)(point.y*0.85);
        rootView.setLayoutParams(vpLp);
    }

}
