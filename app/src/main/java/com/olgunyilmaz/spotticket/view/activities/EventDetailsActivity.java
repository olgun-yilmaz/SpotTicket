package com.olgunyilmaz.spotticket.view.activities;

import static android.content.ContentValues.TAG;
import static com.olgunyilmaz.spotticket.view.activities.MainActivity.MAPS_API_KEY;
import static com.olgunyilmaz.spotticket.view.activities.MainActivity.MAPS_BASE_URL;
import static com.olgunyilmaz.spotticket.view.activities.MainActivity.TICKETMASTER_API_KEY;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityEventDetailsBinding;
import com.olgunyilmaz.spotticket.model.EventDetailsResponse;
import com.olgunyilmaz.spotticket.model.GeocodingResponse;
import com.olgunyilmaz.spotticket.service.GeocodingService;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventDetailsActivity extends AppCompatActivity {
    private ActivityEventDetailsBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String collectionPath;
    private String eventId;

    private double venueLatitude = 40.98780984859083;
    private double venueLongitude = 29.03689029646077;
    private String venueName = "Ülker Stadyumu";

    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        String userEmail = auth.getCurrentUser().getEmail().toString();
        collectionPath = userEmail + "_Events";

        eventId = getIntent().getStringExtra("eventID");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        boolean isLiked = getIntent().getBooleanExtra("isLiked",false);

        if (isLiked){
            binding.favCheckBox.setChecked(true);
            binding.favCheckBox.setButtonDrawable(R.drawable.fav_filled_icon);
        }

        binding.favCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int imgId;
            if (isChecked) {
                imgId = R.drawable.fav_filled_icon;
                likeEvent(eventId, imageUrl, eventName);
            } else {
                imgId = R.drawable.fav_empty_icon;
                unLikeEvent(eventId);
            }
            binding.favCheckBox.setButtonDrawable(imgId);
        });


        TicketmasterApiService apiService = RetrofitClient.getApiService();
        findEventDetails(apiService, eventId, imageUrl);
    }

    private void likeEvent(String eventId, String imgUrl, String eventName) {
        Map<String, Object> userEvent = new HashMap<>();
        userEvent.put("eventID", eventId);
        userEvent.put("imageUrl", imgUrl);
        userEvent.put("eventName", eventName);

        db.collection(collectionPath).add(userEvent).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    private void unLikeEvent(String eventId) {
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
                    @Override
                    public void onResponse(Call<EventDetailsResponse> call, Response<EventDetailsResponse> response) {
                        if (response.isSuccessful()) {
                            EventDetailsResponse eventDetails = response.body();

                            eventName = eventDetails.getName();

                            binding.detailsNameText.setText(eventName);

                            binding.detailsDescriptionText.setText(eventId);

                            String eventDate = eventDetails.getDates().getStart().getDateTime();

                            binding.detailsDateText.setText("Tarih : " + getFormattedDate(eventDate));

                            Picasso.get().load(imageUrl).into(binding.detailsImage);

                            String info = "";

                            info += "Etkinlik türü : " + getEventSegmentInfo(eventDetails, eventDetails.getClassifications());

                            info += "\n\nEtkinlik Mekanı : " + getVenueInfo(eventDetails, eventDetails.getEmbedded().getVenues());


                            binding.detailsDescriptionText.setText(info);

                            binding.buyTicketButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    buyTicket(view, eventDetails.getUrl());
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(Call<EventDetailsResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


    }

    private String getFormattedDate(String eventDate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            ZonedDateTime dateTime = ZonedDateTime.parse(eventDate, formatter);
            ZonedDateTime localDateTime = dateTime.withZoneSameInstant(ZoneId.systemDefault());
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
            String formattedDate = localDateTime.format(outputFormatter);
            return formattedDate;
        }
        return eventDate;
    }

    private String getVenueInfo(EventDetailsResponse eventDetails, List venues) {
        if (venues != null) {
            EventDetailsResponse.Venue venue = eventDetails.getEmbedded().getVenues().get(0);

            venueName = venue.getName() + " " + venue.getCity().getName();

            findVenueLocation(getSearchAddress(venueName));

            return venueName;
        }
        return "";
    }

    private String getSearchAddress(String address) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return URLEncoder.encode(address, StandardCharsets.UTF_8);
            } else {
                return URLEncoder.encode(address, "UTF-8");
            }
        } catch (Exception e) {
            return address;
        }
    }

    public void goToEvent(View view) {
        Intent intent = new Intent(EventDetailsActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("venueLatitude", venueLatitude);
        intent.putExtra("venueLongitude", venueLongitude);
        intent.putExtra("venueName", venueName);
        startActivity(intent);
    }

    private String getEventSegmentInfo(EventDetailsResponse eventDetails, List classifications) {
        if (classifications != null) {
            EventDetailsResponse.Classification classification = eventDetails.getClassifications().get(0);
            return classification.getSegment().getName();
        }
        return "";
    }

    private void findVenueLocation(String address) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);
        Call<GeocodingResponse> call = service.getLatLng(address, MAPS_API_KEY);

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse != null && !geocodingResponse.getResults().isEmpty()) {
                        double lat = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLat();
                        double lng = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLng();
                        venueLatitude = lat;
                        venueLongitude = lng;
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Failed to get LatLng: " + t.getMessage());
            }
        });
    }

    private void buyTicket(View view, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}