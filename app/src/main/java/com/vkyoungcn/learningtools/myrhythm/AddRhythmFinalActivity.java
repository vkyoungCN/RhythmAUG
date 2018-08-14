package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.fragments.FinalAddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;

public class AddRhythmFinalActivity extends AppCompatActivity {

    private RhythmView rhythmView;
    private TextView tv_rhythmType;
    private CheckBox ckb_selfDesign;
    private CheckBox ckb_keepTop;
    private Spinner spinner;
    private EditText edt_description;
    private TextView tv_confirm;

    private Rhythm rhythm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rhythm_final);

        rhythmView = findViewById(R.id.rhView_ARFA);
        tv_rhythmType = findViewById(R.id.tv_rhythmType_ARFA);
        ckb_selfDesign = findViewById(R.id.ckb_selfDesign_ARFA);
        ckb_keepTop = findViewById(R.id.ckb_keepTop_ARFA);
        spinner = findViewById(R.id.spinner_ARFA);
        edt_description = findViewById(R.id.edt_descriptionInput_ARFA);
        tv_confirm = findViewById(R.id.tv_confirm_ARFA);

        rhythm = getIntent().getParcelableExtra("RHYTHM");

        if(rhythm == null){
            Toast.makeText(this, "出错，节奏数据传递为空", Toast.LENGTH_SHORT).show();
            return;
        }

        rhythmView.setRhythmViewInfo(rhythm,14,18);
        String strRhythmType = "";
        switch (rhythm.getRhythmType()){
            case RHYTHM_TYPE_24:
                strRhythmType = "2/4";
                break;
            case RHYTHM_TYPE_34:
                strRhythmType = "3/4";
                break;
            case RHYTHM_TYPE_44:
                strRhythmType = "4/4";
                break;
            case RHYTHM_TYPE_38:
                strRhythmType = "3/8";
                break;
            case RHYTHM_TYPE_68:
                strRhythmType = "6/8";
                break;
        }
        tv_rhythmType.setText(strRhythmType);


        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rhythm.setSelfDesign(ckb_selfDesign.isChecked());
                rhythm.setKeepTop(ckb_keepTop.isChecked());
                rhythm.setStarts(Integer.parseInt((String)(spinner.getSelectedItem())));
                rhythm.setDescription(edt_description.getText().toString());//一定非null

                long currentTime = System.currentTimeMillis();
                rhythm.setCreateTime(currentTime);
                rhythm.setLastModifyTime(currentTime);//二者严格一致

                //向DB填入

                //根据返回的结果（布尔值——成败。），向dfg传递成功或失败。
                //dfg中展示结果（从DB重新获取）
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("FINAL_ADD_RHYTHM");

                if (prev != null) {
                    Toast.makeText(getApplicationContext(), "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
                    transaction.remove(prev);
                }
                DialogFragment dfg = FinalAddRhythmDiaFragment.newInstance();
                dfg.show(transaction, "FINAL_ADD_RHYTHM");

            }
        });

    }
}
