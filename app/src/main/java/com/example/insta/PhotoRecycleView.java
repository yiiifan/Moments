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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.util.List;


public class PhotoRecycleView extends RecyclerView.Adapter{

    private static final int HEADER_ITEM = 1;
    private static final int PHOTO_ITEM = 2;
    private static final int GLOBAL_ITEM = 3;

    //Declare List of Recycleview Items
    private List<RecyclerViewItem> recyclerViewItems;

    private Context mContext;

    PhotoRecycleView(Context mContext, List<RecyclerViewItem> recyclerViewItems) {
        this.mContext = mContext;
        this.recyclerViewItems = recyclerViewItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view;
        if(viewType == HEADER_ITEM){
            view = mInflater.inflate(R.layout.header, parent, false);
            return new HeaderHolder(view);

        }else if(viewType == PHOTO_ITEM) {
            view = mInflater.inflate(R.layout.item, parent, false);
            return new PhotoHolder(view);

        }else if(viewType == GLOBAL_ITEM){
            view = mInflater.inflate(R.layout.global_item, parent, false);
            return new GlobalHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);

        if(holder instanceof HeaderHolder) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            Header header = (Header) recyclerViewItem;

            headerHolder.profile_username.setText(header.getProfile_username());
            headerHolder.profile_bio.setText(header.getProfile_bio());

            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.circleCrop();

            Glide.with(mContext)
                    .load(header.getProfile_avatar())
                    .apply(options)
                    .into(headerHolder.profile_avatar);

        }else if(holder instanceof PhotoHolder){

            final PhotoHolder photoHolder = (PhotoHolder) holder;
            final photoModel photo = (photoModel) recyclerViewItem;

            Glide.with(mContext)
                    .load(photo.getUrl())
                    .into(photoHolder.photo_download);

            //Set click listener
            photoHolder.photo_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    photoHolder.photo_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(mContext, Comments.class);
                            intent.putExtra("author",photo.getuID());
                            intent.putExtra("pID",photo.getpID());
                            intent.putExtra("Url",photo.getUrl().toString());
                            intent.putExtra("caption",photo.getDescription());
                            intent.putExtra("timestamp",photo.getTimestamp());
                            intent.putExtra("location", photo.getLocation());

                            mContext.startActivity(intent);

                        }
                    });
                }
            });

        }else if(holder instanceof GlobalHolder){
            final GlobalHolder globalHolder = (GlobalHolder) holder;
            final GlobalPhotoHolder photo = (GlobalPhotoHolder) recyclerViewItem;

            Glide.with(mContext)
                    .load(photo.getUrl())
                    .into(globalHolder.photo_download);

            globalHolder.photo_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mContext, Comments.class);
                    intent.putExtra("author",photo.getuID());
                    intent.putExtra("pID",photo.getpID());
                    intent.putExtra("Url",photo.getUrl());
                    intent.putExtra("caption",photo.getDescription());
                    intent.putExtra("timestamp",photo.getTimestamp());
                    intent.putExtra("location", photo.getLocation());

                    mContext.startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return recyclerViewItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);

        if (recyclerViewItem instanceof Header)
            return HEADER_ITEM;
        else if (recyclerViewItem instanceof photoModel)
            return PHOTO_ITEM;
        else if(recyclerViewItem instanceof GlobalPhotoHolder)
            return GLOBAL_ITEM;
        else
            return super.getItemViewType(position);
    }


    public static class PhotoHolder extends RecyclerView.ViewHolder{

        // Recycle Photo list
        ImageView photo_download;
        CardView photo_item;

        PhotoHolder(@NonNull View itemView) {
            super(itemView);

            //Get the view from item
            photo_download = itemView.findViewById(R.id.photo_card);
            photo_item = itemView.findViewById(R.id.photo_item);
        }
    }

    public static class GlobalHolder extends RecyclerView.ViewHolder{

        // Recycle Photo list
        ImageView photo_download;
        CardView photo_item;

        GlobalHolder(@NonNull View itemView) {
            super(itemView);

            //Get the view from item
            photo_download = itemView.findViewById(R.id.photo_card);
            photo_item = itemView.findViewById(R.id.photo_item);
        }
    }


    //header holder
    public class HeaderHolder extends RecyclerView.ViewHolder {
        CardView profile;
        TextView profile_username, profile_bio;
        ImageView profile_avatar;

        HeaderHolder(View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile);
            profile_avatar = itemView.findViewById(R.id.profile_avatar);
            profile_username = itemView.findViewById(R.id.profile_username);
            profile_bio = itemView.findViewById(R.id.profile_bio);
        }
    }


}
