package com.example.chatinstablog.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher;

    ActivityResultLauncher<String> permissionLauncher;
    ImageView profile;
    EditText emailText;
    EditText passwordText;
    Button registerButton;

    Uri imageUri;
    String firstName;
    String lastName;

    Bitmap selectedImage;

    String userName;

    String password;
    String email;

    StorageReference storageRef;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profile = findViewById(R.id.imageViewRegisterProfile);
        emailText = findViewById(R.id.editTextRegisterEmailAddress);
        passwordText = findViewById(R.id.editTextRegisterPassword);
        registerButton = findViewById(R.id.buttonRegisterCreateAccount);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        firstName = intent.getStringExtra("firstname");
        lastName = intent.getStringExtra("lastname");
        userName = intent.getStringExtra("username");


        registerLaunchers();


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(view);
            }
        });

    }

    public void createAccount(View view){
       email = emailText.getText().toString();
        password = passwordText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Bir email adresi girin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bir şifre girin", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    if (imageUri != null){
                        addProfilePicture(task.getResult().getUser().getUid(),imageUri);
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    task.getException().printStackTrace();
                }
            }
        });


    }

    public void addProfilePicture(String userId, Uri uri){

        storageRef = FirebaseStorage.getInstance().getReference("UserProfileImages/"+userId);

        storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveUserInfoToFirestore(userId, imageUrl);
                });




            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Resim yükleme başarısız, hata mesajı göster
                Toast.makeText(RegisterActivity.this, "Profil Fotoğrafı yüklenemedi", Toast.LENGTH_SHORT).show();
                 e.printStackTrace();
            }
        });

    }

    public void saveUserInfoToFirestore(String userId,String imageUrl){

        DocumentReference docRef = db.collection("Users").document(userId);

        Map<String,Object> data = new HashMap<>();
        data.put("Email",email);
        data.put("UserName",userName);
        data.put("FirstName",firstName);
        data.put("LastName",lastName);
        data.put("ProfileImgUrl",imageUrl);
        data.put("FollowerCount",0);
        data.put("FollowedCount",0);
        data.put("Posts",0);

        docRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(RegisterActivity.this, "Hesap Oluşturuldu", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Kullanıcı Kaydında Hata.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

    }

    public void selectImage(View view){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

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
                    RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
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
                        profile.setImageURI(imageUri);


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
                    Toast.makeText(RegisterActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();



                }
            }
        });
    }

}

