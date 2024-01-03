package com.example.chatinstablog.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatinstablog.Fragments.LoginFragment;
import com.example.chatinstablog.Fragments.RegisterFragment;
import com.example.chatinstablog.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private SwitchMaterial switchMaterial;
    private FragmentManager fragmentManager;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchMaterial = findViewById(R.id.toggleButton);
        fragmentManager=  getSupportFragmentManager();
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }


        //ilk olarak login gönderilsin
        if (savedInstanceState == null) {
            showLoginFragment();
        }

        // Switch değiştiğinde fragment'ı değiştir
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showRegisterFragment();
            } else {
                showLoginFragment();
            }
        });


    }

    public void showLoginFragment(){
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
    }
    public void showRegisterFragment(){
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new RegisterFragment()).addToBackStack(null).commit();
    }
}