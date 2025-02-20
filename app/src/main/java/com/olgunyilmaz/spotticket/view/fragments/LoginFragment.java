package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentLoginBinding;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private String email;
    private String password;

    public LoginFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(getLayoutInflater(),container,false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        binding.loginButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });

        binding.loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp(view);
            }
        });

        binding.resetPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword(view);
            }
        });
    }

    private void login(View view){
        email = binding.loginUsernameText.getText().toString();
        password = binding.loginPasswordText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Kullanıcı girişi başarılı");
                                FirebaseUser currentUser = auth.getCurrentUser();

                                if (currentUser != null){

                                    String msg;

                                    if(currentUser.isEmailVerified()){
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        getActivity().finish();
                                        msg = "Hoşgeldin : ";
                                    }else{
                                        msg = "Giriş yapabilmek için gönderilen linkten e-posta adresinizi doğrulamanız gerekmektedir.";
                                    }
                                    Toast.makeText(getContext(),msg+currentUser.getDisplayName()
                                            ,Toast.LENGTH_LONG).show();


                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }else{
            Toast.makeText(getContext(),"Lütfen email ve parolanızı girin.",Toast.LENGTH_LONG).show();
        }
    }


    private void signUp(View view){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment signUpFragment = new SignUpFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, signUpFragment).commit();

    }

    private void resetPassword(View view){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, resetPasswordFragment).commit();
    }

}