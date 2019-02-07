package com.example.insta;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preview extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    final private String TAG = "Preview";

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private FirebaseFirestore mDatabase;

    private Uri photoUri;
    private Uri downloadUri;
    private String location;
    private String timestamp;

    private ImageView mPhoto;
    private EditText mDesc;
    private Button mPost;
    private Button mCancel;
    private Switch mSwitch;

    private String mUID;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Views
        mPhoto = findViewById(R.id.preview_photo);
        mDesc = findViewById(R.id.preview_desc);

        Intent intent = getIntent();
        photoUri = Uri.parse(intent.getExtras().getString("photoURI"));
        location = intent.getExtras().getString("location");
        timestamp = intent.getExtras().getString("timestamp");

        Glide.with(this)
                .load(photoUri)
                .into(mPhoto);

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        mUID = user.getUid();

        mSwitch = findViewById(R.id.auto_caption);
        mSwitch.setOnCheckedChangeListener(this);

        mPost = findViewById(R.id.preview_post);
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = mDesc.getText().toString();
                uploadPhoto();
            }
        });

        mCancel = findViewById(R.id.preview_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Preview.this, Profile.class);
                startActivity(intent);

            }
        });
    }

    private void uploadPhoto() {
        String imgpath = mUID.concat("/Photos");
        final StorageReference ref = mStorageRef.child(imgpath).child(photoUri.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(photoUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    savePhotoInfo();
                } else {

                }
            }
        });
    }

    private void savePhotoInfo() {

        Map<String, Object> photo = new HashMap<>();
        photo.put("uID", mUID);
        photo.put("url", downloadUri.toString());
        photo.put("desc", description);
        photo.put("location", location);
        photo.put("timestamp", timestamp);

        String path = "photos";

        mDatabase.collection(path)
                .add(photo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Intent intent = new Intent(Preview.this, Profile.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.auto_caption:
                if(buttonView.isChecked()){
                    Toast.makeText(this,"开关:ON",Toast.LENGTH_SHORT).show();
                    FirebaseVisionImage image;
                    try {
                        image = FirebaseVisionImage.fromFilePath(this, photoUri);

                        // set the minimum confidence required:
                        FirebaseVisionOnDeviceImageLabelerOptions options =
                                new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                                        .setConfidenceThreshold(0.7f)
                                        .build();
                        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                                .getOnDeviceImageLabeler(options);

                        labeler.processImage(image)
                                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                        for (FirebaseVisionImageLabel label: labels) {
                                            String labelText = "#"+label.getText()+"#";
                                            description = mDesc.getText().toString();
                                            mDesc.setText(description.concat(labelText));
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(this,"开关:OFF",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
