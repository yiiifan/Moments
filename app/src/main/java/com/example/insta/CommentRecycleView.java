package com.example.insta;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommentRecycleView extends RecyclerView.Adapter {

    private static final String TAG = "COMMENT_RECYCLER";
    private static final int COMMENT_ITEM = 1;
    private static final int COMMENT_NEW = 2;
    private static final int COMMENT_DETAIL = 3;
    private List<RecyclerViewItem> recyclerViewItems;
    private Context mContext;
    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public CommentRecycleView(List<RecyclerViewItem> recyclerViewItems, Context mContext) {
        this.recyclerViewItems = recyclerViewItems;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view;

        if(viewType == COMMENT_ITEM){
            view = mInflater.inflate(R.layout.comment_item, parent, false);
            return new ItemHolder(view);

        }else if(viewType == COMMENT_NEW){
            view = mInflater.inflate(R.layout.comment_upload, parent, false);
            return new NewHolder(view);

        }else if(viewType == COMMENT_DETAIL){
            view = mInflater.inflate(R.layout.comment_detail, parent, false);
            return new MyDetailHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);

        if(holder instanceof NewHolder){
            NewHolder newHolder = (NewHolder) holder;
            final NewComment comment = (NewComment) recyclerViewItem;

            ((NewHolder) holder).post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = ((NewHolder) holder).newContent.getText().toString();
                    String uID = comment.getuID();
                    String pID = comment.getpID();
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    if(content.length() != 0) {
                        loadAuthorInfo(uID, pID, content, timestamp);
                        hideKeyboard((Activity) mContext);

                    }else{
                        Toast.makeText(mContext, "Empty Comment", Toast.LENGTH_SHORT).show();
                    }

                }
            });


        }else if(holder instanceof ItemHolder){
            ItemHolder itemHolder = (ItemHolder) holder;
            final CommentHolder comment = (CommentHolder) recyclerViewItem;

            itemHolder.username.setText(comment.getUsername());
            itemHolder.content.setText(comment.getContent());
            itemHolder.timestamp.setText(timeformat(comment.getTimestamp()));

            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.circleCrop();

            Glide.with(mContext)
                    .load(Uri.parse(comment.getAvatar()))
                    .apply(options)
                    .into(itemHolder.avatar);

            if(comment.getuID().equals(mAuth.getUid()) || comment.getAuthor().equals(mAuth.getUid())){
                itemHolder.delete.setVisibility(View.VISIBLE);
            }else{
                itemHolder.delete.setVisibility(View.GONE);
            }

            itemHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.collection("comments").document(comment.getcID())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    Toast.makeText(mContext, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                }
            });

        }else if(holder instanceof MyDetailHolder){

            MyDetailHolder itemHolder = (MyDetailHolder) holder;
            final DetailHolder detail = (DetailHolder) recyclerViewItem;

            itemHolder.caption.setText(detail.getCaption());
            itemHolder.timestamp.setText(timeformat(detail.getTimestamp()));

            if(!detail.getuID().equals(mAuth.getUid())){
                itemHolder.delete.setVisibility(View.GONE);
            }

            itemHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.collection("photos").document(detail.getpID())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    deleteComment(detail.getpID());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                }
            });

            Glide.with(mContext)
                    .load(Uri.parse(detail.getPhotoURL()))
                    .into(itemHolder.photo);
        }

    }

    private void deleteComment(String pID){

        mDatabase.collection("comments")
                .whereEqualTo("pID", pID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }

                            Toast.makeText(mContext, "Photo Deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, Profile.class);
                            mContext.startActivity(intent);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void loadAuthorInfo(final String uID, final String pID, final String content, final String timestamp) {
        DocumentReference docRef = mDatabase.collection("users").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String username = (String)document.get("username");
                        String avatar = (String)document.get("avatar");
                        uploadComment(uID, pID, content, username, avatar, timestamp);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void uploadComment(String uID, String pID, String content, String username, String avatar, String timestamp) {

        Map<String, Object> comment = new HashMap<>();
        comment.put("uID", uID);
        comment.put("pID", pID);
        comment.put("content", content);
        comment.put("timestamp", timestamp);
        comment.put("username", username);
        comment.put("avatar", avatar);

        String path = "comments";

        mDatabase.collection(path)
                .add(comment)
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

    @Override
    public int getItemCount() {
        return recyclerViewItems.size();
    }

    @Override
    public int getItemViewType(int position){
        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);

        if (recyclerViewItem instanceof CommentHolder)
            return COMMENT_ITEM;

        else if (recyclerViewItem instanceof NewComment)
            return COMMENT_NEW;

        else if(recyclerViewItem instanceof DetailHolder)
            return COMMENT_DETAIL;

        else
            return super.getItemViewType(position);
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        TextView timestamp;
        TextView content;
        Button delete;

        public ItemHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.cmt_avatar);
            username = itemView.findViewById(R.id.cmt_username);
            timestamp = itemView.findViewById(R.id.cmt_timestamp);
            content = itemView.findViewById(R.id.cmt_content);
            delete = itemView.findViewById(R.id.cmt_delete);

        }
    }

    private class NewHolder extends RecyclerView.ViewHolder {
        EditText newContent;
        Button post;

        public NewHolder(View itemView) {
            super(itemView);
            newContent = itemView.findViewById(R.id.cmt_new);
            post = itemView.findViewById(R.id.cmt_btn);

        }
    }

    private class MyDetailHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView caption;
        TextView timestamp;
        Button delete;

        public MyDetailHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.dtl_view);
            caption = itemView.findViewById(R.id.dtl_caption);
            timestamp = itemView.findViewById(R.id.dtl_time);
            delete = itemView.findViewById(R.id.dtl_delete);
        }
    }

    private String timeformat(String description) {
        String year = description.substring(0,4);
        String month = description.substring(4,6);
        String day = description.substring(6,8);
        String hour = description.substring(9,11);
        String minute = description.substring(11,13);
        String second = description.substring(13,15);

        return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
    }
}
