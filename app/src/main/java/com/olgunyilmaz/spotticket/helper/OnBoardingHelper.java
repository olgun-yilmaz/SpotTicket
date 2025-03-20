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

package com.olgunyilmaz.spotticket.helper;

import static android.content.ContentValues.TAG;

import static com.olgunyilmaz.spotticket.util.Constants.TICKETMASTER_API_KEY;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.model.Language;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.util.CircleTransform;
import com.olgunyilmaz.spotticket.util.RecommendedEventManager;
import com.olgunyilmaz.spotticket.util.UserFavoritesManager;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnBoardingHelper {
    private final Context context;

    public OnBoardingHelper(Context context) {
        this.context = context;
    }

    public ArrayList<Language> getLanguageData(Consumer<ArrayList<Language>> function) {
        ArrayList<Language> languageList = new ArrayList<>();
        languageList.add(new Language(R.drawable.icon_tr, "tr", "Türkçe"));
        languageList.add(new Language(R.drawable.icon_en, "en", "English"));

        if (function != null) {
            function.accept(languageList);
        } else {
            return languageList;
        }

        return null;
    }

    public void goToActivity(Class<?> activityClass, String eventName) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (eventName != null) {
            intent.putExtra(context.getString(R.string.event_name_key), eventName);
        }

        context.startActivity(intent);
    }

    public void getUserData(String email, Runnable updateLoadingText, FirebaseFirestore db, Runnable letsGo) { // download 1

        updateLoadingText.run(); // start process
        db.collection(context.getString(R.string.users_collection_key))
                .whereEqualTo(context.getString(R.string.email_key), email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getFavoriteEvents(email, db, letsGo);
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {

                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                            String ppUrl = document.getString(context.getString(R.string.profile_image_url_key));
                            String name = document.getString(context.getString(R.string.name_key));
                            String surname = document.getString(context.getString(R.string.surname_key));
                            String city = document.getString(context.getString(R.string.city_key));

                            if (ppUrl != null) { // just pp is nullable
                                loadImageToBitmap(ppUrl,name,surname,city);
                            }
                        }
                    }
                });
    }

    private void loadImageToBitmap(String imageUrl, String name,String surname, String city) {
        Picasso.get().load(imageUrl)
                .resize(1024,1024)
                .onlyScaleDown() // if smaller don't resize
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .transform(new CircleTransform())
                .into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                UserManager.getInstance().profileImage = bitmap;
                UserManager.getInstance().name = name;
                UserManager.getInstance().surname = surname;
                UserManager.getInstance().city = city;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Resim yükleme hatası durumunda yapılacak işlemler
                Log.e("Picasso", "Resim yükleme hatası: " + e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                System.out.println("");
            }
        });
    }

        private void getFavoriteEvents (String userEmail, FirebaseFirestore db, Runnable letsGo)
        { // download 2

            String path = userEmail + context.getString(R.string.my_events_key);
            db.collection(path).orderBy(context.getString(R.string.event_date_key)).get() // order by date
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (UserFavoritesManager.getInstance().userFavorites != null) {
                                UserFavoritesManager.getInstance().userFavorites.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String eventID = (String) document.get(context.getString(R.string.event_id_key));
                                String imageUrl = (String) document.get(context.getString(R.string.image_url_key));
                                String eventName = (String) document.get(context.getString(R.string.event_name_key));
                                String eventDate = (String) document.get(context.getString(R.string.event_date_key));
                                Long categoryIcon = (Long) document.get(context.getString(R.string.category_icon_key));

                                FavoriteEventModel myEventModel = new FavoriteEventModel
                                        (eventID, eventName, imageUrl, eventDate, categoryIcon);

                                UserFavoritesManager.getInstance().addFavorite(myEventModel);
                            }
                            getRecommendedEvents(letsGo);
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_text), Toast.LENGTH_LONG).show();
                        }
                    });
        }

        private void getRecommendedEvents (Runnable letsGo){ // download 3
            String city = UserManager.getInstance().city;

            TicketmasterApiService apiService = RetrofitClient.getApiService();
            apiService.getEvents(TICKETMASTER_API_KEY, city, "", "", "")
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<EventResponse> call, @NonNull Response<EventResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    if (response.body().getEmbedded() != null) {
                                        RecommendedEventManager.getInstance().recommendedEvents = response.body().getEmbedded().getEvents();
                                    }
                                }
                            }
                            letsGo.run();
                        }

                        @Override
                        public void onFailure(@NonNull Call<EventResponse> call, @NonNull Throwable t) {
                            letsGo.run();
                            Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                        }
                    });
        }
    }