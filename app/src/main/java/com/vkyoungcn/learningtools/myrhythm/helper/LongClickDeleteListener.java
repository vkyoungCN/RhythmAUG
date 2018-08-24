package com.vkyoungcn.learningtools.myrhythm.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.DeleteModelDiaFragment;

public class LongClickDeleteListener implements View.OnLongClickListener {
    int itemId = 0;
    int deleteType = 0;
    Context context;

    public LongClickDeleteListener(Context context, int itemId,int deleteType ) {
        this.itemId = itemId;
        this.deleteType = deleteType;
    }

    @Override
    public boolean onLongClick(View v) {
        longClickingDelete(itemId,deleteType);
        return true;
    }

    void longClickingDelete(int itemId,int deleteType){
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
