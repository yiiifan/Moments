package com.example.insta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        String username = intent.getStringExtra(Register.username);
        TextView username_view = findViewById(R.id.username);
        username_view.setText(username);

        String bio = intent.getStringExtra(Register.bio);
        TextView bio_view = findViewById(R.id.bio);
        bio_view.setText(bio);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ImageView image = findViewById(R.id.img);
        image.setImageBitmap(bitmap);
    }
}
