package com.vkyoungcn.learningtools.myrhythm.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class ToDetailClickListener implements View.OnClickListener {
    int model_id = 0;
    Context context;
    Class<?> clz;
//    Bundle dataWithIntent;

    public ToDetailClickListener(int model_id, Context context, Class<?> clz) {
        this.model_id = model_id;
        this.context = context;
        this.clz = clz;
//        this.dataWithIntent = dataWithIntent;
    }

    @Override
    public void onClick(View v) {
        toDetailActivity(context,model_id,clz);//这样就可以通过覆写此方法达到保留onClick部分代码的目的

    }


    void toDetailActivity(Context context, int model_id, Class<?> clz){
        Intent intentToDetailActivity = new Intent(context, clz);
        intentToDetailActivity.putExtra("MODEL_ID",model_id);
        context.startActivity(intentToDetailActivity);
    }

}
