package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.ChooseRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.DeleteModelDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmLiteForGpX;

public class AdapterMethodsHelper {
    private static final String TAG = "AdapterMethodsHelper";
    public static void toDetailActivity(Context context, BaseModel model, Class<?> clz){
        Intent intentToDetailActivity = new Intent(context, clz);
//        Log.i(TAG, "toDetailActivity: model in Listener = "+model.toString());
        intentToDetailActivity.putExtra("MODEL",model);
        context.startActivity(intentToDetailActivity);
    }
    public static void makeChoice(ChooseRhythmDiaFragment dfg, RhythmLiteForGpX item, boolean isToAdd){
        dfg.choseClicking(item,isToAdd);

    }

    public static void longClickingDelete(Context context, int itemId, int deleteType){
        FragmentTransaction transaction =((Activity)context).getFragmentManager().beginTransaction();
        Fragment prev = ((Activity)context).getFragmentManager().findFragmentByTag("DELETE_MODEL");

        if (prev != null) {
            Toast.makeText(context, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = DeleteModelDiaFragment.newInstance(itemId,deleteType);
        dfg.show(transaction, "DELETE_MODEL");
    }

}
