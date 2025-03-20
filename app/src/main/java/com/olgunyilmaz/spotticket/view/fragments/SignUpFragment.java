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

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentSignUpBinding;
import com.olgunyilmaz.spotticket.helper.HomePageHelper;
import com.olgunyilmaz.spotticket.util.LocalDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SignUpFragment extends Fragment implements SelectCityFragment.CitySelectListener {
    private FragmentSignUpBinding binding;
    private FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = requireActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        String countryCode = new LocalDataManager(requireActivity()).getStringData(getString(R.string.language_code_key), "tr");

        auth.setLanguageCode(countryCode);

        db = FirebaseFirestore.getInstance();

        HomePageHelper helper = new HomePageHelper(requireContext(),null);

        ArrayList<String> cities = helper.getCities();
        binding.cityText.setOnClickListener(v -> showCityPicker(cities));

        binding.signUpButton.setOnClickListener(v -> signUp());
        binding.signUpLoginButton.setOnClickListener(v -> replaceFragment(new LoginFragment()));

    }

    private ArrayList<View> getAllViews() {
        ArrayList<View> fragmentViews = new ArrayList<>();
        fragmentViews.add(binding.signUpNameText);
        fragmentViews.add(binding.signUpSurnameText);
        fragmentViews.add(binding.signUpEmailText);
        fragmentViews.add(binding.signUpButton);
        fragmentViews.add(binding.signUpPasswordText);
        fragmentViews.add(binding.signUpPasswordValidText);
        fragmentViews.add(binding.signUpLoginButton);
        fragmentViews.add(binding.alreadyAccountText);
        return fragmentViews;
    }

    private void showCityPicker(ArrayList<String> cities) {
        SelectCityFragment fragment = new SelectCityFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.cities_key), cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(requireActivity().getSupportFragmentManager(), getString(R.string.city_picker_tag));
    }


    public void signUp() {
        String email = binding.signUpEmailText.getText().toString();
        String password = binding.signUpPasswordText.getText().toString();
        String validPassword = binding.signUpPasswordValidText.getText().toString();

        String name = binding.signUpNameText.getText().toString();
        String surname = binding.signUpSurnameText.getText().toString();
        String city = binding.cityText.getText().toString();

        if (!(email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() || city.isEmpty())) {
            if (password.equals(validPassword)) {
                loadingMode();
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    saveUserData(email, name, surname, city);

                                                } else {
                                                    Toast.makeText(requireContext(),
                                                            getString(R.string.error_validation_email_send_text),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            } else {
                                if (isAdded()) {
                                    normalMode();
                                }
                                Log.w(TAG, getString(R.string.error_text), task.getException());
                                Toast.makeText(getContext(), getString(R.string.error_sign_up_text),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                if (isAdded()) {
                    normalMode();
                }
                Toast.makeText(getContext(), getString(R.string.please_check_your_info_text), Toast.LENGTH_LONG).show();
            }

        } else {
            if (isAdded()) {
                normalMode();
            }
            Toast.makeText(requireActivity(),
                    requireActivity().getString(R.string.please_check_your_info_text), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCitySelected(String city) {
        binding.cityText.setText(city);
    }

    private void normalMode() {
        binding.cityText.setText(getString(R.string.please_select_your_city_text));
        binding.cityText.setEnabled(true);

        for (View view : getAllViews()) {
            view.setVisibility(View.VISIBLE);
        }

        handler.removeCallbacks(runnable);

    }

    private void loadingMode() {
        updateLoadingText();
        binding.cityText.setEnabled(false);

        for (View view : getAllViews()) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = () -> {
            if (isAdded()) {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.cityText.setText(String.format("%s%s", getString(R.string.please_wait_text), numPointText));
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void saveUserData(String email, String name, String surname, String city) {
        db.collection(getString(R.string.users_collection_key))
                .whereEqualTo(getString(R.string.email_key), email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Map<String, Object> user = new HashMap<>();
                    user.put(getString(R.string.email_key), email);
                    user.put(getString(R.string.name_key), name);
                    user.put(getString(R.string.surname_key), surname);
                    user.put(getString(R.string.city_key), city);
                    user.put(getString(R.string.profile_image_url_key), "");

                    db.collection(getString(R.string.users_collection_key)).add(user)
                            .addOnSuccessListener(documentReference -> {
                                if(isAdded()){
                                    Bundle args = new Bundle();
                                    args.putBoolean(getString(R.string.from_login_key), true);
                                    EmailSentFragment fragment = new EmailSentFragment();
                                    fragment.setArguments(args);
                                    replaceFragment(fragment);
                                }

                            });
                });
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.loginFragmentContainer, fragment).commit();
    }
}