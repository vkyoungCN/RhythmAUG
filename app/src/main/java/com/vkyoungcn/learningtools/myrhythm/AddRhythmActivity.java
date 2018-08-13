package com.vkyoungcn.learningtools.myrhythm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_24;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_34;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_38;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_44;
import static com.vkyoungcn.learningtools.myrhythm.models.Rhythm.RHYTHM_TYPE_68;

public class AddRhythmActivity extends AppCompatActivity implements View.OnClickListener {

    /* 逻辑*/
    private int valueOfBeat = 16;
    private int valueOfSection = 64;
    private int sectionSize = 4;

    private ArrayList<Byte> codes = new ArrayList<>();
    private ArrayList<ArrayList<Byte>> codesInSections = new ArrayList<>();

    private int currentSectionIndex = 0;
    private int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

    Rhythm rhythm ;


    /* 自定义控件*/
    private RhythmSingleLineEditor rh_editor_ARA;

    /* 35个控件，其中33个（非edt的）有点击事件*/
    private TextView tv_x0;
    private TextView tv_xb1;
    private TextView tv_xb2;
    private TextView tv_xb3;

    private TextView tv_xp;
    private TextView tv_xpb1;
    private TextView tv_xpb2;

    private TextView tv_xl1;
    private TextView tv_xl2;
    private TextView tv_xl3;

    private TextView tv_xm;
    private TextView tv_xm1;
    private TextView tv_xm2;

    private TextView tv_empty;

    private ImageView imv_x0;
    private ImageView imv_xb1;
    private ImageView imv_xb2;
    private ImageView imv_xb3;

    private ImageView imv_xp;
    private ImageView imv_xpb1;
    private ImageView imv_xpb2;

    private ImageView imv_xl1;
    private ImageView imv_xl2;
    private ImageView imv_xl3;

    private ImageView imv_xm;
    private ImageView imv_xm1;
    private ImageView imv_xm2;

    private EditText edt_xmNum;
    private EditText edt_longCurveNum;

    private ImageView imv_longCurve;

    private TextView tv_longCurve_remove;
    private TextView tv_allConfirm;

    private TextView tv_lastSection;
    private TextView tv_nextSection;
    private TextView tv_lastUnit;
    private TextView tv_nextUnit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rhythm);

        rh_editor_ARA = findViewById(R.id.rh_editor_ARA);

        tv_x0 = findViewById(R.id.tv_x0_ARA);
        tv_xb1 = findViewById(R.id.tv_xb1_ARA);
        tv_xb2 = findViewById(R.id.tv_xb2_ARA) ;
        tv_xb3 = findViewById(R.id.tv_xb3_ARA) ;

        tv_xp = findViewById(R.id.tv_xp_ARA) ;
        tv_xpb1 = findViewById(R.id.tv_xpb1_ARA) ;
        tv_xpb2 = findViewById(R.id.tv_xpb2_ARA) ;

        tv_xl1 = findViewById(R.id.tv_xl1_ARA) ;
        tv_xl2 = findViewById(R.id.tv_xl2_ARA) ;
        tv_xl3 = findViewById(R.id.tv_xl3_ARA) ;

        tv_xm = findViewById(R.id.tv_xm_ARA) ;
        tv_xm1 = findViewById(R.id.tv_xm1_ARA) ;
        tv_xm2 = findViewById(R.id.tv_xm2_ARA) ;

        tv_empty = findViewById(R.id.tv_empty_ARA);

        imv_x0 = findViewById(R.id.imv_x0_ARA);
        imv_xb1 = findViewById(R.id.imv_xb1_ARA) ;
        imv_xb2 = findViewById(R.id.imv_xb2_ARA) ;
        imv_xb3 = findViewById(R.id.imv_xb3_ARA) ;

        imv_xp = findViewById(R.id.imv_xp_ARA) ;
        imv_xpb1 = findViewById(R.id.imv_xpb1_ARA) ;
        imv_xpb2 = findViewById(R.id.imv_xpb2_ARA) ;

        imv_xl1 = findViewById(R.id.imv_xl1_ARA) ;
        imv_xl2 = findViewById(R.id.imv_xl2_ARA) ;
        imv_xl3 = findViewById(R.id.imv_xl3_ARA) ;

        imv_xm = findViewById(R.id.imv_xm_ARA) ;
        imv_xm1 = findViewById(R.id.imv_xm1_ARA) ;
        imv_xm2 = findViewById(R.id.imv_xm2_ARA) ;


        edt_xmNum = findViewById(R.id.edt_xmNum_ARA);
        edt_longCurveNum = findViewById(R.id.edt_longCurveSpan_ARA);

        imv_longCurve = findViewById(R.id.imv_longCurveEnd_ARA);

        tv_longCurve_remove =findViewById(R.id.tv_longCurveRemove_ARA) ;
        tv_allConfirm = findViewById(R.id.tv_confirmAddRhythm_ARA);

        tv_lastSection = findViewById(R.id.tv_lastSection_ARA);
        tv_nextSection = findViewById(R.id.tv_nextSection_ARA);
        tv_lastUnit=findViewById(R.id.tv_lastUnit_ARA);
        tv_nextUnit = findViewById(R.id.tv_nextUnit_ARA);


        //设监听
        imv_x0.setOnClickListener(this);
        imv_xb1.setOnClickListener(this);
        imv_xb2.setOnClickListener(this);
        imv_xb3.setOnClickListener(this);

        imv_xp.setOnClickListener(this) ;
        imv_xpb1.setOnClickListener(this);
        imv_xpb2.setOnClickListener(this);

        imv_xl1.setOnClickListener(this);
        imv_xl2.setOnClickListener(this);
        imv_xl3.setOnClickListener(this);

        imv_xm.setOnClickListener(this);
        imv_xm1.setOnClickListener(this);
        imv_xm2.setOnClickListener(this);

