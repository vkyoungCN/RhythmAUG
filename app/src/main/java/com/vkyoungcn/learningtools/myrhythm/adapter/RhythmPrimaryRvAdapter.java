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

    private ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections;//数据源
    private ArrayList<Lyric> lyrics_1;//数据源-词1
    private ArrayList<Lyric> lyrics_2;//数据源-词2
    private ArrayList<Integer> rhythmTypes;//

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

    public RhythmPrimaryRvAdapter(ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections, ArrayList<Lyric> lyrics_1,ArrayList<Lyric> lyrics_2,ArrayList<Integer> rhythmType, Context context) {
        this.rhythmCodesInSections = rhythmCodesInSections;
        this.lyrics_1 = lyrics_1;
        this.lyrics_2 = lyrics_2;
        this.rhythmTypes = rhythmType;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_rhythm_of_main,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RhythmPrimaryRvAdapter.ViewHolder holder, int position) {
        ArrayList<ArrayList<Byte>> codeInSections = rhythmCodesInSections.get(position);
        int rhythmType = rhythmTypes.get(position);
        Lyric lyric_1 = new Lyric();
        Lyric lyric_2 = new Lyric();
        if(lyrics_1!=null) {
            lyric_1 = lyrics_1.get(position);
        }
        if(lyrics_2!=null){
            lyric_2 = lyrics_2.get(position);
        }
        holder.getRhv_singleLV().setRhythmAndLyric(codeInSections,rhythmType,lyric_1,lyric_2,16,18);

    }

    @Override
    public int getItemCount() {
        return rhythmCodesInSections.size();
    }
}
