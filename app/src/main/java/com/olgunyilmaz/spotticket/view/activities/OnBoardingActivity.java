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

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityOnBoardingBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.service.RecommendedEventManager;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.service.UserManager;
import com.olgunyilmaz.spotticket.util.LocalDataManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    int counter = 0;
    private LocalDataManager localDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        TICKETMASTER_BASE_URL = getString(R.string.ticketmaster_base_url);
        TICKETMASTER_API_KEY = getString(R.string.ticketmaster_api_key);

        MAPS_BASE_URL = getString(R.string.maps_base_url);
        MAPS_API_KEY = getString(R.string.maps_api_key);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        db = FirebaseFirestore.getInstance();

        localDataManager = new LocalDataManager(OnBoardingActivity.this);

        boolean isFromLogin = getIntent().getBooleanExtra("fromLogin", false);

        boolean isRemember = localDataManager.getBooleanData("rememberMe");

        if (isFromLogin) {
            String email = getIntent().getStringExtra("userEmail");
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

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentUser != null) {
                    goToActivity(MainActivity.class);
                    finish(); // if user in app, won't back by intent
                } else {
                    goToActivity(EmailPasswordActivity.class);
                }
            }
        });
    }


    private void getRecommendedEvents() { // download 3
        String city = localDataManager.getStringData("city", "Amsterdam");

        TicketmasterApiService apiService = RetrofitClient.getApiService();
        apiService.getEvents(TICKETMASTER_API_KEY, city, "")
                .enqueue(new Callback<EventResponse>() {
                    @Override
                    public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getEmbedded() != null) {
                                    RecommendedEventManager.getInstance().recommendedEvents = response.body().getEmbedded().getEvents();
                                }
                            }
                        }
                        letsGo();
                    }

                    @Override
                    public void onFailure(Call<EventResponse> call, Throwable t) {
                        letsGo();
                        Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                    }
                });
    }

    private void letsGo() { // end process
        binding.nextButton.setEnabled(true); // u can go
        binding.getStartText.setText("Haydi Başlayalım !");
        handler.removeCallbacks(runnable);
    }

    private void downloadData(String email) {
        binding.nextButton.setEnabled(false);
        getPp(email);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = ". ".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.getStartText.setText("Yükleniyor " + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(OnBoardingActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void getPp(String email) { // download 1
        updateLoadingText(); // start process
        db.collection("Users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            getFavoriteEvents(email);
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                                String ppUrl = document.getString("profileImageUrl");
                                if (ppUrl != null) {
                                    UserManager.getInstance().ppUrl = ppUrl;
                                }
                            }
                        }
                    }
                });
    }

    private void getFavoriteEvents(String userEmail) { // download 2
        String path = userEmail + "_Events";
        db.collection(path).orderBy("eventName").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (UserFavoritesManager.getInstance().userFavorites != null) {
                                UserFavoritesManager.getInstance().userFavorites.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String eventID = (String) document.get("eventID");
                                String imageUrl = (String) document.get("imageUrl");
                                String eventName = (String) document.get("eventName");

                                System.out.println(eventID);
                                System.out.println(eventName);
                                System.out.println(imageUrl);

                                FavoriteEventModel myEventModel = new FavoriteEventModel(eventID, eventName, imageUrl);
                                UserFavoritesManager.getInstance().addFavorite(myEventModel);
                            }
                            getRecommendedEvents();
                        } else {
                            Toast.makeText(OnBoardingActivity.this, "Hata!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}