//        edt_xmNum.setOnClickListener(this);
//        edt_longCurveNum.setOnClickListener(this);

        imv_longCurve.setOnClickListener(this);

        tv_longCurve_remove.setOnClickListener(this);
        tv_allConfirm.setOnClickListener(this);

        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);

        tv_empty.setOnClickListener(this);

        int rhythmType = getIntent().getBundleExtra("BUNDLE").getInt("RHYTHM_TYPE");
        switch (rhythmType){
            case RHYTHM_TYPE_24:
                valueOfSection = 32;
                //此时beat值==16无需修改
                sectionSize = 2;
                break;
            case RHYTHM_TYPE_34:
                valueOfSection = 48;
                sectionSize = 3;
                break;
            case RHYTHM_TYPE_44:
                valueOfSection = 64;
                break;
            case RHYTHM_TYPE_38:
                valueOfSection = 24;
                valueOfBeat = 8;
                sectionSize = 3;
                break;
            case RHYTHM_TYPE_68:
                valueOfSection = 48;
                valueOfBeat = 8;
                sectionSize = 6;
                break;
        }

        //给下方的tv区设置值（对应时值的值的说明区域）
        tv_x0.setText(String.valueOf(valueOfBeat));
        tv_xb1.setText(String.valueOf(valueOfBeat/2));
        tv_xb2.setText(String.valueOf(valueOfBeat/4));
        tv_xb3.setText(String.valueOf(valueOfBeat/8));

        tv_xp.setText(String.valueOf(valueOfBeat+valueOfBeat/2));
        tv_xpb1.setText(String.valueOf(valueOfBeat/2+valueOfBeat/4));
        tv_xpb2.setText(String.valueOf(valueOfBeat/4+valueOfBeat/8));

        tv_xl1.setText(String.valueOf(valueOfBeat*2));
        tv_xl2.setText(String.valueOf(valueOfBeat*3));
        tv_xl3.setText(String.valueOf(valueOfBeat*4));

        tv_xm.setText(String.valueOf(valueOfBeat));
        tv_xm1.setText(String.valueOf(valueOfBeat/2));
        tv_xm2.setText(String.valueOf(valueOfBeat/4));

        //初始化初始数据源
        ArrayList<Byte> firstSection = new ArrayList<>();
        for (int i=0;i<sectionSize;i++) {
            firstSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
        }
        codesInSections.add(firstSection);
        codes.addAll(firstSection);

        rhythm = new Rhythm();
        //暂时只对节奏数据类设置两项即可。
        rhythm.setRhythmCodeSerial(codes);
        rhythm.setRhythmType(rhythmType);

        rh_editor_ARA.setRhythm(codesInSections,rhythmType,14,18);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){


        }
    }

    private void changeCode(Byte newCode){
        //确定剩余的可用时值值
        ArrayList<Byte> currentSectionCodes = codesInSections.get(currentSectionIndex);
        int emptyValueInSection = 0;
        for(Byte code:currentSectionCodes){
            if(code< 0 ){
                emptyValueInSection-=code;
            }
        }

        //判断新编码的时值是否符号条件
        int newValue = valueOfBeat;
        if(newCode < 73 && newCode>0){
            newValue = newCode;
        }
        if(emptyValueInSection<newValue){
            Toast.makeText(this, "小节内剩余空时值不足，请考虑删除其他已有音符", Toast.LENGTH_SHORT).show();
        }else {
            currentSectionCodes.set(currentUnitIndexInSection,newCode);
        }

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }

    private void changeToEmpty(){
        byte b = codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection);
        codesInSections.get(currentSectionIndex).set(currentUnitIndexInSection,(byte)-b);

        //通知到UI改变
        rh_editor_ARA.codeChangedReDraw();


    }

    private void moveBox(int moveType){
        int result = rh_editor_ARA.moveBox(moveType);
        switch (result){
            case 1:
                currentUnitIndexInSection++;
                break;
            case 11:
                currentUnitIndexInSection =0;
                currentSectionIndex++;
                break;
            case -1:
                currentUnitIndexInSection--;
                break;
            case -11:
                currentSectionIndex--;
                currentUnitIndexInSection =(codesInSections.get(currentSectionIndex).size()-1);
                break;
            case -19:
                currentSectionIndex--;
                currentUnitIndexInSection = 0;
                break;
        }


    }



}
