package com.olgunyilmaz.spotticket.view.fragments;


import static android.content.ContentValues.TAG;

import android.app.Activity;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentSignUpBinding;


public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private String username;


    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        binding.signUpButton.setOnClickListener(v -> signUp());

    }


    public void signUp() {

        String email = binding.signUpEmailText.getText().toString();
        String password = binding.signUpPasswordText.getText().toString();
        String validPassword = binding.signUpPasswordValidText.getText().toString();
        username = binding.signUpUserNameText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            if (password.equals(validPassword)) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Başarıyla kayıt oldunuz");
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        // update username
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(username)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User profile updated.");
                                                        }
                                                    }
                                                });

                                        user.sendEmailVerification()
                                                .addOnCompleteListener(task1 -> {
                                                    String msg;
                                                    if (task1.isSuccessful()) {
                                                        msg = "Doğrulama e-postası gönderildi!";
                                                        login();

                                                    } else {
                                                        msg = "Doğrulama e-postası gönderilemedi";
                                                    }
                                                    Toast.makeText(requireContext(),
                                                            msg,
                                                            Toast.LENGTH_LONG).show();
                                                });
                                    }
                                } else {
                                    Log.w(TAG, "Kayıt olunamadı!", task.getException());
                                    Toast.makeText(getContext(), "Kayıt olunamadı!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Lütfen girdiğiniz bilgileri kontrol edin!", Toast.LENGTH_LONG).show();
            }

        }
    }


    private void login() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment fragment = new LoginFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, fragment).commit();
    }

}