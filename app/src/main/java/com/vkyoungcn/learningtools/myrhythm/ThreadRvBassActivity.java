package com.vkyoungcn.learningtools.myrhythm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.sqlite.MyRhythmDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ThreadRvBassActivity<T extends BaseModel,K extends RecyclerView.Adapter >
        extends AppCompatActivity implements OnGeneralDfgInteraction {
    public static final int MESSAGE_PRE_DB_FETCHED = 5505;
    public static final int MESSAGE_RE_FETCHED = 5506;

    private static final String TAG = "ThreadRvBassActivity";
    RecyclerView mRv;
    K adapter;
    View maskView;


    MyRhythmDbHelper rhythmDbHelper;

    ArrayList<T> dataFetched;//T是BM的子类，使用泛型使dataFetched可以指向AL<Rhythm>等类型的列表。
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
            fetchAndSort();//子类可以通过覆写该方法实现自定义行为

            //然后封装消息
            Message message = new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);

        }
    }

    class ReFetchDataRunnable implements Runnable{
        @Override
        public void run() {
            reFetchAndSort();//子类可以通过覆写该方法实现自定义行为
//            Log.i(TAG, "run: reFetchRunnable");
            //然后封装消息
            Message message = new Message();
            message.what = MESSAGE_RE_FETCHED;
            //数据通过全局变量直接传递。

            handler.sendMessage(message);

        }
    }

    void fetchAndSort(){

    }

    void reFetchAndSort(){

    }

    final static class RvBassActivityHandler extends Handler {
        final WeakReference<ThreadRvBassActivity> activityWeakReference;

        RvBassActivityHandler(ThreadRvBassActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ThreadRvBassActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

    void handleMessage(Message message) {
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED:
                //取消上方遮罩
                if(maskView.getVisibility() == View.VISIBLE) {
                    maskView.setVisibility(View.GONE);
                }

                //为mRv加载adapter
                loadAdapter();

                break;
            case MESSAGE_RE_FETCHED:
//                Log.i(TAG, "handleMessage: ReFetched");
                //取消遮罩、更新rv数据
                if(maskView.getVisibility() == View.VISIBLE) {
                    maskView.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                break;
        }

    }

    void loadAdapter(){
        //子类负责实现
    };

    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        int modelId = data.getInt("MODEL_ID");
        switch (dfgType){
            case DELETE_RHYTHM:

                int l = rhythmDbHelper.deleteRhythmById(modelId);
//                Log.i(TAG, "onButtonClickingDfgInteraction: deleteRh. id="+modelId+", affected lines="+l);
                //删完要刷新
                new Thread(new ReFetchDataRunnable()).start();
                break;

            case DELETE_GROUP:
                rhythmDbHelper.deleteGroupById(data.getInt("MODEL_ID"));
                //删完要刷新
                new Thread(new ReFetchDataRunnable()).start();
                break;

            case REMOVE_RHYTHM:
            case REMOVE_LYRIC:
                rhythmDbHelper.removeModelCrossGroup(data.getInt("MODEL_ID"),data.getInt("GROUP_ID"));
                //删完要刷新
                new Thread(new ReFetchDataRunnable()).start();
                break;

        }
    }

}
