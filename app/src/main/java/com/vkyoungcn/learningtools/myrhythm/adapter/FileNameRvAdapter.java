package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.fragments.ChooseRhythmDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.fragments.SelectFileNameDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmLiteForGpX;

import java.util.ArrayList;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.23
 * */
public class FileNameRvAdapter extends RecyclerView.Adapter<FileNameRvAdapter.ViewHolder>{
    ArrayList<String> dataList;
    SelectFileNameDiaFragment dfg;//持有dfg引用以便交互数据【方案是否存在弊端？】

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_file;

       private final LinearLayout llt_overall;

        private ViewHolder(View itemView) {
            super(itemView);

            tv_file = itemView.findViewById(R.id.tv_title_FSRV);
            llt_overall = itemView.findViewById(R.id.llt_overall_FSRV);
            llt_overall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdapterMethodsHelper.makeChoice(dfg,dataList.get(getAdapterPosition()));
                }
            });
        }

        public TextView getTv_file() {
            return tv_file;
        }

    }

    public FileNameRvAdapter() {
    }



    public FileNameRvAdapter(SelectFileNameDiaFragment dfg, ArrayList<String> models){
        this.dfg = dfg;
        this.dataList = models;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_file_name,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(FileNameRvAdapter.ViewHolder holder, int position) {

        holder.getTv_file().setText(dataList.get(position));

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
