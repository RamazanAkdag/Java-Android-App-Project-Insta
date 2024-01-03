package com.example.chatinstablog.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> activityResultLauncher;

    ActivityResultLauncher<String> permissionLauncher;

    FrameLayout frameLayout;
    ImageView icon;

    ImageView postImage;
    Uri imageUri;

    Bitmap selectedImage;

    Button shareButton;

    EditText sharingDescEditText;

    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;

    FirebaseAuth auth;


    String storageImageUrl;

    String postId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        frameLayout = findViewById(R.id.frameLayoutAddPost);
        postImage = findViewById(R.id.imageViewPost);
        icon = findViewById(R.id.imageViewAddPostIcon);

        shareButton = findViewById(R.id.buttonSharePost);
        sharingDescEditText = findViewById(R.id.editTextDescription);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        auth = FirebaseAuth.getInstance();
        registerLaunchers();
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePost(view);
            }
        });
    }


    public void sharePost(View view) {
        String desc = sharingDescEditText.getText().toString();

        if (imageUri == null){
            Toast.makeText(this, "Lütfen bir resim seçiniz", Toast.LENGTH_SHORT).show();
        } else {


            // Firestore'a gönderi bilgilerini ekleyin
            addPostToFirestore(desc);






        }
    }

    public void goToHomeActivity(){
        Intent intent = new Intent(AddPostActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void addPostToFirestore(String desc) {
        System.out.println("Firestore kayıt işlemi başladı");
        // Firestore'a gönderi bilgilerini ekleyin
        String userId = auth.getCurrentUser().getUid().toString();
        Post post = new Post(desc, storageImageUrl, 0, 0, Timestamp.now(), userId);
        System.out.println("eklencek post : " + post.description);

        firebaseFirestore.collection("Posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {

                    // Firestore'a ekleme başarılı oldu

                    postId = documentReference.getId();
                    System.out.println("eklendiii : " + postId);
                    // Resmi Firebase Storage'a yükle
                    uploadImageToFirebaseStorage(postId);

                })
                .addOnFailureListener(e -> {
                    // Firestore'a ekleme sırasında bir hata oluştu
                    System.out.println("eklenemediiii");
                    Toast.makeText(this, "Gönderi paylaşılırken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }
    private void uploadImageToFirebaseStorage(String storageImageId) {
        System.out.println("storageuye kayıt işlemi başladı");
        if (imageUri != null) {
            String fileName = "PostImages/" + storageImageId ;
            System.out.println("eklenecek yer : " + fileName);
            StorageReference storageReference = firebaseStorage.getReference(fileName);

            // Resmi Storage'a yükleyin
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Yükleme başarılı oldu, buradan resmin URL alacağız
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            storageImageUrl = uri.toString();
                            updatePostWithImageUrl(postId,storageImageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Yükleme sırasında bir hata oluştu
                        Toast.makeText(this, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updatePostWithImageUrl(String postId, String storageImageUrl) {
        System.out.println("Post güncelleme işlemi başladı");
        // Firestore'daki ilgili gönderi belgesini güncelleyin
        firebaseFirestore.collection("Posts")
                .document(postId)
                .update("imageUri", storageImageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Firestore güncelleme başarılı oldu
                    Toast.makeText(this, "Gönderi paylaşıldı", Toast.LENGTH_SHORT).show();
                    goToHomeActivity();
                })
                .addOnFailureListener(e -> {
                    // Firestore güncelleme sırasında bir hata oluştu
                    Toast.makeText(this, "Gönderi paylaşılırken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }


    public void selectImage(View view){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(AddPostActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view,"need to permission for search gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();

                }else{
                    permissionLauncher.launch(
                            Manifest.permission.READ_MEDIA_IMAGES);
                }





            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);


            }


        }else{
            if (ContextCompat.checkSelfPermission(
                    AddPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view,"need to permission for search gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                }else{
                    permissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                }



            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);


            }

        }

    }

    private void registerLaunchers(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent intentFromResult = o.getData();

                    if(intentFromResult != null){
                        imageUri = intentFromResult.getData();
                        postImage.setImageURI(imageUri);
                        icon.setVisibility(ImageView.INVISIBLE);

                        /*try {
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source=  ImageDecoder.createSource(getContentResolver(),imageUri);
                                selectedImage = ImageDecoder.decodeBitmap(source);

                                int newWidth = 300;
                                int newHeight = 300;

                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedImage, newWidth, newHeight, true);


                                postImage.setImageBitmap(scaledBitmap);


                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(AddPostActivity.this.getContentResolver(),imageUri);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }*/

                    }
                }else{

                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else{
                    Toast.makeText(AddPostActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();



                }
            }
        });
    }
}