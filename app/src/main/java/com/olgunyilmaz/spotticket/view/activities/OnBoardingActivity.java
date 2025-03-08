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

package com.olgunyilmaz.spotticket.view.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityOnBoardingBinding;
import com.olgunyilmaz.spotticket.util.OnBoardingHelper;
import com.olgunyilmaz.spotticket.util.Language;
import com.olgunyilmaz.spotticket.util.LocalDataManager;

import java.util.ArrayList;
import java.util.Locale;


public class OnBoardingActivity extends AppCompatActivity {
    private ActivityOnBoardingBinding binding;
    public static String TICKETMASTER_BASE_URL;
    public static String TICKETMASTER_API_KEY;
    public static String MAPS_BASE_URL;
    public static String MAPS_API_KEY;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private Runnable runnable;
    private Handler handler;
    private OnBoardingHelper helper;
    private LocalDataManager localDataManager;
    private ArrayList<Language> languageList;
    private Language selectedLanguage;
    private int counter = 0;
    private int languageCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDataManager = new LocalDataManager(OnBoardingActivity.this);
        getLanguageData();
        localDataManager.updateStringData(getString(R.string.language_code_key),selectedLanguage.getCode());

        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.languageButton.setImageResource(selectedLanguage.getImageID());
        binding.languageText.setText(selectedLanguage.getLanguageText());

        TICKETMASTER_BASE_URL = getString(R.string.ticketmaster_base_url);
        TICKETMASTER_API_KEY = getString(R.string.ticketmaster_api_key);

        MAPS_BASE_URL = getString(R.string.maps_base_url);
        MAPS_API_KEY = getString(R.string.maps_api_key);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode(selectedLanguage.getCode());

        db = FirebaseFirestore.getInstance();

        helper = new OnBoardingHelper(OnBoardingActivity.this);

        boolean isFromLogin = getIntent().getBooleanExtra(getString(R.string.from_login_key), false);

        boolean isRemember = localDataManager.getBooleanData(getString(R.string.remember_me_key));

        if (isFromLogin) {
            String email = getIntent().getStringExtra(getString(R.string.user_email_key));
            isRemember = true; // dont update data just give permission for login

            //binding.imageView.setImageResource(R.drawable.loading); // will use a diff background
            downloadData(email);
        }


        if (isRemember) {
            currentUser = auth.getCurrentUser();
        }

        if (currentUser != null) {

            if (currentUser.getEmail() != null) {

                String email = currentUser.getEmail().toString();

                if (!email.isEmpty()) {
                    downloadData(email);
                }

            }
        }

        binding.languageButton.setOnClickListener(v -> changeLanguage());

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentUser != null) {
                    helper.goToActivity(MainActivity.class);
                    finish(); // if user in app, won't back by intent
                } else {
                    helper.goToActivity(EmailPasswordActivity.class);
                }
            }
        });
    }

    private void selectLanguage(){
        languageCounter = localDataManager.getIntegerData(getString(R.string.language_counter_key),0);

        int selectedId = languageCounter % languageList.size();

        selectedLanguage = languageList.get(selectedId);

        Locale locale = new Locale(selectedLanguage.getCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Context context = createConfigurationContext(config);
            getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        } else {
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    private void changeLanguage(){
        languageCounter = localDataManager.getIntegerData(getString(R.string.language_counter_key),0);

        languageCounter += 1;

        if(languageCounter == languageList.size() * 10000){ // reset if it's very big
            languageCounter = 0;
        }

        localDataManager.updateIntegerData(getString(R.string.language_counter_key),languageCounter);

        recreate();

    }

    private void getLanguageData(){
        languageList = new ArrayList<>();
        languageList.add(new Language(R.drawable.icon_tr,"tr","Türkçe"));
        languageList.add(new Language(R.drawable.icon_en,"en","English"));
        languageList.add(new Language(R.drawable.icon_de,"de","Deutsch"));

        selectLanguage(); // after get data select language
    }

    private void letsGo() { // end process
        binding.nextButton.setEnabled(true); // u can go
        binding.getStartText.setText(getString(R.string.lets_go_text));
        handler.removeCallbacks(runnable);
    }

    private void downloadData(String email) {
        binding.nextButton.setEnabled(false);
        helper.getPp(email,this::updateLoadingText,db,localDataManager,this::letsGo);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.getStartText.setText(getString(R.string.plain_loading_text) + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }


}