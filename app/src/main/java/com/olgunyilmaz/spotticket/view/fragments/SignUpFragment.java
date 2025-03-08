/*
 * Project: SpotTicket
 * Copyright 2025 Olgun Yılmaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olgunyilmaz.spotticket.view.fragments;


import static android.content.ContentValues.TAG;

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
import com.olgunyilmaz.spotticket.util.LocalDataManager;


public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private String username;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        String countryCode = new LocalDataManager(requireActivity()).getStringData(getString(R.string.language_code_key),"tr");

        auth.setLanguageCode(countryCode);

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
                                    Log.d(TAG, getString(R.string.successfully_sign_up_text));
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
                                                        msg = getString(R.string.validation_email_send_text);
                                                        login();

                                                    } else {
                                                        msg = getString(R.string.error_validation_email_send_text);
                                                    }
                                                    Toast.makeText(requireContext(),
                                                            msg,
                                                            Toast.LENGTH_LONG).show();
                                                });
                                    }
                                } else {
                                    Log.w(TAG, getString(R.string.error_text), task.getException());
                                    Toast.makeText(getContext(), getString(R.string.error_sign_up_text),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), getString(R.string.please_check_your_info_text), Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(requireActivity(),
                    requireActivity().getString(R.string.please_check_your_info_text),Toast.LENGTH_SHORT).show();
        }
    }


    private void login() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment fragment = new LoginFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, fragment).commit();
    }

}