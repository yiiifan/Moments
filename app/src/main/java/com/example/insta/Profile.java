package com.example.insta;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG  = "Profile";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION = 3;
    private static final int PICK_IMAGE = 2;

    private String username;
    private String bio;
    private String avatar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    String mCurrentPhotoPath;

    private String uid;
    private Uri photoURI;

    private RecyclerView mRecycleView;
    private FloatingActionButton mMenu;
    private FloatingActionButton mCamera;
    private FloatingActionButton mFolder;
    private FloatingActionButton mLogout;

    LocationManager locationManager;
    String mLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);


        mMenu = findViewById(R.id.btn_menu);
        mCamera= findViewById(R.id.btn_camera);
        mFolder= findViewById(R.id.btn_folder);
        mLogout= findViewById(R.id.btn_logout);

        mCamera.hide();
        mFolder.hide();
        mLogout.hide();

        // Button views
        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_folder).setOnClickListener(this);
        findViewById(R.id.btn_logout).setOnClickListener(this);

        mRecycleView = findViewById(R.id.gallery);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        initRecyclerView();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        checkAuth();
    }

    private void checkAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            goWelcome();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_menu) {
            if(mCamera.isShown() && mFolder.isShown() && mLogout.isShown()){
                mCamera.hide();
                mFolder.hide();
                mLogout.hide();
            }else{
                mCamera.show();
                mFolder.show();
                mLogout.show();
            }

        }else if(i == R.id.btn_camera){
            dispatchTakePictureIntent();

        }else if(i == R.id.btn_folder){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);

        }else if(i == R.id.btn_logout){
            FirebaseAuth.getInstance().signOut();
            goWelcome();

        }
    }

    private void initRecyclerView() {

        //Download username and bio
        DocumentReference docRef = mDatabase.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        username = (String)document.get("username");
                        bio = (String)document.get("bio");
                        downloadAvatar();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void downloadAvatar() {

        // Download avatar from Storage
        String path = uid.concat("/avatar/avatar.png");
        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Log.d(TAG,"Got the download URL");
                avatar = uri.toString();
                downloadPhoto();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG,"Fail to got the download URL");
            }
        });
    }

    private void downloadPhoto() {

        final List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();

        // Download all photos
        mDatabase.collection("users").document(uid).collection("Photos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Header header = new Header(username, bio, avatar);
                            recyclerViewItems.add(header);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                photoModel model = new photoModel(document.getId(), document.getString("url"), document.getString("location"),document.getString("timestamp"),document.getString("desc"));
                                recyclerViewItems.add(model);
                            }

                            final PhotoRecycleView adapter = new PhotoRecycleView(Profile.this, recyclerViewItems);

                            GridLayoutManager mLayoutManager = new GridLayoutManager(Profile.this, 3);
                            mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                @Override
                                public int getSpanSize(int position) {
                                    if(adapter.getItemViewType(position) == 1){
                                        return 3;
                                    }else{
                                        return 1;
                                    }
                                }
                            });

                            mRecycleView.setLayoutManager(mLayoutManager);
                            mRecycleView.setAdapter(adapter);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


    // Upload a new photo from camera
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
            getLocation();
            Log.d(TAG, "Location is "+ mLocation);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Intent intent = new Intent(Profile.this, Preview.class);
            intent.putExtra("photoURI", photoURI.toString());
            intent.putExtra("location", mLocation);
            intent.putExtra("timestamp", timeStamp);
            startActivity(intent);
        }else if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri localFile = data.getData();
            getLocation();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Intent intent = new Intent(Profile.this, Preview.class);
            intent.putExtra("photoURI", localFile.toString());
            intent.putExtra("location", mLocation);
            intent.putExtra("timestamp", timeStamp);
            startActivity(intent);
        }
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            currLocation();
        }
    }

    private void currLocation() {
        String latitude, longitude;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                mLocation = latitude+","+longitude;

            } else  if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                mLocation = latitude+","+longitude;

            } else  if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                mLocation = latitude+","+longitude;


            }else{

                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


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

}

