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
import com.vkyoungcn.learningtools.myrhythm.fragments.ChooseRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmLiteForGpX;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_RHYTHM;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.23
 * */
public class RhythmLiteRvAdapter extends RecyclerView.Adapter<RhythmLiteRvAdapter.ViewHolder>{
//用于为分组选定节奏资源（只显示id、title、描述三字段，不加载rhView这种复杂UI）
    static final String TAG = "RhythmLiteRvAdapter";
    ArrayList<RhythmLiteForGpX> dataList;
//    Context context;
    ChooseRhythmDiaFragment dfg;//持有dfg引用以便交互数据【方案是否存在弊端？】
    RhythmLiteForGpX singleModel;
    boolean isClickingToAdd = true;//点击单项是增加还是移除

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_id;
        private final TextView tv_title;
        private final TextView tv_description;

        //        private final TextView tv_createTime;
        private final LinearLayout llt_overall;
//        private final RhythmSingleLineView mainModelView;

        private ViewHolder(View itemView) {
            super(itemView);

            tv_id = itemView.findViewById(R.id.tv_rhId_RHLT);
            tv_title = itemView.findViewById(R.id.tv_title_RHLT);
            tv_description = itemView.findViewById(R.id.tv_description_RHLT);
            llt_overall = itemView.findViewById(R.id.llt_overall_RHLT);
            llt_overall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdapterMethodsHelper.makeChoice(dfg,dataList.get(getAdapterPosition()),isClickingToAdd);
                }
            });
//            mainModelView = itemView.findViewById(R.id.rhView_singleLine);
//            tv_createTime = itemView.findViewById(R.id.tv_createTime);
//            llt_overall.setOnLongClickListener(new View.OnLongClickListener() {【不需长按】
//                @Override
//                public boolean onLongClick(View v) {
//                    AdapterMethodsHelper.longClickingDelete(context,dataList.get(getAdapterPosition()).getId(),DELETE_RHYTHM);
//
//                    return true;
//                }
//            });
//            llt_overall.setOnLongClickListener(new LongClickMultiFunctionListener(context,dataList.get(getAdapterPosition()).getId(),MODEL_TYPE_RH));
        }


        public TextView getTv_id() {
            return tv_id;
        }

        public TextView getTv_title() {
            return tv_title;
        }

        public TextView getTv_description() {
            return tv_description;
        }
        //        public TextView getTv_createTime() {
//            return tv_createTime;
//        }

//        public RhythmSingleLineView getMainModelView() {
//            return mainModelView;
//        }
    }

    public RhythmLiteRvAdapter() {
    }



    public RhythmLiteRvAdapter(ChooseRhythmDiaFragment dfg, ArrayList<RhythmLiteForGpX> models, boolean isClickingToAdd ){
        this.dfg = dfg;
        this.dataList = models;
//        this.context = context;
        this.isClickingToAdd = isClickingToAdd;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_rhythm_lite,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RhythmLiteRvAdapter.ViewHolder holder, int position) {
        singleModel = this.dataList.get(position);

//        holder.getMainSourceView().setRhythmViewData(singleModel);
        holder.getTv_id().setText(String.format(dfg.getContext().getResources().getString(R.string.plh_sharp_id), singleModel.getId()));
        holder.getTv_title().setText(singleModel.getTitle());
        holder.getTv_description().setText(singleModel.getDescription());

        //        holder.getMainModelView().setRhythmViewData(singleModel);
//        holder.getTv_createTime().setText(singleModel.getCreateTimeStr());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
