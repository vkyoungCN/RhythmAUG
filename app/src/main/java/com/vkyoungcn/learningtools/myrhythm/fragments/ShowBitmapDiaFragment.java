package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vkyoungcn.learningtools.myrhythm.R;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.01
 * */
@SuppressWarnings("all")
public class ShowBitmapDiaFragment extends DialogFragment
        implements View.OnClickListener {
    private Bitmap bitmap;
//    private int deleteType = 0;

//    private OnGeneralDfgInteraction mListener;

    public ShowBitmapDiaFragment() {
        // Required empty public constructor
    }


    public static ShowBitmapDiaFragment newInstance(Bitmap bitmap) {
        ShowBitmapDiaFragment fragment = new ShowBitmapDiaFragment();
        Bundle args = new Bundle();
        args.putParcelable("BITMAP",bitmap);
//        args.putInt("DELETE_TYPE",deleteModelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bitmap = getArguments().getParcelable("BITMAP");
//            deleteType = getArguments().getInt("DELETE_TYPE");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dfg_show_bitmap, container, false);

        ImageView imv_ShowBM = (ImageView) rootView.findViewById(R.id.imv_showBitmap_DSB);
        imv_ShowBM.setImageBitmap(bitmap);
//        TextView cancel = (TextView) rootView.findViewById(R.id.btn_cancel_DSB);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_DSB);

        //部分需要添加事件监听
        confirm.setOnClickListener(this);
//        cancel.setOnClickListener(this);

        return rootView;
    }



    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralDfgInteraction) {
            mListener = (OnGeneralDfgInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGeneralDfgInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

*/
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok_DSB:
                this.dismiss();
                break;


/*
            case R.id.btn_cancel_DSB:
                this.dismiss();
                break;

*/
        }

    }
}
