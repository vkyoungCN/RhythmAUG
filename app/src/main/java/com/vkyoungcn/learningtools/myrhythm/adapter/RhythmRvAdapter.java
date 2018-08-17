package com.vkyoungcn.learningtools.myrhythm.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.RhythmDetailActivity;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineView;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;
import com.vkyoungcn.learningtools.myrhythm.R;

import java.util.ArrayList;

import static java.lang.String.valueOf;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
public class RhythmRvAdapter extends RecyclerView.Adapter<RhythmRvAdapter.ViewHolder>{
//* 是展示任务所属分组的RecyclerView所使用的适配器
// 采用纵向列表形式。
    private static final String TAG = "GroupsOfMissionRvAdapter";

    private ArrayList<CompoundRhythm> compoundRhythms;
//    private ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections;//数据源
//    private ArrayList<Lyric> lyrics_1;//数据源-词1
//    private ArrayList<Lyric> lyrics_2;//数据源-词2
//    private ArrayList<Integer> rhythmTypes;//

    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        private final RhythmSingleLineView rhv_singleLV;
        private final TextView tv_id;
        private final TextView tv_title;
        private final TextView tv_createTime;



        private ViewHolder(View itemView) {
            super(itemView);
            rhv_singleLV = itemView.findViewById(R.id.rhView_singleLine_MA);
            tv_id = itemView.findViewById(R.id.tv_rhId_MA);
            tv_title = itemView.findViewById(R.id.tv_title_MA);
            tv_createTime = itemView.findViewById(R.id.tv_createTime_MA);

            rhv_singleLV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rhView_singleLine_MA:
                    //进入详情页
//                    Toast.makeText(context, "详情页施工中", Toast.LENGTH_SHORT).show();
                    Intent intentToRDA = new Intent(context, RhythmDetailActivity.class);
                    intentToRDA.putExtra("RHYTHM",compoundRhythms.get(getAdapterPosition()));

                    context.startActivity(intentToRDA);
                    break;

            }
        }


        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.rhView_singleLine_MA:
                    //弹出删除确认DFG
//                    Toast.makeText(context, "详情页施工中", Toast.LENGTH_SHORT).show();
                    Intent intentToRDA = new Intent(context, RhythmDetailActivity.class);
                    intentToRDA.putExtra("RHYTHM_ID", compoundRhythms.get(getAdapterPosition()).getId());

                    context.startActivity(intentToRDA);
                    break;
            }
                return true;//如果返回false则其他方法需要继续处理本事件。
        }



        private RhythmSingleLineView getRhv_singleLV() {
            return rhv_singleLV;
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

    public RhythmRvAdapter(ArrayList<CompoundRhythm> compoundRhythms, Context context){
        this.compoundRhythms = compoundRhythms;
        this.context = context;
    }
    /*public RhythmRvAdapter(ArrayList<ArrayList<ArrayList<Byte>>> rhythmCodesInSections, ArrayList<Lyric> lyrics_1,ArrayList<Lyric> lyrics_2,ArrayList<Integer> rhythmType, Context context) {
        this.rhythmCodesInSections = rhythmCodesInSections;
        this.lyrics_1 = lyrics_1;
        this.lyrics_2 = lyrics_2;
        this.rhythmTypes = rhythmType;

        this.context = context;
    }*/

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_rhythm_of_main,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RhythmRvAdapter.ViewHolder holder, int position) {
        CompoundRhythm compoundRhythm = compoundRhythms.get(position);

        holder.getRhv_singleLV().setRhythmViewData(compoundRhythm);
        holder.getTv_id().setText(String.format(context.getResources().getString(R.string.plh_sharp_id),compoundRhythm.getId()));
        holder.getTv_title().setText(compoundRhythm.getTitle());
        holder.getTv_createTime().setText(compoundRhythm.getCreateTimeStr());

    }

    @Override
    public int getItemCount() {
        return compoundRhythms.size();
    }
}
