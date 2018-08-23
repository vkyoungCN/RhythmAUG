package com.vkyoungcn.learningtools.myrhythm.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.DeleteRhythmDiaFragment;

public class LongClickDeleteListener implements View.OnLongClickListener {
    int itemId = 0;
    Context context;

    public LongClickDeleteListener(Context context, int itemId ) {
        this.itemId = itemId;
    }

    @Override
    public boolean onLongClick(View v) {
        longClickingDelete(itemId);
        return true;
    }

    void longClickingDelete(int itemId){
        FragmentTransaction transaction =((Activity)context).getFragmentManager().beginTransaction();
        Fragment prev = ((Activity)context).getFragmentManager().findFragmentByTag("DELETE_RHYTHM");

        if (prev != null) {
            Toast.makeText(context, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = DeleteRhythmDiaFragment.newInstance(itemId);
        dfg.show(transaction, "DELETE_RHYTHM");
    }

}
