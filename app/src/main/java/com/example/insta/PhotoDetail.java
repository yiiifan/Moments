package com.example.insta;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PhotoDetail extends AppCompatActivity {

    private TextView desc_view;
    private TextView info_view;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        img = findViewById(R.id.detail_view);
        desc_view = findViewById(R.id.detail_desc);
        info_view = findViewById(R.id.detail_info);

        //Receive data
        Intent intent = getIntent();

        String PhotoUrl = intent.getExtras().getString("PhotoUrl");
        Uri uri = Uri.parse(PhotoUrl);
        String desc = intent.getExtras().getString("desc");
        String location = intent.getExtras().getString("location");
        String timestamp = intent.getExtras().getString("timestamp");

        Glide.with(this)
                .load(uri)
                .into(img);
        desc_view.setText(desc);
        info_view.setText(timestamp + "/"+location);

    }
}
