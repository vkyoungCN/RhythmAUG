package com.vkyoungcn.learningtools.myrhythm.helper;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;


public class ToDetailClickListener implements View.OnClickListener {
    BaseModel model ;
    Context context;
    Class<?> clz;
//    Bundle dataWithIntent;

    public ToDetailClickListener(BaseModel model, Context context, Class<?> clz) {
        this.model = model;
        this.context = context;
        this.clz = clz;
//        this.dataWithIntent = dataWithIntent;
    }

    @Override
    public void onClick(View v) {
        toDetailActivity(context, model,clz);//这样就可以通过覆写此方法达到保留onClick部分代码的目的

    }


    void toDetailActivity(Context context, BaseModel model, Class<?> clz){
        Intent intentToDetailActivity = new Intent(context, clz);
        intentToDetailActivity.putExtra("MODEL",model);
        context.startActivity(intentToDetailActivity);
    }

}
