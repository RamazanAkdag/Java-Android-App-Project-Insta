package com.example.chatinstablog.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.chatinstablog.Activities.RegisterActivity;
import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    Button registerButton;

    EditText userNameEditText;
    EditText firstNameEditText;

    EditText lastNameEditText;


    String collectionName = "Users";

    FragmentManager fragmentManager;

    String userName;
    String firstName;
    String lastName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        fragmentManager = getParentFragmentManager();

        registerButton = view.findViewById(R.id.buttonRegister);
        userNameEditText = view.findViewById(R.id.editTextRegisterUserName);
        firstNameEditText = view.findViewById(R.id.editTextRegisterFirstName);
        lastNameEditText = view.findViewById(R.id.editTextRegisterLastName);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kullanıcı adı kullanılmış mı? yoksa devam et
                 userName = userNameEditText.getText().toString();
                 firstName = firstNameEditText.getText().toString();
                 lastName = lastNameEditText.getText().toString();



                 checkUserNameAndProceed();
            }
        });

        return view;
    }

    public void checkUserNameAndProceed(){
        Query query = db.collection(collectionName).whereEqualTo("UserName",userName);
        boolean isAvailable = true;
        query.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               QuerySnapshot querySnapshot = task.getResult();
               if (querySnapshot != null && !querySnapshot.isEmpty()) {
                   // Kullanıcı adı mevcut
                   for (QueryDocumentSnapshot document : querySnapshot) {
                       // Belge üzerinde işlemler
                       String existingUserName = document.getString("UserName");
                       // Örneğin, mevcut kullanıcı adı
                       System.out.println("Mevcut Kullanıcı Adı: " + existingUserName);
                   }
                   Toast.makeText(requireContext(), "Kullanıcı adı mevcut! Başka bir kullanıcı adı deneyin.", Toast.LENGTH_SHORT).show();


               }else{
                    goToRegister();

               }
           }
        });


    }

    public void goToRegister(){
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        intent.putExtra("username",userName);
        intent.putExtra("firstname",firstName);
        intent.putExtra("lastname",lastName);

        startActivity(intent);
        getActivity().finish();
    }
}
