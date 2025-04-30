/*
 * Project: EventBox
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

package com.olgunyilmaz.eventbox.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.eventbox.R;
import com.olgunyilmaz.eventbox.databinding.FragmentEmailSentBinding;


public class EmailSentFragment extends Fragment {
    private FragmentEmailSentBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmailSentBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.infoText.setText(getString(R.string.email_sent_text,getString(R.string.reset_password_text)));

        Bundle args = getArguments();
        if(args != null){
            boolean isFromLogin = args.getBoolean(getString(R.string.from_login_key));
            if(isFromLogin){
                binding.infoText.setText(getString(R.string.email_verification_message));
            }
        }

        binding.backToLogin.setOnClickListener(v -> login());
    }

    private void login() {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, loginFragment).commit();
    }
}