package com.example.insta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Register extends AppCompatActivity {
    public static final String username  = "Ivana";
    public static final String bio = "Hello world!";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText username_view;
    private EditText bio_view;
    private ImageButton button;
    private byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username_view = findViewById(R.id.username);
        bio_view = findViewById(R.id.bio);
        button = findViewById(R.id.img);
    }

    public void profile(View view) {
        Intent intent = new Intent(this, Profile.class);
        String username_string = username_view.getText().toString();
        String bio_string = bio_view.getText().toString();
        if(byteArray == null){
            showToast(view, "Please upload your photo!");
        }else if(username_string.length() == 0){
            showToast(view, "Please fill your username!");
        }else if(bio_string.length() == 0){
            showToast(view, "Please fill your short bio!");
        }else {
            intent.putExtra(username, username_string);
            intent.putExtra(bio, bio_string);
            intent.putExtra("image", byteArray);
            startActivity(intent);
        }
    }

    public void camera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            button.setImageBitmap(imageBitmap);

            //Convert to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
        }
    }

    public void showToast(View view, String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(getApplicationContext());
        imageCodeProject.setImageResource(R.drawable.ic_launcher_foreground);
        toastView.addView(imageCodeProject, 0);
        toast.show();
    }

}
