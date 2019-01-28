package com.example.insta;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PhotoRecycleView extends RecyclerView.Adapter<PhotoRecycleView.MyViewHolder>{

    private Context mContext;
    private List<photoModel> mData;

    public PhotoRecycleView(Context mContext, List<photoModel> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Glide.with(mContext)
                .load(mData.get(i).getUrl())
                .into(myViewHolder.photo_download);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView photo_download;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photo_download = (ImageView) itemView.findViewById(R.id.photo_card);
        }
    }
}
