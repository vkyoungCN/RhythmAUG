package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RvBassActivity extends AppCompatActivity {
    public static final int MESSAGE_PRE_DB_FETCHED = 5505;
    public static final int MESSAGE_RE_FETCHED = 5506;

    RecyclerView mRv;
    RecyclerView.Adapter adapter;
    View maskView;


    MyRhythmDbHelper rhythmDbHelper;

    ArrayList<Object> dataFetched;
    Handler handler = new RvBassActivityHandler(this);//涉及弱引用，通过其发送消息。


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这是一个用于扩展的基础类，不加载具体布局

        //但是要实例化dbH类。
        rhythmDbHelper = MyRhythmDbHelper.getInstance(getApplicationContext());

//        new Thread(new FetchDataRunnable()).start();
    }

    class FetchDataRunnable implements Runnable{
        @Override
        public void run() {

            //然后封装消息
            Message message = new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);

        }
    }


    final static class RvBassActivityHandler extends Handler {
        final WeakReference<RvBassActivity> activityWeakReference;

        RvBassActivityHandler(RvBassActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RvBassActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

    void handleMessage(Message message) {
        //基础基类的实现只负责取消上方遮罩
        if(maskView.getVisibility() == View.VISIBLE) {
            maskView.setVisibility(View.GONE);
        }
    }
}
