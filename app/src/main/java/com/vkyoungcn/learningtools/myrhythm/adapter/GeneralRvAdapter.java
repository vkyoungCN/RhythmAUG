package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineView;
import com.vkyoungcn.learningtools.myrhythm.fragments.DeleteRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.helper.LongClickDeleteListener;
import com.vkyoungcn.learningtools.myrhythm.helper.ToDetailClickListener;
import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.23
 * */
public class GeneralRvAdapter<T extends BaseModel> extends RecyclerView.Adapter<GeneralRvAdapter.ViewHolder>{
//* 是展示任务所属分组的RecyclerView所使用的适配器
// 采用纵向列表形式。
    static final String TAG = "GeneralRvAdapter";
    ArrayList<T> dataList;
    Context context;
    T singleModel;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_id;
        private final TextView tv_title;
        private final TextView tv_createTime;
        private final LinearLayout llt_overall;
//        private final View mainModelView;

        private ViewHolder(View itemView) {
            super(itemView);

//            mainModelView = itemView.findViewById(R.id.rhView_mainModel_generalRv);
            tv_id = itemView.findViewById(R.id.tv_rhId_generalRv);
            tv_title = itemView.findViewById(R.id.tv_title_generalRv);
            tv_createTime = itemView.findViewById(R.id.tv_createTime_generalRv);
            llt_overall = itemView.findViewById(R.id.llt_overall_generalRv);

            llt_overall.setOnClickListener(new ToDetailClickListener(getAdapterPosition(),context,null,null));
            llt_overall.setOnLongClickListener(new LongClickDeleteListener(context,getAdapterPosition()));
        }


        public TextView getTv_id() {
            return tv_id;
        }

        public TextView getTv_title() {
            return tv_title;
        }

        public TextView getTv_createTime() {
            return tv_createTime;
        }
    }

    public GeneralRvAdapter() {
    }



    public GeneralRvAdapter(ArrayList<T> models, Context context){
        this.dataList = models;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_general,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(GeneralRvAdapter.ViewHolder holder, int position) {
        singleModel = this.dataList.get(position);

//        holder.getMainSourceView().setRhythmViewData(singleModel);
        holder.getTv_id().setText(String.format(context.getResources().getString(R.string.plh_sharp_id), singleModel.getId()));
        holder.getTv_title().setText(singleModel.getTitle());
//        holder.getTv_createTime().setText(singleModel.getCreateTimeStr());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
