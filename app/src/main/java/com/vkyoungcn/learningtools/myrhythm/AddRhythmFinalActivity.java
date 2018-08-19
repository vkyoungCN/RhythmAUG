package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmHelper;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.fragments.FinalAddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.util.ArrayList;

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

    private CompoundRhythm compoundRhythm;



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

        compoundRhythm = getIntent().getParcelableExtra("COMPOUND_RHYTHM");

        if(compoundRhythm == null){
            Toast.makeText(this, "出错，节奏数据传递为空", Toast.LENGTH_SHORT).show();
            return;
        }

        rhythmView.setRhythmViewData(compoundRhythm);

        String strRhythmType = "";
        switch (compoundRhythm.getRhythmType()){
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
                compoundRhythm.setSelfDesign(ckb_selfDesign.isChecked());
                compoundRhythm.setKeepTop(ckb_keepTop.isChecked());
                compoundRhythm.setStars(Integer.parseInt((String)(spinner.getSelectedItem())));
                compoundRhythm.setDescription(edt_description.getText().toString());//一定非null

                long currentTime = System.currentTimeMillis();
                compoundRhythm.setCreateTime(currentTime);
                compoundRhythm.setLastModifyTime(currentTime);//二者严格一致

                //向DB填入
                MyRhythmDbHelper dbHelper = MyRhythmDbHelper.getInstance(getApplicationContext());
                long l = dbHelper.createRhythm(compoundRhythm);
                //根据返回的结果（布尔值——成败。），向dfg传递成功或失败。
                boolean resultOk = true;
                if(l<0){
                    //失败
                    resultOk =false;
                }
                //dfg中展示结果（从DB重新获取）
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("FINAL_ADD_RHYTHM");

                if (prev != null) {
                    Toast.makeText(getApplicationContext(), "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
                    transaction.remove(prev);
                }
                DialogFragment dfg = FinalAddRhythmDiaFragment.newInstance(resultOk);
                dfg.show(transaction, "FINAL_ADD_RHYTHM");

            }
        });

    }
}
