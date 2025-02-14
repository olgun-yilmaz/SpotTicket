package com.olgunyilmaz.spotticket.view;

import static android.content.ContentValues.TAG;
import static com.olgunyilmaz.spotticket.view.MainActivity.MAPS_API_KEY;
import static com.olgunyilmaz.spotticket.view.MainActivity.MAPS_BASE_URL;
import static com.olgunyilmaz.spotticket.view.MainActivity.TICKETMASTER_API_KEY;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventDetailsActivity extends AppCompatActivity {
    private ActivityEventDetailsBinding binding;

    public double getVenueLongitude() {
        return venueLongitude;
    }

    public void setVenueLongitude(double venueLongitude) {
        this.venueLongitude = venueLongitude;
    }

    public double getVenueLatitude() {
        return venueLatitude;
    }

    public void setVenueLatitude(double venueLatitude) {
        this.venueLatitude = venueLatitude;
    }

    private double venueLatitude = 40.98780984859083;
    private double venueLongitude = 29.03689029646077;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        String eventId = getIntent().getStringExtra("eventID");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        TicketmasterApiService apiService = RetrofitClient.getApiService();
        apiService.getEventDetails(eventId, TICKETMASTER_API_KEY)
                .enqueue(new Callback<EventDetailsResponse>() {
                    @Override
                    public void onResponse(Call<EventDetailsResponse> call, Response<EventDetailsResponse> response) {
                        if (response.isSuccessful()) {
                            EventDetailsResponse eventDetails = response.body();

                            binding.detailsNameText.setText(eventDetails.getName());

                            binding.detailsDescriptionText.setText(eventId);

                            String eventDate = eventDetails.getDates().getStart().getDateTime();

                            binding.detailsDateText.setText("Tarih : "+getFormattedDate(eventDate));

                            Picasso.get().load(imageUrl).into(binding.detailsImage);

                            String info = "";

                            info += "Etkinlik Mekanı : "+getVenueInfo(eventDetails,eventDetails.getEmbedded().getVenues());

                            info += "\n\nEtkinlik türü : " + getEventSegmentInfo(eventDetails,eventDetails.getClassifications());

                            binding.detailsDescriptionText.setText(info);

                            binding.buyTicketButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    buyTicket(view,eventDetails.getUrl());
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

    private String getFormattedDate(String eventDate){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            ZonedDateTime dateTime = ZonedDateTime.parse(eventDate, formatter);
            ZonedDateTime localDateTime = dateTime.withZoneSameInstant(ZoneId.systemDefault());
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
            String formattedDate = localDateTime.format(outputFormatter);
            return formattedDate;
        }
        return eventDate;
    }

    private String getVenueInfo(EventDetailsResponse eventDetails,List venues){
        if (venues != null){
            EventDetailsResponse.Venue venue = eventDetails.getEmbedded().getVenues().get(0);
            String encodedAddress = URLEncoder.encode(venue.getName() + " " + venue.getCity().getName(), StandardCharsets.UTF_8);
            findVenueLocation(encodedAddress);

            return venue.getName()+ "/"+venue.getCity().getName();
        }
        return "";
    }

    private String getEventSegmentInfo(EventDetailsResponse eventDetails,List classifications){
        if (classifications != null){
            EventDetailsResponse.Classification classification = eventDetails.getClassifications().get(0);
            return classification.getSegment().getName();
        }
        return "";
    }

    private void findVenueLocation(String address){
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
                        setVenueLatitude(lat);
                        setVenueLongitude(lng);
                        System.out.println("Lat and Long : "+lat+" "+lng);
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Failed to get LatLng: " + t.getMessage());
            }
        });
    }

    private void buyTicket(View view, String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }
}