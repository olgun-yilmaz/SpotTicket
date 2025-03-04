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

import static com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity.TICKETMASTER_API_KEY;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentDisplayBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RetrofitClient;
import com.olgunyilmaz.spotticket.service.TicketmasterApiService;
import com.olgunyilmaz.spotticket.util.Categories;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DisplayFragment extends Fragment {
    private FragmentDisplayBinding binding;
    private EventAdapter eventAdapter;
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        LocalDataManager localDataManager = new LocalDataManager(requireActivity());

        String city = localDataManager.getStringData
                (getString(R.string.city_key), getString(R.string.default_city_name));

        binding.fragmentCityText.setText(city);

        String category = localDataManager.getStringData
                (getString(R.string.category_key), getString(R.string.default_category_name));

        binding.fragmentCategoryText.setText(category);

        binding.selectLayout.setOnClickListener(v -> goToHomePage());

        String keyword = "";
        boolean searchByKeyword = false;

        Bundle args = getArguments();
        if (args != null) {
            searchByKeyword = args.getBoolean(getString(R.string.search_by_keyword_key), false);
            if (searchByKeyword){
                keyword = args.getString(getString(R.string.keyword_key),"");
            }
        }

        apiService = RetrofitClient.getApiService();
        findEvent(apiService, city, category, keyword,searchByKeyword);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.fragmentResultText.setText(getString(R.string.plain_loading_text) + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void goToHomePage() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.displayButton.setEnabled(true);
        activity.binding.homeButton.setEnabled(false);

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HomePageFragment fragment = new HomePageFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    private void findEvent(TicketmasterApiService service, String city, String category,
                           String keyword, boolean searchByKeyword) {
        updateLoadingText();

        if(searchByKeyword){
            keywordMode(keyword);
            city = ""; // don't save, just for this request
            category = "";
        }else{
            selectMode();
        }

        service.getEvents(TICKETMASTER_API_KEY, city, Categories.CATEGORIES.get(category), keyword)
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
                                binding.fragmentResultText.setText(events.size() + " " + getString(R.string.result_founded_text));
                                eventAdapter = new EventAdapter(events);
                                binding.recyclerView.setAdapter(eventAdapter);
                            }

                        } else {
                            try {
                                String errorMessage = getString(R.string.error_text) + response.code() + " - " + response.message();
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
                        Toast.makeText(getContext(), getString(R.string.error_text) + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectMode(){
        binding.displayKeywordText.setVisibility(View.GONE);
        binding.fragmentCategoryText.setVisibility(View.VISIBLE);
        binding.fragmentCityText.setVisibility(View.VISIBLE);
    }

    private void keywordMode(String keyword){
        binding.displayKeywordText.setText(keyword);
        binding.displayKeywordText.setVisibility(View.VISIBLE);
        binding.fragmentCategoryText.setVisibility(View.GONE);
        binding.fragmentCityText.setVisibility(View.GONE);

    }

}