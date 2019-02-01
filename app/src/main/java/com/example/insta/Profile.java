package com.example.insta;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG  = "Profile";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int CAMERA_REQUEST_CODE = 1;

    private TextView mUsernameField;
    private TextView mBioField;
    private ImageView mAvatar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    String mCurrentPhotoPath;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    private String uid;
    private Uri photoURI;

    private RecyclerView mRecycleView;
    private FirestoreRecyclerAdapter mAdapter;
    LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Views
        mUsernameField = findViewById(R.id.username);
        mBioField = findViewById(R.id.bio);
        mAvatar = findViewById(R.id.img);

        mRecycleView = findViewById(R.id.gallery);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ButterKnife.bind(this);

        //Buttons
        findViewById(R.id.logout).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        showProfile();
        loadGallery();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            goWelcome();
        }else if(i == R.id.add){
            dispatchTakePictureIntent();
        }
    }

    public void loadGallery(){
        mDatabase.collection("users").document(uid).collection("Photos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final ArrayList<photoModel> photoList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                photoModel model = new photoModel(document.getId(), document.getString("url"), document.getString("location"),document.getString("timestamp"));
                                photoList.add(model);
                            }
                            Log.d(TAG,"photolist size: "+photoList.size());
                            PhotoRecycleView myAdapter = new PhotoRecycleView(Profile.this, photoList);
                            mRecycleView.setLayoutManager(new GridLayoutManager(Profile.this, 3));

                            mRecycleView.setAdapter(myAdapter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
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
                        Uri downloadUri = task.getResult();
                        savePhotoInfo(task.getResult(), mLocation);
                        Log.d(TAG, "download url" + downloadUri);

                    } else {

                    }
                }
            });
        }
    }

    private void savePhotoInfo(Uri imgURL, Location mLocation) {

        Map<String, Object> photo = new HashMap<>();
        photo.put("url", imgURL.toString());
        // TODO: fetch the location
        photo.put("location", "fetch the location");

        String path = "users/" + uid + "/Photos";

        mDatabase.collection(path)
                .add(photo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }


//    private void fetchLocation(){
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                            // Logic to handle location object
//                            mLocation = location;
//                            Log.d(TAG,"location: "+location);
//                        }
//                    }
//                });
//    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void goWelcome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private void showProfile() {
        // Download document from Firebase
        DocumentReference docRef = mDatabase.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        mUsernameField.setText((String)document.get("username"));
                        mBioField.setText((String)document.get("bio"));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // Download avatar from Storage
        String path = uid.concat("/avatar/avatar.png");
        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Log.d(TAG,"Got the download URL");
                RequestOptions options = new RequestOptions();
                options.centerCrop();
                options.circleCrop();

                Glide
                        .with(getApplicationContext())
                        .load(uri)
                        .apply(options)
                        .into(mAvatar);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG,"Fail to got the download URL");
            }
        });

    }


}

