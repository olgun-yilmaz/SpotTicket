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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.databinding.FragmentEventDetailsBinding;
import com.olgunyilmaz.spotticket.model.EventDetailsResponse;
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

        String userEmail = auth.getCurrentUser().getEmail().toString();
        collectionPath = userEmail + "_Events";

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventID");
            String imageUrl = args.getString("imageUrl");

            if (isLiked()) {
                binding.favCheckBox.setChecked(true);
                binding.favCheckBox.setButtonDrawable(R.drawable.fav_filled_icon);
            }

            binding.favCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int imgId;
                if (isChecked) {
                    imgId = R.drawable.fav_filled_icon;
                    addFavorite(eventId, imageUrl, eventName);
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

    private void addFavorite(String eventId, String imgUrl, String eventName) {
        Map<String, Object> userEvent = new HashMap<>();
        userEvent.put("eventID", eventId);
        userEvent.put("imageUrl", imgUrl);
        userEvent.put("eventName", eventName);

        db.collection(collectionPath).add(userEvent).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        UserFavoritesManager.getInstance().addFavorite(new FavoriteEventModel(eventId, imgUrl, eventName));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

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
                .enqueue(new Callback<EventDetailsResponse>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call<EventDetailsResponse> call, Response<EventDetailsResponse> response) {
                        if (response.isSuccessful()) {
                            EventDetailsResponse eventDetails = response.body();

                            eventName = eventDetails.getName();

                            binding.detailsNameText.setText(eventName);

                            String eventDate = eventDetails.getDates().getStart().getDateTime();

                            binding.detailsDateText.setText("Tarih : " + detailsHelper.getFormattedDate(eventDate));

                            Picasso.get().
                                    load(imageUrl)
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.error)
                                    .into(binding.detailsImage);

                            binding.detailsTypeText.setText("Etkinlik türü : " +
                                    detailsHelper.getEventSegmentInfo(eventDetails, eventDetails.getClassifications()));

                            binding.detailsVenueText.setText
                                    (detailsHelper.getVenueInfo(eventDetails, eventDetails.getEmbedded().getVenues()));

                            binding.buyTicketButton.setOnClickListener(v -> buyTicket(eventDetails.getUrl()));
                        }
                    }

                    @Override
                    public void onFailure(Call<EventDetailsResponse> call, Throwable t) {
                        t.printStackTrace();
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