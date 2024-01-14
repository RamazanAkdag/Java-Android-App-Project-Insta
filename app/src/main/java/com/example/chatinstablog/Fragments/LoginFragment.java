package com.example.chatinstablog.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.chatinstablog.Activities.ForgotPasswordActivity;
import com.example.chatinstablog.Activities.HomeActivity;
import com.example.chatinstablog.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText editTextLoginEmail, editTextLoginPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;

    public LoginFragment() {
        // Boş bir constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();

        editTextLoginEmail = view.findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = view.findViewById(R.id.editTextLoginPassword);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        textViewForgotPassword = view.findViewById(R.id.textViewForgotPassword);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(container.getContext(), ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    private void loginUser() {
        String email = editTextLoginEmail.getText().toString().trim();
        String password = editTextLoginPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {


                        // HomeActivity'e geçiş yap
                        Intent intent = new Intent(requireContext(), HomeActivity.class);
                        startActivity(intent);
                        requireActivity().finish(); // Bu activity'yi kapat, geri dönüş yapılamasın
                    } else {
                        // Giriş başarısızsa kullanıcıyı bilgilendir
                        Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        // task.getException().getMessage() ile hatayı alabilirsiniz
                    }
                });
    }

    private void saveUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            // Kullanıcı bilgilerini SharedPreferences veya başka bir yerde saklayabilirsiniz
            // Örneğin, SharedPreferences kullanımı:
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username);
            editor.apply();
        }
    }
}