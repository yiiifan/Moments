package com.example.insta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class PhotoRecycleView extends RecyclerView.Adapter{
    private static final String TAG = "Recycle";

    private static final int HEADER_ITEM = 1;
    private static final int PHOTO_ITEM = 2;
    private static final int FOOTER_VIEW = 3;

    //Declare List of Recycleview Items
    private List<RecyclerViewItem> recyclerViewItems;

    private Context mContext;
    private List<photoModel> mData;

    public PhotoRecycleView(Context mContext, List<RecyclerViewItem> recyclerViewItems) {
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

            PhotoHolder PhotoHolder = (PhotoHolder) holder;
            final photoModel photo = (photoModel) recyclerViewItem;

            Glide.with(mContext)
                    .load(photo.getUrl())
                    .into(PhotoHolder.photo_download);

            //Set click listener
            PhotoHolder.photo_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog PhotoDetail = new Dialog(mContext);
                    PhotoDetail.setContentView(R.layout.popup_detail);
                    ImageView photoView = (ImageView) PhotoDetail.findViewById(R.id.detail_view);
                    TextView descView = (TextView) PhotoDetail.findViewById(R.id.detail_desc);
                    TextView timeView = (TextView) PhotoDetail.findViewById(R.id.detail_time);
                    TextView locationView = (TextView) PhotoDetail.findViewById(R.id.detail_location);

                    Glide.with(mContext)
                            .load(photo.getUrl())
                            .into(photoView);

                    PhotoDetail.show();
                    descView.setText(photo.getDescription());
                    timeView.setText(timeformat(photo.getTimestamp()));
                    locationView.setText(locationformat(photo.getLocation()));

                    TextView exit = (TextView) PhotoDetail.findViewById(R.id.detail_exit);
                    exit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PhotoDetail.dismiss();
                        }
                    });

                }
            });
        }
    }

    private String locationformat(String location){
        return "Location: "+ location;
    }

    private String timeformat(String description) {
        String year = description.substring(0,4);
        String month = description.substring(4,6);
        String day = description.substring(6,8);
        String hour = description.substring(9,11);
        String minute = description.substring(11,13);
        String second = description.substring(13,15);

        return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
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
            photo_download = (ImageView) itemView.findViewById(R.id.photo_card);
            photo_item = (CardView) itemView.findViewById(R.id.photo_item);
        }
    }

    //header holder
    private class HeaderHolder extends RecyclerView.ViewHolder {
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
