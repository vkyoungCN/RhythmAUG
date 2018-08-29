package com.vkyoungcn.learningtools.myrhythm.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.GroupDetailActivity;
import com.vkyoungcn.learningtools.myrhythm.fragments.DeleteModelDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.RemoveModelFromGroupDiaFragment;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_LYRIC;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_RHYTHM;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.MODEL_TYPE_LY;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.MODEL_TYPE_RH;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.REMOVE_LYRIC;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.REMOVE_RHYTHM;

/*多功能长按监听器。根据情况弹出对话框，选择“删除、移除、添加到分组”等选项*/
public class LongClickMultiFunctionListener implements View.OnLongClickListener {
    int itemId = 0;
    int modelType = 0;
    int actionType = 0;
    int groupId = 0;
    Context context;

    public LongClickMultiFunctionListener(Context context, int itemId, int modelType,int groupId) {
        this.itemId = itemId;
        this.modelType = modelType;
        this.context = context;
        this.groupId = groupId;
    }


    public LongClickMultiFunctionListener(Context context, int itemId, int modelType) {
        this.itemId = itemId;
        this.modelType = modelType;
        this.context = context;
    }

    @Override
    public boolean onLongClick(View v) {
        if(context instanceof GroupDetailActivity){
            switch (modelType){
                case MODEL_TYPE_RH:
                    actionType = REMOVE_RHYTHM;
                    break;
                case MODEL_TYPE_LY:
                    actionType = REMOVE_LYRIC;
                    break;
            }
            //弹出"从分组内移除"的DFG
            longClickingRemove(itemId, actionType,groupId);

        }else {
            switch (modelType){
                case MODEL_TYPE_RH:
                    actionType = DELETE_RHYTHM;
                    break;
                case MODEL_TYPE_LY:
                    actionType = DELETE_LYRIC;
                    break;
            }

            //弹出“删除”DFG
            longClickingDelete(itemId, modelType);
        }

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

    void longClickingRemove(int itemId,int removeType, int groupId){
        FragmentTransaction transaction =((Activity)context).getFragmentManager().beginTransaction();
        Fragment prev = ((Activity)context).getFragmentManager().findFragmentByTag("REMOVE_MODEL");

        if (prev != null) {
            Toast.makeText(context, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = RemoveModelFromGroupDiaFragment.newInstance(itemId,removeType,groupId);
        dfg.show(transaction, "REMOVE_MODEL");
    }

}
