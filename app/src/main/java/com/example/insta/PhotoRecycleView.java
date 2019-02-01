package com.example.insta;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
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
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int position) {
        Glide.with(mContext)
                .load(mData.get(position).getUrl())
                .into(myViewHolder.photo_download);

        //Set click listener
        myViewHolder.photo_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PhotoDetail.class);

                // Passing data to the Detail activity
                intent.putExtra("PhotoUrl", mData.get(position).getUrl().toString());
                intent.putExtra("desc", "Photo information");
                intent.putExtra("location", mData.get(position).getLocation());
                intent.putExtra("timestamp", mData.get(position).getTimestamp());

                // Start the activity
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView photo_download;
        CardView photo_item;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photo_download = (ImageView) itemView.findViewById(R.id.photo_card);
            photo_item = (CardView) itemView.findViewById(R.id.photo_item);
        }
    }
}
