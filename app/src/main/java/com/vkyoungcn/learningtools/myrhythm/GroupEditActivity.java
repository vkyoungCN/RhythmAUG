package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.LyricFreeRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.REQUEST_CODE_GP_EDIT;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_GP_EDIT_DONE;

public class GroupEditActivity extends ThreadRvBassActivity implements OnGeneralDfgInteraction {

    Group group = new Group();

    private TextView tv_id;
    private EditText edt_title;
    private EditText edt_descriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);


        if(savedInstanceState!=null){
            group = savedInstanceState.getParcelable("GROUP");
        }else {
            group = getIntent().getParcelableExtra("GROUP");
        }

        tv_id = findViewById(R.id.tv_rhId_GEA);
        edt_title = findViewById(R.id.edt_title_GEA);
        edt_descriptions = findViewById(R.id.edt_description_GEA);

        initUiData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("GROUP", group);

    }


    private void initUiData(){
        tv_id.setText(String.format(this.getResources().getString(R.string.plh_sharp_id), group.getId()));
        edt_title.setText(group.getTitle());
        edt_descriptions.setText(group.getDescription());

    }
    
    public void confirmAndBack(View view){
        Intent intentBack = new Intent(this,GroupDetailActivity.class);
        group.setTitle(edt_title.getText().toString());
        group.setDescription(edt_descriptions.getText().toString());

        //存入DB（仅涉及group一张表，交叉信息不在此存储）
        MyRhythmDbHelper rhythmDbHelper = MyRhythmDbHelper.getInstance(this);
        rhythmDbHelper.updateGroupPure(group);

        intentBack.putExtra("GROUP", group);
        intentBack.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(intentBack,RESULT_CODE_GP_EDIT_DONE);


    }

}
