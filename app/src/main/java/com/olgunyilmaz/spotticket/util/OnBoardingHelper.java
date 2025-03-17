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

package com.olgunyilmaz.spotticket.util;

import static android.content.ContentValues.TAG;

import static com.olgunyilmaz.spotticket.util.Constants.TICKETMASTER_API_KEY;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.model.Language;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;

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

    public ArrayList<Language> getLanguageData(Consumer<ArrayList<Language>> function){
        ArrayList<Language> languageList = new ArrayList<>();
        languageList.add(new Language(R.drawable.icon_tr,"tr","Türkçe"));
        languageList.add(new Language(R.drawable.icon_en,"en","English"));
        languageList.add(new Language(R.drawable.icon_de,"de","Deutsch"));
        languageList.add(new Language(R.drawable.icon_fr,"fr","Français"));
        languageList.add(new Language(R.drawable.icon_it,"it","Italiano"));
        languageList.add(new Language(R.drawable.icon_ko,"ko","한국인"));
        languageList.add(new Language(R.drawable.icon_es,"es","Español"));
        languageList.add(new Language(R.drawable.icon_ru,"ru","Русский"));

        if(function != null){
            function.accept(languageList);
        }else{
            return languageList;
        }

        return null;
    }

    public void goToActivity(Class<?> activityClass,String eventName) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(eventName != null){
            intent.putExtra(context.getString(R.string.event_name_key),eventName);
        }

        context.startActivity(intent);
    }

    public void getUserData(String email, Runnable updateLoadingText, FirebaseFirestore db,
                            LocalDataManager ldm, Runnable letsGo) { // download 1

        updateLoadingText.run(); // start process
        db.collection(context.getString(R.string.users_collection_key))
                .whereEqualTo(context.getString(R.string.email_key), email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            getFavoriteEvents(email, db, ldm, letsGo);
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                                String ppUrl = document.getString(context.getString(R.string.profile_image_url_key));
                                String name = document.getString(context.getString(R.string.name_key));
                                String surname = document.getString(context.getString(R.string.surname_key));
                                String city = document.getString(context.getString(R.string.city_key));
                                if (ppUrl != null) { // just pp is nullable
                                    UserManager.getInstance().ppUrl = ppUrl;
                                    UserManager.getInstance().name = name;
                                    UserManager.getInstance().surname = surname;
                                    UserManager.getInstance().city = city;
                                }
                            }
                        }
                    }
                });
    }

    private void getFavoriteEvents(String userEmail, FirebaseFirestore db,
                                   LocalDataManager ldm, Runnable letsGo) { // download 2

        String path = userEmail + context.getString(R.string.my_events_key);
        db.collection(path).orderBy(context.getString(R.string.event_date_key)).get() // order by date
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (UserFavoritesManager.getInstance().userFavorites != null) {
                                UserFavoritesManager.getInstance().userFavorites.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String eventID = (String) document.get(context.getString(R.string.event_id_key));
                                String imageUrl = (String) document.get(context.getString(R.string.image_url_key));
                                String eventName = (String) document.get(context.getString(R.string.event_name_key));
                                String eventDate = (String) document.get(context.getString(R.string.event_date_key));
                                Long categoryIcon = (Long) document.get(context.getString(R.string.category_icon_key));

                                FavoriteEventModel myEventModel = new FavoriteEventModel(eventID, eventName, imageUrl,eventDate,categoryIcon);
                                UserFavoritesManager.getInstance().addFavorite(myEventModel);
                            }
                            getRecommendedEvents(ldm, letsGo);
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_text), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getRecommendedEvents(LocalDataManager localDataManager, Runnable letsGo) { // download 3
        //String city = localDataManager.getStringData
                //(context.getString(R.string.city_key), context.getString(R.string.default_city_name));

        String city = UserManager.getInstance().city;

        TicketmasterApiService apiService = RetrofitClient.getApiService();
        apiService.getEvents(TICKETMASTER_API_KEY, city, "", "")
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
                        letsGo.run();
                    }

                    @Override
                    public void onFailure(Call<EventResponse> call, Throwable t) {
                        letsGo.run();
                        Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                    }
                });
    }
}