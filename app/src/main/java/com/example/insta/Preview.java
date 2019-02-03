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
import android.widget.EditText;
import android.widget.ImageView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class Preview extends AppCompatActivity {
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

        mPost = findViewById(R.id.preview_post);
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = mDesc.getText().toString();
                uploadPhoto(mUID, photoUri, location, timestamp, description);
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

    private void uploadPhoto(String uid, Uri photoURI, final String location, final String timestamp, String post) {
        String imgpath = uid.concat("/Photos");
        final StorageReference ref = mStorageRef.child(imgpath).child(photoURI.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(photoURI);

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
                    savePhotoInfo(downloadUri, description, location, timestamp);
                } else {

                }
            }
        });
    }

    private void savePhotoInfo(Uri imgURL, String desc, String mLocation, String timestamp) {

        Map<String, Object> photo = new HashMap<>();
        photo.put("url", imgURL.toString());
        photo.put("desc", desc);
        photo.put("location", mLocation);
        photo.put("timestamp", timestamp);

        String path = "users/" + mUID + "/Photos";

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
}
