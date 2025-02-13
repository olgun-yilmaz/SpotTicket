package com.olgunyilmaz;

import static android.content.Context.MODE_PRIVATE;

import static com.olgunyilmaz.spotticket.view.MainActivity.API_KEY;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentChangeCityBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.view.MainActivity;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChangeCityFragment extends Fragment {
    private FragmentChangeCityBinding binding;
    private EventAdapter eventAdapter;

    FragmentManager fragmentManager;
    SharedPreferences sharedPreferences;


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

        fragmentManager = getActivity().getSupportFragmentManager();
        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket.view", MODE_PRIVATE);

        String city = sharedPreferences.getString("city","ankara");

        System.out.println(city);

        TicketmasterApiService apiService = RetrofitClient.getApiService();
        apiService.getEvents(API_KEY, city)
                .enqueue(new Callback<EventResponse>() {
                    @Override
                    public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                        if (response.isSuccessful()) {
                            List<EventResponse.Event> events = response.body().getEmbedded().getEvents();
                            eventAdapter = new EventAdapter(events);
                            binding.recyclerView.setAdapter(eventAdapter);
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