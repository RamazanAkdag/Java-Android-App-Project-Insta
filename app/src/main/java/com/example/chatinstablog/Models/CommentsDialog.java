package com.example.chatinstablog.Models;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatinstablog.Adapters.CommentsAdapter;
import com.example.chatinstablog.Adapters.PostAdapter;
import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentsDialog extends Dialog {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    Post post;
    private String postId;
    private EditText editTextComment;
    private Button buttonPostComment;
    private RecyclerView recyclerViewComments;

    private PostAdapter postAdapter;

    private ArrayList<Comment> comments;
    private CommentsAdapter commentsAdapter;

    private int position;

    public CommentsDialog(@NonNull Context context, ArrayList<Comment> comments,String postId,Post post,PostAdapter postAdapter,int position) {
        super(context, R.style.CustomDialog);  // Stil belirle
        setContentView(R.layout.dialog_comment);
        this.postId = postId;
        this.post = post;
        this.comments = comments;
        this.postAdapter = postAdapter;
        this.position = position;
        editTextComment = findViewById(R.id.editTextComment);
        buttonPostComment = findViewById(R.id.buttonPostComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));

        commentsAdapter = new CommentsAdapter(comments);
        recyclerViewComments.setAdapter(commentsAdapter);

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editTextComment.getText().toString().trim();

                if (!commentText.isEmpty()) {
                    // Yorum nesnesini oluştur
                    String userId = auth.getCurrentUser().getUid();


                    db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();

                                User user = documentSnapshot.toObject(User.class);

                                String username = user.UserName
                                        ;
                                String profileImgUrl = user.ProfileImgUrl;
                                Comment newComment = new Comment(postId,userId, username, profileImgUrl, commentText, Timestamp.now());

                                // Firestore'a yorumu ekle
                                addCommentToFirestore(newComment);

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println(e.getMessage());
                        }
                    });




                }

                // Diyalogu kapat
                dismiss();
            }
        });
    }

    private void addCommentToFirestore(Comment comment) {
        // "comments" koleksiyonu içine yeni bir belge ekleyin
        db.collection("comments")
                .add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Yorum başarıyla eklendiğinde yapılacak işlemler
                        editTextComment.setText(""); // Yorum girdi alanını temizle

                        Map<String, Object> updates = new HashMap<>();
                        post.commentsCount++;
                        updates.put("commentsCount", post.commentsCount);
                        db.collection("Posts").document(postId).update(updates);
                        postAdapter.notifyItemChanged(position);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hata durumunda yapılacak işlemler
                        Toast.makeText(getContext(), "Yorum gönderme başarısız", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}