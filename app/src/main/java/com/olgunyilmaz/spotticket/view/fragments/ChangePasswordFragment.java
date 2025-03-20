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
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentChangePasswordBinding;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

import java.util.ArrayList;


public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private FirebaseAuth auth;
    private Handler handler;
    private Runnable runnable;
    private int counter = 0;
    private ArrayList<View> allViews;
    private MainActivity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        getAllViews();

        activity = (MainActivity) requireActivity();
        activity.binding.fixedBar.setVisibility(View.INVISIBLE);

        binding.backButton.setOnClickListener(v -> backToSettings());
        binding.changePasswordButton.setOnClickListener(v -> changePassword());

    }

    private void getAllViews() {
        allViews = new ArrayList<>();
        allViews.add(binding.changePasswordButton);
        allViews.add(binding.newPasswordText);
        allViews.add(binding.oldPasswordText);
        allViews.add(binding.newPasswordValidText);
        allViews.add(binding.changeTitle);
        allViews.add(binding.line);
    }

    private void loadingMode() {
        for (View v : allViews) {
            v.setVisibility(View.INVISIBLE);
        }
        binding.loadingTextView.setVisibility(View.VISIBLE);
        updateLoadingText();
    }

    private void normalMode() {
        for (View v : allViews) {
            v.setVisibility(View.VISIBLE);
        }
        binding.loadingTextView.setVisibility(View.INVISIBLE);
        handler.removeCallbacks(runnable);
    }

    private void changePassword() {
        String oldPassword = binding.oldPasswordText.getText().toString();

        if (oldPassword.isEmpty()) {
            toaster(getString(R.string.please_check_your_info_text));
        } else {
            loadingMode();
            auth.signInWithEmailAndPassword(UserManager.getInstance().email, oldPassword)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            checkNewPassword(oldPassword);
                        } else {
                            normalMode();
                            toaster(getString(R.string.please_check_your_info_text));
                        }
                    });
        }
    }

    private void checkNewPassword(String oldPassword) {
        String newPassword = binding.newPasswordText.getText().toString();
        String validPassword = binding.newPasswordValidText.getText().toString();

        if (!newPassword.isEmpty() && newPassword.equals(validPassword)) {
            if (newPassword.equals(oldPassword)) { // is same
                normalMode();
                toaster(getString(R.string.password_must_be_different));
            } else {
                FirebaseUser user = auth.getCurrentUser();
                assert user != null;
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                toaster(getString(R.string.successfully_change_password_text));
                                normalMode();
                                backToSettings();
                            } else {
                                toaster(getString(R.string.error_text));
                                normalMode();
                            }
                        });
            }
        } else {
            toaster(getString(R.string.please_check_your_info_text));
            normalMode();
        }
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = () -> {
            if (isAdded()) {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.loadingTextView.setText(String.format("%s%s", getString(R.string.plain_loading_text), numPointText));
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void toaster(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void backToSettings() {
        activity.binding.fixedBar.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.fragmentContainerView, fragment).commit();

    }

}