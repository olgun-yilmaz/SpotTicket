package com.olgunyilmaz.spotticket.view;

import static com.olgunyilmaz.spotticket.view.MainActivity.API_KEY;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.olgunyilmaz.spotticket.databinding.ActivityEventDetailsBinding;
import com.olgunyilmaz.spotticket.model.EventDetailsResponse;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {
    private ActivityEventDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        String eventId = getIntent().getStringExtra("eventID");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        TicketmasterApiService apiService = RetrofitClient.getApiService();
        apiService.getEventDetails(eventId, API_KEY)
                .enqueue(new Callback<EventDetailsResponse>() {
                    @Override
                    public void onResponse(Call<EventDetailsResponse> call, Response<EventDetailsResponse> response) {
                        if (response.isSuccessful()) {
                            EventDetailsResponse eventDetails = response.body();

                            binding.detailsNameText.setText(eventDetails.getName());

                            binding.detailsDescriptionText.setText(eventDetails.getDescription());

                            binding.detailsDateText.setText(eventDetails.getDates().getStart().getDateTime());

                            Picasso.get().load(imageUrl).into(binding.detailsImage);

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

    private void buyTicket(View view, String url){
        System.out.println("buying ticket from : "+url);
        binding.detailsDateText.setText(url);
    }
}