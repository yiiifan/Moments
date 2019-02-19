package com.example.insta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Register";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mUsernameField;
    private EditText mBioField;
    private EditText mComfirmField;
    private ImageButton button;
    private ProgressBar mProgress;
    private byte[] byteArray;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;

    private String uid;

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
        mProgress = findViewById(R.id.reg_progress);
        mProgress.setVisibility(View.GONE);

        //Firebase
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
            mProgress.setVisibility(View.VISIBLE);
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());

        }
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            mProgress.setVisibility(View.GONE);
            return;
        }

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
                                    LENGTH_SHORT).show();
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
                            Toast.makeText(Register.this,"Avatar Upload Failed", LENGTH_SHORT) .show();
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
                        mProgress.setVisibility(View.GONE);
                        goProfile();
                        Toast.makeText(Register.this, "Account Created", LENGTH_SHORT).show();

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

    private boolean validateForm() {
        boolean valid = true;

        if(byteArray == null){
            showToast("Avatar is required.");
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

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(getApplicationContext());
        imageCodeProject.setImageResource(R.drawable.ic_launcher_foreground);
        toastView.addView(imageCodeProject, 0);
        toast.show();
    }



}
