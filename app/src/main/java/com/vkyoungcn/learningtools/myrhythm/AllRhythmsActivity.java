package com.vkyoungcn.learningtools.myrhythm;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.adapter.RhythmRvAdapter;
import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllRhythmsActivity extends AppCompatActivity implements OnGeneralDfgInteraction {
    public static final int MESSAGE_PRE_DB_FETCHED = 5505;
    public static final int MESSAGE_RE_FETCHED = 5506;

    private RecyclerView mRv;
    private RhythmRvAdapter adapter;
    private TextView tv_mask;


    MyRhythmDbHelper rhythmDbHelper;
    long timeThreshold;

    ArrayList<CompoundRhythm> compoundRhythms;
    private Handler handler = new AllRhythmsActivityHandler(this);//涉及弱引用，通过其发送消息。


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_rhythms);
        
        mRv = findViewById(R.id.rv_all_rhythms_ARhA);
        tv_mask = findViewById(R.id.tv_mask_ARhA);

        new Thread(new FetchAllRhythmRunnable()).start();

    }

    class FetchAllRhythmRunnable implements Runnable{
        @Override
        public void run() {
            compoundRhythms = rhythmDbHelper.getAllCompoundRhythms() ;

            //对返回的节奏进行排序（置顶的在最上，其余按时间，越近越先）
            Collections.sort(compoundRhythms,new SortByModifyTime());

            //然后封转消息
            Message message = new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);

        }
    }

    class SortByModifyTime implements Comparator {
        public int compare(Object o1, Object o2) {
            CompoundRhythm s1 = (CompoundRhythm) o1;
            CompoundRhythm s2 = (CompoundRhythm) o2;
            return -Long.compare(s1.getLastModifyTime(), s2.getLastModifyTime());
            //降序
        }
    }



    final static class AllRhythmsActivityHandler extends Handler {
        private final WeakReference<AllRhythmsActivity> activityWeakReference;

        private AllRhythmsActivityHandler(AllRhythmsActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AllRhythmsActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

    void handleMessage(Message message) {
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED:
                //取消上方遮罩
                if(tv_mask.getVisibility() == View.VISIBLE) {
                    tv_mask.setVisibility(View.GONE);
                }

                //初始化Rv构造器，令UI加载Rv控件……
                adapter = new RhythmRvAdapter(compoundRhythms,this) ;
                mRv.setLayoutManager(new LinearLayoutManager(this));
                mRv.setAdapter(adapter);

                break;
            case MESSAGE_RE_FETCHED:
                //取消上方遮罩
                if(tv_mask.getVisibility() == View.VISIBLE) {
                    tv_mask.setVisibility(View.GONE);
                }

                //Rv数据修改
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case DELETE_RHYTHM:
                rhythmDbHelper.deleteRhythmById(data.getInt("RHYTHM_ID"));
                //删完要刷新
                new Thread(new FetchAllRhythmRunnable()).start();
                break;

        }
    }

}
