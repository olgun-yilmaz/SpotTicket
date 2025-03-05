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


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
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
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private Runnable runnable;
    private Handler handler;
    private LocalDataManager localDataManager;
    int counter = 0;

    public LoginFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = requireActivity().getSupportFragmentManager();
        localDataManager = new LocalDataManager(requireActivity());

        auth = FirebaseAuth.getInstance();
        String countryCode = localDataManager.getStringData(getString(R.string.language_code_key),"tr");

        auth.setLanguageCode(countryCode);

        String lastUserEmail = localDataManager.getStringData(getString(R.string.user_email_key), "");

        binding.loginEmailText.setText(lastUserEmail);

        binding.loginButton.setOnClickListener(v -> login());

        binding.loginSignUpButton.setOnClickListener(v -> signUp());

        binding.resetPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void updateRememberMe() {
        if (binding.rememberMeButton.isChecked()) {
            localDataManager.updateBooleanData(getString(R.string.remember_me_key), true);
        } else {
            localDataManager.updateBooleanData(getString(R.string.remember_me_key), false);
        }

    }

    private void normalMode() {
        binding.resetPasswordText.setText(getText(R.string.forgot_password_text));
        binding.resetPasswordText.setEnabled(true);

        binding.loginEmailText.setVisibility(View.VISIBLE);
        binding.loginSignUpButton.setVisibility(View.VISIBLE);
        binding.loginButton.setVisibility(View.VISIBLE);
        binding.loginPasswordText.setVisibility(View.VISIBLE);
        binding.rememberMeButton.setVisibility(View.VISIBLE);

        handler.removeCallbacks(runnable);

    }

    private void loadingMode() {
        updateLoadingText();
        binding.resetPasswordText.setEnabled(false);

        binding.loginEmailText.setVisibility(View.INVISIBLE);
        binding.loginSignUpButton.setVisibility(View.INVISIBLE);
        binding.loginButton.setVisibility(View.INVISIBLE);
        binding.loginPasswordText.setVisibility(View.INVISIBLE);
        binding.rememberMeButton.setVisibility(View.INVISIBLE);
    }

    private void login() {
        String email = binding.loginEmailText.getText().toString();
        String password = binding.loginPasswordText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            loadingMode();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (isAdded()) {
                                if (task.isSuccessful()) {
                                    FirebaseUser currentUser = auth.getCurrentUser();

                                    if (currentUser != null) {
                                        String msg;

                                        if (currentUser.isEmailVerified()) {
                                            localDataManager.updateStringData(getString(R.string.user_email_key), email);
                                            updateRememberMe();

                                            Intent intent = new Intent(requireActivity(), OnBoardingActivity.class); // for download the user data
                                            intent.putExtra(getString(R.string.from_login_key), true);
                                            intent.putExtra(getString(R.string.user_email_key), email);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            requireActivity().finish();
                                            msg = getString(R.string.welcome_message) + " " + currentUser.getDisplayName();

                                        } else {
                                            normalMode();
                                            msg = getString(R.string.email_verification_message);
                                        }
                                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();


                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (isAdded()) {
                                normalMode();
                                Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(requireContext(), getString(R.string.pleae_check_your_info_text), Toast.LENGTH_LONG).show();
        }
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    counter++;
                    int numPoint = counter % 4;
                    String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                    binding.resetPasswordText.setText(getString(R.string.please_wait_text) + numPointText);
                    handler.postDelayed(runnable, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void signUp() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment signUpFragment = new SignUpFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, signUpFragment).commit();

    }

    private void resetPassword() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, resetPasswordFragment).commit();
    }

}