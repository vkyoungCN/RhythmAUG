package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineView;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.R;

import java.util.ArrayList;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
public class RhythmPrimaryRvAdapter extends RecyclerView.Adapter<RhythmPrimaryRvAdapter.ViewHolder>{
//* 是展示任务所属分组的RecyclerView所使用的适配器
// 采用纵向列表形式。
    private static final String TAG = "GroupsOfMissionRvAdapter";

    private ArrayList<Rhythm> rhythms;//数据源1
    private ArrayList<Lyric> lyrics;//数据源2

    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final RhythmSingleLineView rhv_singleLV;


        private ViewHolder(View itemView) {
            super(itemView);
            rhv_singleLV = itemView.findViewById(R.id.rhView_singleLine_MA);
            rhv_singleLV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rhView_singleLine_MA:
                    //进入详情页
                    Toast.makeText(context, "详情页施工中", Toast.LENGTH_SHORT).show();
                    break;

            }
        }


        private RhythmSingleLineView getRhv_singleLV() {
            return rhv_singleLV;
        }

    }

    public RhythmPrimaryRvAdapter(ArrayList<Rhythm> rhythms,ArrayList<Lyric> lyrics, Context context) {
        this.rhythms = rhythms;
        this.lyrics = lyrics;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_rhythm_of_main,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RhythmPrimaryRvAdapter.ViewHolder holder, int position) {
        Rhythm rhythm = rhythms.get(position);
        Lyric lyric = lyrics.get(position);
        holder.getRhv_singleLV().setRhythmAndLyric(rhythm,lyric,14,20);

    }

    @Override
    public int getItemCount() {
        return rhythms.size();
    }
}
