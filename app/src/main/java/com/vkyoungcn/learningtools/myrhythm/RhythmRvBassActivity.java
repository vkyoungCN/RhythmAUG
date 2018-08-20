package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

public class RhythmRvBassActivity extends RvBassActivity implements OnGeneralDfgInteraction {

    /* 基类的部分字段实现为具体类型的字段*/
    RhythmRvAdapter adapter;
    ArrayList<CompoundRhythm> dataFetched;

    Handler handler = new RvBassActivityHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这是一个用于扩展的基础类，不加载具体布局

    }

    class FetchDataRunnable implements Runnable{
        @Override
        public void run() {
            dataFetched = rhythmDbHelper.getAllCompoundRhythms() ;

            //对返回的节奏进行排序（按修改时间降序？）
            Collections.sort(dataFetched,new SortByModifyTime());

            //然后封装消息
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



    void handleMessage(Message message) {
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED:
                //取消上方遮罩
                if(maskView.getVisibility() == View.VISIBLE) {
                    maskView.setVisibility(View.GONE);
                }
                //初始化Rv构造器，令UI加载Rv控件……
                adapter = new RhythmRvAdapter(dataFetched,this) ;
                mRv.setLayoutManager(new LinearLayoutManager(this));
                mRv.setAdapter(adapter);

                break;
            case MESSAGE_RE_FETCHED:
                //取消遮罩、更新rv数据
                if(maskView.getVisibility() == View.VISIBLE) {
                    maskView.setVisibility(View.GONE);
                }
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
                new Thread(new FetchDataRunnable()).start();
                break;
        }
    }

}
