package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.GroupDetailActivity;
import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.LongClickMultiFunctionListener;
import com.vkyoungcn.learningtools.myrhythm.helper.ToDetailClickListener;
import com.vkyoungcn.learningtools.myrhythm.models.Group;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_GROUP;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.24
 * */
public class GroupRvAdapter extends RecyclerView.Adapter<GroupRvAdapter.ViewHolder>{
    //* 是展示任务所属分组的RecyclerView所使用的适配器
    static final String TAG = "GroupRvAdapter";
    ArrayList<Group> dataList;
    Context context;
    Group singleModel;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_id;
        private final TextView tv_title;
        private final TextView tv_createTime;
        private final LinearLayout llt_overall;
        private final TextView tv_description; //主要就是这个不同的控件很难在继承中处理

        private ViewHolder(View itemView) {
            super(itemView);

            tv_description = itemView.findViewById(R.id.tv_description_RVG);
            tv_id = itemView.findViewById(R.id.tv_rhId_RVG);
            tv_title = itemView.findViewById(R.id.tv_title_RVG);
            tv_createTime = itemView.findViewById(R.id.tv_createTime_RVG);
            llt_overall = itemView.findViewById(R.id.llt_overall_RVG);

            llt_overall.setOnClickListener(new ToDetailClickListener(dataList.get(getAdapterPosition()),context, GroupDetailActivity.class));
            llt_overall.setOnLongClickListener(new LongClickMultiFunctionListener(context,dataList.get(getAdapterPosition()).getId(),DELETE_GROUP));
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

        public TextView getTv_description() {
            return tv_description;
        }
    }

    public GroupRvAdapter() {
    }



    public GroupRvAdapter(ArrayList<Group> models, Context context){
        this.dataList = models;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_group,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(GroupRvAdapter.ViewHolder holder, int position) {
        singleModel = this.dataList.get(position);

//        holder.getMainSourceView().setRhythmViewData(singleModel);
        holder.getTv_id().setText(String.format(context.getResources().getString(R.string.plh_sharp_id), singleModel.getId()));
        holder.getTv_title().setText(singleModel.getTitle());
        holder.getTv_description().setText(singleModel.getDescription());
//        holder.getTv_createTime().setText(singleModel.getCreateTimeStr());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
