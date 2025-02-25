package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.Context.MODE_PRIVATE;

import static com.olgunyilmaz.spotticket.view.activities.MainActivity.TICKETMASTER_API_KEY;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentChangeCityBinding;
import com.olgunyilmaz.spotticket.model.CitiesResponse;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChangeCityFragment extends Fragment implements SelectCityFragment.CitySelectListener {
    private FragmentChangeCityBinding binding;
    private EventAdapter eventAdapter;

    SharedPreferences sharedPreferences;
    TicketmasterApiService apiService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangeCityBinding.inflate(getLayoutInflater(),container,false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        String city = sharedPreferences.getString("city","Ankara");
        binding.fragmentCityText.setText(city);

        ArrayList<String> cities = getCities();

        binding.fragmentCityText.setOnClickListener(v -> showCityPicker(cities));

        apiService = RetrofitClient.getApiService();
        findEventByCity(apiService,city);
    }

    private ArrayList<String> getCities() {
        try {
            Reader reader = new InputStreamReader(getActivity().getAssets().open("cities.json"));
            Gson gson = new Gson();
            CitiesResponse response = gson.fromJson(reader, CitiesResponse.class);

            if (response != null && response.getCities() != null) {
                return response.getCities();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showCityPicker(ArrayList <String> cities) {
        SelectCityFragment fragment = new SelectCityFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("cities",cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(getActivity().getSupportFragmentManager(), "cityPicker");
    }

    @Override
    public void onCitySelected(String city) {
        binding.fragmentCityText.setText(city);
        findEventByCity(apiService,city);
        sharedPreferences.edit().putString("city",city).apply();
    }

    private void findEventByCity(TicketmasterApiService apiService,String city){
        apiService.getEvents(TICKETMASTER_API_KEY, city)
                .enqueue(new Callback<EventResponse>() {
                    @Override
                    public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null){
                                List<EventResponse.Event> events = new ArrayList<>();
                                if (response.body().getEmbedded() != null) {
                                    events = response.body().getEmbedded().getEvents();
                                }
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