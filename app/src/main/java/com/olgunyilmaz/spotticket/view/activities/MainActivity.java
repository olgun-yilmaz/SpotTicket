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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.view.fragments.SettingsFragment;
import com.olgunyilmaz.spotticket.notification.NotificationScheduler;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.notification.NotificationHelper;
import com.olgunyilmaz.spotticket.helper.MainHelper;
import com.olgunyilmaz.spotticket.util.UserFavoritesManager;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.fragments.DisplayFragment;
import com.olgunyilmaz.spotticket.view.fragments.FavoritesFragment;
import com.olgunyilmaz.spotticket.view.fragments.HomePageFragment;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> permissionLauncher;
    private LocalDataManager localDataManager;
    private final ArrayList<HashMap<String, Object>> pendingNotifications = new ArrayList<>();
    private MainHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerLauncher();

        helper = new MainHelper(this);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                helper.showExitDialog();
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();

        localDataManager = new LocalDataManager(this);

        String languageCode = localDataManager.getStringData(getString(R.string.language_code_key), "tr");
        auth.setLanguageCode(languageCode);

        fragmentManager = getSupportFragmentManager();

        setNotificationAlert();

        String eventName = getIntent().getStringExtra(getString(R.string.event_name_key)); // if it comes from notification

        helper.directToEventDetails(eventName, fragmentManager);

        binding.displayButton.setOnClickListener(v -> helper.replaceFragment(new DisplayFragment(), fragmentManager));

        binding.homeButton.setOnClickListener(v -> helper.replaceFragment(new HomePageFragment(), fragmentManager));

        binding.myEventsButton.setOnClickListener(v -> helper.replaceFragment(new FavoritesFragment(), fragmentManager));
    }
    public void goToSettingsScreen(View v) {
        helper.replaceFragment(new SettingsFragment(), fragmentManager);
    }

    private void setNotificationAlert() {
        pendingNotifications.clear();
        NotificationHelper notificationHelper = new NotificationHelper(MainActivity.this);

        for (FavoriteEventModel event : UserFavoritesManager.getInstance().userFavorites) {
            boolean isSentBefore = localDataManager.getBooleanData(event.getEventId()); // default false

            Long daysLeft = notificationHelper.calculateDaysLeft(event.getDate());

            if (!(daysLeft == null || isSentBefore)) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(getString(R.string.category_icon_key), event.getCategoryIcon());
                hashMap.put(getString(R.string.days_left_key), daysLeft);
                hashMap.put(getString(R.string.event_name_key), event.getEventName());
                pendingNotifications.add(hashMap);
                localDataManager.updateBooleanData(event.getEventId(), true); // update for the next
            }
        }

        if (!pendingNotifications.isEmpty()) {
            requestNotificationPermission(binding.homeButton);
        }
    }

    private void requestNotificationPermission(View view) {
        @SuppressLint("InlinedApi") String permission = Manifest.permission.POST_NOTIFICATIONS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                    Snackbar.make(view, getString(R.string.notification_permission_text),
                            Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.give_permission_text), v -> {
                        permissionLauncher.launch(permission); // ask permission
                    }).show();
                }else{
                    permissionLauncher.launch(permission); // ask permission
                }

            } else { // granted
                sendAllNotifications();
            }
        } else { // don't need permission
            sendAllNotifications();
        }
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                sendAllNotifications();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.notification_permission_text), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendAllNotifications() {
        for (HashMap<String, Object> hashMap : pendingNotifications) {

            Long daysLeft = (Long) hashMap.get(getString(R.string.days_left_key));

            Integer hours = helper.calculateSendDelayInHours(daysLeft);

            if(hours != null){ // negative -> past event
                String eventName = (String) hashMap.get(getString(R.string.event_name_key));
                Long categoryIconId = (Long) hashMap.get(getString(R.string.category_icon_key));
                NotificationScheduler scheduler = new NotificationScheduler(daysLeft, eventName, categoryIconId);
                scheduler.scheduleNotification(this, hours * 3600L); // hours to seconds
            }
        }
    }
}