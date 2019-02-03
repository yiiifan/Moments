package com.example.insta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
    private EditText mComfirmField;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;

    private String uid;
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
        mComfirmField = findViewById(R.id.password_confirm);
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
    public void onStart() {
        super.onStart();
        checkAuth();
    }

    private void checkAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            goProfile();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.signup){
            createAccount(v, mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    private void createAccount(View v, String email, String password) {
        if (!validateForm(v)) {
            return;
        }
        Log.d(TAG, "createAccount:" + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            uid = user.getUid();
                            saveInfo(mUsernameField.getText().toString(),mBioField.getText().toString());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }


                });
    }

    private void saveInfo(final String username, final String bio) {

        // Save avatar in storage
        String path = uid.concat("/avatar/avatar.png");

        StorageReference storageRef = mStorage.getReference();
        final StorageReference avatarRef = storageRef.child(path);

        avatarRef.putBytes(byteArray)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    saveFirestore(downloadUrl, username, bio);

                                }
                            });

                        }else{
                            Toast.makeText(Register.this,"Your picture did NOT saved",Toast.LENGTH_SHORT) .show();
                        }
                    }
                });
    }

    private void saveFirestore(String downloadUrl, String username, String bio) {

        // Save username and bio in Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDatabase.setFirestoreSettings(settings);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("bio", bio);
        user.put("avatar", downloadUrl);

        mDatabase.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        goProfile();

                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

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
            assert extras != null;
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap scaleBitmap = Bitmap.createScaledBitmap(imageBitmap, 350, 350, true);
            button.setImageBitmap(scaleBitmap);

            //Convert to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
        }
    }

    private void goProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    private boolean validateForm(View v) {
        boolean valid = true;

        if(byteArray == null){
            showToast(v, "Avatar is required");
            valid = false;
        }

        String mEmail = mEmailField.getText().toString();
        if (TextUtils.isEmpty(mEmail)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String mPassword = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String mComfirm = mComfirmField.getText().toString();
        if (!mPassword.equals(mComfirm)) {
            mComfirmField.setError("Not match.");
            valid = false;
        } else {
            mComfirmField.setError(null);
        }

        String mUsername = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameField.setError("Required.");
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        String mBio = mBioField.getText().toString();
        if (TextUtils.isEmpty(mBio)) {
            mBioField.setError("Required.");
            valid = false;
        } else {
            mBioField.setError(null);
        }

        return valid;
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
