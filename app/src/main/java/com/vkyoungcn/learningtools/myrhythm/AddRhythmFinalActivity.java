package com.vkyoungcn.learningtools.myrhythm;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmView;
import com.vkyoungcn.learningtools.myrhythm.fragments.FinalAddRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.DELIVER_ERROR;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_DONE;
import static com.vkyoungcn.learningtools.myrhythm.MyRhythmConstants.RESULT_CODE_RH_CREATE_FAILURE;
import static com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper.RHYTHM_TYPE_68;

public class AddRhythmFinalActivity extends Activity implements OnGeneralDfgInteraction {
    private static final String TAG = "";
    private RhythmView rhythmView;
    private TextView tv_rhythmType;
    private CheckBox ckb_selfDesign;
    private CheckBox ckb_keepTop;
    private Spinner spinner;
    private EditText edt_description;
    private EditText edt_title;
    private TextView tv_confirm;

    private RhythmBasedCompound rhythmBasedCompound;


    //操作结果，是否成功（根据DB返回确定）
    boolean isResultOk = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_rhythm_final);

        rhythmView = findViewById(R.id.rhView_ARFA);
        tv_rhythmType = findViewById(R.id.tv_rhythmType_ARFA);
        ckb_selfDesign = findViewById(R.id.ckb_selfDesign_ARFA);
        ckb_keepTop = findViewById(R.id.ckb_keepTop_ARFA);
        spinner = findViewById(R.id.spinner_ARFA);
        edt_description = findViewById(R.id.edt_descriptionInput_ARFA);
        edt_title = findViewById(R.id.edt_titleInput_ARFA);
        tv_confirm = findViewById(R.id.tv_confirm_ARFA);

        rhythmBasedCompound = getIntent().getParcelableExtra("COMPOUND_RHYTHM");
        if(rhythmBasedCompound == null){
            Toast.makeText(this, "出错，节奏数据传递为空", Toast.LENGTH_SHORT).show();
            setResult(DELIVER_ERROR);
            this.finish();
            return;
        }

        rhythmView.setRhythmViewData(rhythmBasedCompound);

        String strRhythmType = "";
        switch (rhythmBasedCompound.getRhythmType()){
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
                rhythmBasedCompound.setSelfDesign(ckb_selfDesign.isChecked());
                rhythmBasedCompound.setKeepTop(ckb_keepTop.isChecked());
                rhythmBasedCompound.setStars(Integer.parseInt((String)(spinner.getSelectedItem())));
                rhythmBasedCompound.setDescription(edt_description.getText().toString());//一定非null(编译器提示)

                long currentTime = System.currentTimeMillis();
                String title = edt_title.getText().toString();
                if(title.isEmpty()){
                    title = "缺省标题"+currentTime%1000000;
                }
                rhythmBasedCompound.setTitle(title);//一定非null

                rhythmBasedCompound.setCreateTime(currentTime);
                rhythmBasedCompound.setLastModifyTime(currentTime);//二者严格一致

                //向DB填入
                MyRhythmDbHelper dbHelper = MyRhythmDbHelper.getInstance(getApplicationContext());
                long l = dbHelper.createRhythm(rhythmBasedCompound);
                //根据返回的结果（布尔值——成败。），向dfg传递成功或失败。
                if(l<0){
                    //失败
                    isResultOk =false;
                }
                Log.i(TAG, "onClick: affected lines="+l);

                //dfg中展示结果（从DB重新获取）
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("FINAL_ADD_RHYTHM");

                if (prev != null) {
                    Toast.makeText(getApplicationContext(), "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
                    transaction.remove(prev);
                }
                DialogFragment dfg = FinalAddRhythmDiaFragment.newInstance(isResultOk);
                dfg.show(transaction, "FINAL_ADD_RHYTHM");

            }
        });

    }

    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case RHYTHM_CREATE_DONE:
                //在提示成功或失败的dfg中点击了确定后，依次返回原来的各调用页，无需传递数据。
                if(isResultOk){
                    setResult(RESULT_CODE_RH_CREATE_DONE,null);
//                    Log.i(TAG, "onButtonClickingDfgInteraction: resultCode to send back="+RESULT_CODE_RH_CREATE_DONE);
                }else{
                    setResult(RESULT_CODE_RH_CREATE_FAILURE,null);
//                    Log.i(TAG, "onButtonClickingDfgInteraction: resultCode to send back=..");
                }
                this.finish();
        }
    }
}
