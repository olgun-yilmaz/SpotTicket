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
import static com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity.TICKETMASTER_API_KEY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.databinding.FragmentEventDetailsBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.util.EventDetailsHelper;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.activities.MapsActivity;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        detailsHelper = new EventDetailsHelper();

        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.homeButton.setEnabled(true); // for back to home page
        activity.binding.myEventsButton.setEnabled(true); // for back to fav page
        activity.binding.displayButton.setEnabled(true); // for back to display page

        String userEmail = auth.getCurrentUser().getEmail().toString();
        collectionPath = userEmail + "_Events";

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventID");
            System.out.println(eventId);
            String imageUrl = args.getString("imageUrl");
            eventName = args.getString("eventName");

            if (isLiked()) {
                binding.favCheckBox.setChecked(true);
                binding.favCheckBox.setButtonDrawable(R.drawable.fav_filled_icon);
            }

            binding.favCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int imgId;
                if (isChecked) {
                    imgId = R.drawable.fav_filled_icon;
                    addFavorite(eventId, eventName, imageUrl);
                } else {
                    imgId = R.drawable.fav_empty_icon;
                    removeFavorite(eventId);
                }
                binding.favCheckBox.setButtonDrawable(imgId);
            });

            binding.mapButton.setOnClickListener(v -> goToEvent());

            TicketmasterApiService apiService = RetrofitClient.getApiService();
            findEventDetails(apiService, eventId, imageUrl);
        }
    }

    private boolean isLiked() {
        for (FavoriteEventModel event : UserFavoritesManager.getInstance().userFavorites) {
            if (eventId.equals(event.getEventId())) {
                return true;
            }
        }
        return false;
    }

    private void addFavorite(String eventId, String eventName, String imageUrl) {
        Map<String, Object> favorite = new HashMap<>();
        favorite.put("eventID", eventId);
        favorite.put("eventName", eventName);
        favorite.put("imageUrl", imageUrl);

        db.collection(collectionPath)
            .add(favorite)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                UserFavoritesManager.getInstance().addFavorite(
                    new FavoriteEventModel(eventId, eventName, imageUrl)
                );
            })
            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    private void removeFavorite(String eventId) {
        db.collection(collectionPath)
                .whereEqualTo("eventID", eventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                document.getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                UserFavoritesManager.getInstance().removeFavorite(eventId);
                                                Log.d(TAG, "Etkinlik başarıyla kaldırıldı");
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void findEventDetails(TicketmasterApiService apiService, String eventId, String imageUrl) {
        apiService.getEventDetails(eventId, TICKETMASTER_API_KEY)
                .enqueue(new Callback<EventResponse.Event>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call<EventResponse.Event> call, Response<EventResponse.Event> response) {
                        if (response.isSuccessful()) {
                            EventResponse.Event event = response.body();

                            eventName = event.getName();

                            binding.detailsNameText.setText(eventName);

                            String eventDate = event.getDates().getStart().getDateTime();

                            binding.detailsDateText.setText("Tarih : " + detailsHelper.getFormattedDate(eventDate));

                            Picasso.get().
                                    load(imageUrl)
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.error)
                                    .into(binding.detailsImage);

                            binding.detailsTypeText.setText("Etkinlik türü : " +
                                    detailsHelper.getEventSegmentInfo(event, event.getClassifications()));

                            binding.detailsVenueText.setText
                                    (detailsHelper.getVenueInfo(event, event.getEmbedded().getVenues()));

                            binding.buyTicketButton.setOnClickListener(v -> buyTicket(event.getUrl()));
                        }

                    }

                    @Override
                    public void onFailure(Call<EventResponse.Event> call, Throwable t) {
                        Toast.makeText(requireContext(),t.getLocalizedMessage(),Toast.LENGTH_LONG).show();

                    }
                });
    }

    public void goToEvent() {
        Intent intent = new Intent(getContext(), MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("venueLatitude", detailsHelper.getVenueLatitude());
        intent.putExtra("venueLongitude", detailsHelper.getVenueLongitude());
        intent.putExtra("venueName", detailsHelper.getVenueName());
        startActivity(intent);
    }

    private void buyTicket(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}