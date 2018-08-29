package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.helper.LongClickMultiFunctionListener;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction.DELETE_LYRIC;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.29
 * */
public class LyricFreeRvAdapter extends RecyclerView.Adapter<LyricFreeRvAdapter.ViewHolder>{
    static final String TAG = "LyricFreeRvAdapter";
    Context context;
    ArrayList<Lyric> dataList;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_description;

        private ViewHolder(View itemView) {
            super(itemView);

            tv_description = itemView.findViewById(R.id.tv_description_RVL);
//            llt_overall.setOnClickListener(new ToDetailClickListener(getAdapterPosition(),context, LyricDetailActivity.class));
            tv_description.setOnLongClickListener(new LongClickMultiFunctionListener(context,dataList.get(getAdapterPosition()).getId(),DELETE_LYRIC));
        }


        public TextView getTv_description() {
            return tv_description;
        }
    }

    public LyricFreeRvAdapter() {
    }



    public LyricFreeRvAdapter(ArrayList<Lyric> dataList, Context context){
        this.dataList = dataList;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_lyric_free,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(LyricFreeRvAdapter.ViewHolder holder, int position) {
        String string = this.dataList.get(position).getCodeSerialString();

        holder.getTv_description().setText(string);

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
