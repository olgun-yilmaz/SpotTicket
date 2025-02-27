package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.Context.MODE_PRIVATE;

import static com.olgunyilmaz.spotticket.view.activities.MainActivity.TICKETMASTER_API_KEY;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentDisplayBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.util.Categories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DisplayFragment extends Fragment {
    private FragmentDisplayBinding binding;
    private EventAdapter eventAdapter;

    SharedPreferences sharedPreferences;
    TicketmasterApiService apiService;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDisplayBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        String city = sharedPreferences.getString("city", "Ankara");
        binding.fragmentCityText.setText(city);

        String category = sharedPreferences.getString("category","Pop");
        binding.fragmentCategoryText.setText(category);

        apiService = RetrofitClient.getApiService();
        findEvent(apiService, city, category);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = ". ".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.fragmentResultText.setText("Aranıyor " + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void findEvent(TicketmasterApiService apiService, String city, String category) {
        updateLoadingText();
        apiService.getEvents(TICKETMASTER_API_KEY, city, Categories.CATEGORIES.get(category))
                .enqueue(new Callback<EventResponse>() {
                    @Override
                    public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<EventResponse.Event> events = new ArrayList<>();
                                if (response.body().getEmbedded() != null) {
                                    events = response.body().getEmbedded().getEvents();
                                }
                                handler.removeCallbacks(runnable);
                                binding.fragmentResultText.setText(events.size() + " SONUÇ BULUNDU");
                                eventAdapter = new EventAdapter(events);
                                binding.recyclerView.setAdapter(eventAdapter);
                            }

                        } else {
                            try {
                                String errorMessage = "Hata: " + response.code() + " - " + response.message();
                                if (response.errorBody() != null) {
                                    errorMessage += "\n" + response.errorBody().string();
                                }
                                Log.e("API Error", errorMessage);
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        eventAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<EventResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Hata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}