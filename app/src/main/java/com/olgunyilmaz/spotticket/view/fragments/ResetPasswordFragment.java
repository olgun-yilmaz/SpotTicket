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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentResetPasswordBinding;
import com.olgunyilmaz.spotticket.util.LocalDataManager;

public class ResetPasswordFragment extends Fragment {
    private FragmentResetPasswordBinding binding;
    FragmentManager fragmentManager;
    private FirebaseAuth auth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =FragmentResetPasswordBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentManager = requireActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();

        String countryKey = getString(R.string.language_code_key);
        String countryCode = new LocalDataManager(requireActivity()).getStringData(countryKey,"tr");

        auth.setLanguageCode(countryCode);

        binding.resetPasswordButton.setOnClickListener(this::resetPassword);

        binding.backButton.setOnClickListener(v -> replaceFragment(new LoginFragment()));
    }

    private void resetPassword(View view){
        String emailAddress = binding.resetEmailText.getText().toString();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    String msg;
                    if (task.isSuccessful()) {
                         msg = getString(R.string.send_password_reset_link);
                         replaceFragment(new EmailSentFragment());
                    }else{
                        msg = getString(R.string.error_text);
                    }
                    Toast.makeText(getContext(),msg,Toast.LENGTH_LONG).show();
                });
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.loginFragmentContainer, fragment).commit();
    }
}