package com.example.insta;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Comments extends AppCompatActivity {


    private static final String TAG = "Comments";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private String author;
    private String pID;
    private Uri Url;
    private String caption;
    private String location;
    private String timestamp;

    private String uID;

    private RecyclerView mRecycleView;
    private FloatingActionButton mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        uID = mAuth.getUid();

        Intent intent = getIntent();
        author = intent.getExtras().getString("author");
        pID = intent.getExtras().getString("pID");
        Url = Uri.parse(intent.getExtras().getString("Url"));
        caption = intent.getExtras().getString("caption");
        location = intent.getExtras().getString("location");
        timestamp = intent.getExtras().getString("timestamp");

        mRecycleView = findViewById(R.id.comments);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));

        mBack = findViewById(R.id.cmt_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initDetail();
        refreshPage();

    }

    private void refreshPage() {
        mDatabase.collection("comments")
                .whereEqualTo("pID",pID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        initDetail();
                    }
                });
    }

    private void initDetail(){

        DocumentReference docRef = mDatabase.collection("photos").document(pID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        DetailHolder detailHolder = new DetailHolder(document.getString("uID"),document.getId(), document.getString("url"), document.getString("desc"), document.getString("timestamp"),document.getString("location"));
                        initComments(detailHolder);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void initComments(final DetailHolder detailHolder) {

        final List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
        mDatabase.collection("comments")
                .whereEqualTo("pID",pID)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            recyclerViewItems.add(detailHolder);
                            recyclerViewItems.add(new NewComment(uID, pID, author, "No content", "No timestamp"));

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                CommentHolder model = new CommentHolder(document.getId(), document.getString("uID"), document.getString("pID"), author, document.getString("content"),document.getString("timestamp"), document.getString("avatar"), document.getString("username"));
                                recyclerViewItems.add(model);
                            }

                            final CommentRecycleView adapter = new CommentRecycleView(recyclerViewItems, Comments.this);
                            GridLayoutManager mLayoutManager = new GridLayoutManager(Comments.this, 1);

                            mRecycleView.setLayoutManager(mLayoutManager);
                            mRecycleView.setAdapter(adapter);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


}
