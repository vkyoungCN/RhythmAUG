package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllRhythmsActivity extends RhythmRvBassActivity {

    //此控件在本页面实现中为tv

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //需要加载具体布局和控件了
        setContentView(R.layout.activity_all_rhythms);
        mRv = findViewById(R.id.rv_all_rhythms_ARhA);
        maskView = findViewById(R.id.tv_mask_ARhA);

        new Thread(new FetchDataRunnable()).start();//使用基类中的实现

    }

    //其余均使用直接基类的逻辑。
}
