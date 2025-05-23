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

import static android.content.ContentValues.TAG;
import static com.olgunyilmaz.eventbox.util.Constants.TICKETMASTER_API_KEY;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.olgunyilmaz.eventbox.R;
import com.olgunyilmaz.eventbox.model.EventResponse;
import com.olgunyilmaz.eventbox.util.UserFavoritesManager;
import com.olgunyilmaz.eventbox.databinding.FragmentEventDetailsBinding;
import com.olgunyilmaz.eventbox.model.FavoriteEventModel;
import com.olgunyilmaz.eventbox.service.RetrofitClient;
import com.olgunyilmaz.eventbox.service.TicketmasterApiService;
import com.olgunyilmaz.eventbox.helper.EventDetailsHelper;
import com.olgunyilmaz.eventbox.util.LocalDataManager;
import com.olgunyilmaz.eventbox.util.UserManager;
import com.olgunyilmaz.eventbox.view.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsFragment extends Fragment {

    private FragmentEventDetailsBinding binding;
    private FirebaseFirestore db;
    private String collectionPath;
    private String eventId;
    private String eventName;
    private EventDetailsHelper detailsHelper;
    private LocalDataManager localDataManager;
    private String fromWhere;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        localDataManager = new LocalDataManager(requireActivity());

        String languageCode = localDataManager.getStringData(getString(R.string.language_code_key), "tr");
        auth.setLanguageCode(languageCode);

        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.fixedBar.setVisibility(View.GONE);

        detailsHelper = new EventDetailsHelper(activity);

        collectionPath = UserManager.getInstance().email + getString(R.string.my_events_key);

        Bundle args = getArguments();
        if (args != null) {
            fromWhere = args.getString(getString(R.string.from_key), getString(R.string.from_home));
            eventId = args.getString(getString(R.string.event_id_key));
            String imageUrl = args.getString(getString(R.string.image_url_key));
            eventName = args.getString(getString(R.string.event_name_key));
            String eventDate = args.getString(getString(R.string.event_date_key));
            Long categoryIconId = args.getLong(getString(R.string.category_icon_key));

            if (detailsHelper.isLiked(eventId)) {
                binding.favCheckBox.setChecked(true);
                binding.favCheckBox.setBackgroundResource(R.drawable.fav_filled_icon);
            }

            binding.favCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int imgId;
                if (isChecked) {
                    imgId = R.drawable.fav_filled_icon;
                    addFavorite(eventId, eventName, imageUrl, eventDate, categoryIconId);
                } else {
                    imgId = R.drawable.fav_empty_icon;
                    removeFavorite(eventId);
                }
                binding.favCheckBox.setBackgroundResource(imgId);
            });

            binding.detailsBackButton.setOnClickListener(v -> detailsHelper.goBack(fromWhere));

            binding.detailsLocationIcon.setOnClickListener(v -> detailsHelper.goToEvent());

            TicketmasterApiService apiService = RetrofitClient.getApiService();
            findEventDetails(apiService, eventId, imageUrl);
        }
    }



    private void addFavorite(String eventId, String eventName, String imageUrl, String eventDate, Long categoryIconId) {
        Map<String, Object> favorite = new HashMap<>();
        favorite.put(getString(R.string.event_id_key), eventId);
        favorite.put(getString(R.string.event_name_key), eventName);
        favorite.put(getString(R.string.image_url_key), imageUrl);
        favorite.put(getString(R.string.event_date_key), eventDate);
        favorite.put(getString(R.string.category_icon_key), categoryIconId);

        db.collection(collectionPath)
                .add(favorite)
                .addOnSuccessListener(documentReference -> {
                    UserFavoritesManager.getInstance().addFavorite(
                            new FavoriteEventModel(eventId, eventName, imageUrl, eventDate, categoryIconId)
                    );
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    private void removeFavorite(String eventId) {
        db.collection(collectionPath)
                .whereEqualTo(getString(R.string.event_id_key), eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        UserFavoritesManager.getInstance().removeFavorite(eventId);
                                        localDataManager.deleteData(eventId);
                                    });
                        }
                    }
                });
    }

    private void findEventDetails(TicketmasterApiService apiService, String eventId, String imageUrl) {
        apiService.getEventDetails(eventId, TICKETMASTER_API_KEY)
                .enqueue(new Callback<>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<EventResponse.Event> call, @NonNull Response<EventResponse.Event> response) {
                        if (response.isSuccessful()) {
                            EventResponse.Event event = response.body();

                            assert event != null;
                            String eventDate = event.getDates().getStart().getDateTime();

                            eventName = event.getName();
                            String eventLocation = detailsHelper.getVenueInfo(event, event.getEmbedded().getVenues());

                            String full_date = detailsHelper.getFormattedDate(eventDate, true);

                            binding.detailsNameText.setText(eventName);

                            binding.detailsLocationText.setText(event.getEmbedded().getVenues().get(0).getCity().getName());

                            binding.detailsDescriptionText.setText(
                                    getString(R.string.event_description_text, eventName, eventLocation, full_date));

                            binding.detailsDateText.setText(detailsHelper.getFormattedDate(eventDate, false));

                            Picasso.get().
                                    load(imageUrl)
                                    .resize(1024, 1024)
                                    .onlyScaleDown() // if smaller don't resize
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.error)
                                    .into(binding.detailsImage);

                            binding.buyTicketButton.setOnClickListener(v -> detailsHelper.buyTicket(event.getUrl()));
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<EventResponse.Event> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }
}