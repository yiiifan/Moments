package com.example.insta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Register";
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mUsernameField;
    private EditText mBioField;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;

    private String uid;

    public static final String username  = "Ivana";
    public static final String bio = "Hello world!";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageButton button;
    private byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Views
        mEmailField = findViewById(R.id.email);
        mPasswordField = findViewById(R.id.password);
        mUsernameField = findViewById(R.id.username);
        mBioField = findViewById(R.id.bio);

        //Buttons
        findViewById(R.id.signup).setOnClickListener(this);
        button = findViewById(R.id.img);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.signup){
            Log.d(TAG,"onclick");
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
//        if (!validateForm()) {
//            return;
//        }

//        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            uid = user.getUid();
                            saveInfo(mUsernameField.getText().toString(),mBioField.getText().toString());
                            goProfile();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

//                        // [START_EXCLUDE]
//                        hideProgressDialog();
//                        // [END_EXCLUDE]
                    }


                });
    }

    private void saveInfo(String username, String bio) {

        // Save username and bio in Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDatabase.setFirestoreSettings(settings);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("bio", bio);

        mDatabase.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });


        // Save avatar in storage
        String path = uid.concat("/avatar/avatar.png");

        StorageReference storageRef = mStorage.getReference();
        StorageReference avatarRef = storageRef.child(path);

        UploadTask uploadTask = avatarRef.putBytes(byteArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.w(TAG, "Error adding document", exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload avatar successfully");

            }
        });
    }

    private void goProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }


    public void profile(View view) {
        Intent intent = new Intent(this, Profile.class);
        String username_string = mUsernameField.getText().toString();
        String bio_string = mBioField.getText().toString();
        if(byteArray == null){
            showToast(view, "Please upload your photo");
        }else if(username_string.length() == 0){
            showToast(view, "Please fill your username");
        }else if(bio_string.length() == 0){
            showToast(view, "Please fill your short bio");
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
