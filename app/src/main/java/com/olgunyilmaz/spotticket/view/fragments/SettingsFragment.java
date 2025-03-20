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

import static androidx.core.app.ActivityCompat.recreate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.model.Language;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentSettingsBinding;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.helper.MainHelper;
import com.olgunyilmaz.spotticket.helper.OnBoardingHelper;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private FragmentManager fragmentManager;
    private MainActivity activity;
    private Language selectedLanguage;
    LocalDataManager localDataManager;
    int languageCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) requireActivity();

        localDataManager = new LocalDataManager(activity);
        OnBoardingHelper helper = new OnBoardingHelper(activity);
        helper.getLanguageData(this::selectLanguage);

        fragmentManager = activity.getSupportFragmentManager();

        if(UserManager.getInstance().profileImage == null){
            binding.settingsProfileImage.setImageResource(R.drawable.sample_profile_image);
        }else{
            binding.settingsProfileImage.setImageBitmap(UserManager.getInstance().profileImage);
        }

        binding.settingsNameText.setText(UserManager.getInstance().name);
        binding.settingsCityText.setText(UserManager.getInstance().city);

        binding.settingsEditButton.setOnClickListener(v -> replaceFragment(new ProfileFragment()));
        binding.settingsPasswordLayout.setOnClickListener(v -> replaceFragment(new ChangePasswordFragment()));
        binding.settingsLanguageLayout.setOnClickListener(v -> changeLanguage(helper.getLanguageData(null)));
        binding.settingsLogOutLayout.setOnClickListener(v ->signOut());
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerView,fragment).commit();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void selectLanguage(ArrayList<Language> languages){
        languageCounter = localDataManager.getIntegerData(getString(R.string.language_counter_key),0);

        int selectedId = languageCounter % languages.size();

        selectedLanguage = languages.get(selectedId);

        Locale locale = new Locale(selectedLanguage.getCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Context context = activity.createConfigurationContext(config);
            getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        } else {
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        binding.languageIcon.setImageResource(selectedLanguage.getImageID());
        binding.languageText.setText(
                String.format("%s %s", activity.getString(R.string.title_language), selectedLanguage.getLanguageText()));

    }

    private void changeLanguage(ArrayList<Language> languages) {
        languageCounter = localDataManager.getIntegerData(getString(R.string.language_counter_key), 0);
        languageCounter += 1;

        if (languageCounter == languages.size() * 10000) { // reset if it's very big
            languageCounter = 0;
        }

        localDataManager.updateIntegerData(getString(R.string.language_counter_key), languageCounter);

        int selectedId = languageCounter % languages.size();
        selectedLanguage = languages.get(selectedId);

        localDataManager.updateStringData(getString(R.string.language_code_key), selectedLanguage.getCode());

        selectLanguage(languages);

        recreate(activity);
    }

    private void signOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        new MainHelper((MainActivity) requireContext()).goToLoginActivity();

        LocalDataManager localDataManager = new LocalDataManager(requireContext());
        localDataManager.deleteData(getString(R.string.city_key));
        localDataManager.deleteData(getString(R.string.category_key));

        UserManager.getInstance().profileImage = null; // clean for next user
    }


}