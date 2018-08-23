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
import com.vkyoungcn.learningtools.myrhythm.models.BaseModel;
import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;

import org.w3c.dom.Text;

import java.util.ArrayList;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.23
 * */
public class GroupRvAdapter<T extends BaseModel> extends GeneralRvAdapter{
//* 是展示任务所属分组的RecyclerView所使用的适配器
// 采用纵向列表形式。


    @Override
    void toDetailActivity() {
        super.toDetailActivity();
        //跳到专用详情页面

    }


    public GroupRvAdapter() {
    }

    public GroupRvAdapter(ArrayList<Group> groups, Context context){
        this.dataList = groups;
        this.context = context;
    }



    @Override
    public void onBindViewHolder(GroupRvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        //Group没有字串时间类，不显示（如果现场转换太费时间）
        //在一般显示编码的区域，显示描述字串。
        ((TextView)(holder.getMainModelView())).setText(singleModel.getDescription());

    }

}
