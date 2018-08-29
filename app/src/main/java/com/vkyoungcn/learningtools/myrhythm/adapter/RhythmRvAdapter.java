package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.RhythmDetailActivity;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineView;
import com.vkyoungcn.learningtools.myrhythm.helper.LongClickMultiFunctionListener;
import com.vkyoungcn.learningtools.myrhythm.helper.ToDetailClickListener;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_RHYTHM;
import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.MODEL_TYPE_RH;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.23
 * */
public class RhythmRvAdapter extends RecyclerView.Adapter<RhythmRvAdapter.ViewHolder>{
//这个东西很难设计为继承啊？！尝试一天，失败告终（仅剩的成果是把监听独立了出去）
    //* 是展示任务所属分组的RecyclerView所使用的适配器
// 采用纵向列表形式。
    static final String TAG = "RhythmRvAdapter";
    ArrayList<RhythmBasedCompound> dataList;
    Context context;
    RhythmBasedCompound singleModel;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_id;
        private final TextView tv_title;
        private final TextView tv_createTime;
        private final LinearLayout llt_overall;
        private final RhythmSingleLineView mainModelView; //主要就是这个很难处理，VH这种内部类如何继承是个问题（暂未掌握，或者根本没法继承）

        private ViewHolder(View itemView) {
            super(itemView);

            mainModelView = itemView.findViewById(R.id.rhView_singleLine);
            tv_id = itemView.findViewById(R.id.tv_rhId);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_createTime = itemView.findViewById(R.id.tv_createTime);
            llt_overall = itemView.findViewById(R.id.llt_overall);

            llt_overall.setOnClickListener(new ToDetailClickListener(dataList.get(getAdapterPosition()),context, RhythmDetailActivity.class));
            llt_overall.setOnLongClickListener(new LongClickMultiFunctionListener(context,dataList.get(getAdapterPosition()).getId(),MODEL_TYPE_RH));
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

        public RhythmSingleLineView getMainModelView() {
            return mainModelView;
        }
    }

    public RhythmRvAdapter() {
    }



    public RhythmRvAdapter(ArrayList<RhythmBasedCompound> models, Context context){
        this.dataList = models;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_rhythm,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RhythmRvAdapter.ViewHolder holder, int position) {
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
