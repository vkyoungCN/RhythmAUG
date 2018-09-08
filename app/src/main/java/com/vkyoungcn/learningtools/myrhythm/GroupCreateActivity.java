package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.models.Group;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_CREATED;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class GroupCreateActivity extends GroupEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void confirmAndBack(View view){
        super.confirmAndBack(view);
    }

    @Override
    void updateModel() {
        //必须覆写，其基类（edit）是update方法。
        rhythmDbHelper.createGroupPure((Group) model);//存入DB（仅涉及group一张表，交叉信息不在此存储）
        //至此是生成一个新组，暂时没有关联所属内容。
    }

    @Override
    void backToDetail() {
        //实际要返回调用方，一般并不是详情页
//        Intent intentBack = new Intent();不用携带数据，回allGpRv列表，刷新数据即可。
//        intentBack.putExtra("MODEL", model);
        setResult(RESULT_CODE_GP_CREATED);
        this.finish();
    }
}
