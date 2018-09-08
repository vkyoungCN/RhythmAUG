package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.view.View;

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
    public void refresh(View view){
        new Thread(new ReFetchDataRunnable()).start();
    }


}
