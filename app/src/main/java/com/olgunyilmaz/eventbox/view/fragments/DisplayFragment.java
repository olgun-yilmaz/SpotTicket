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

import static com.olgunyilmaz.eventbox.util.Constants.TICKETMASTER_API_KEY;

import android.annotation.SuppressLint;
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

import com.olgunyilmaz.eventbox.R;
import com.olgunyilmaz.eventbox.adapter.SearchAdapter;
import com.olgunyilmaz.eventbox.databinding.FragmentDisplayBinding;
import com.olgunyilmaz.eventbox.model.EventResponse;
import com.olgunyilmaz.eventbox.service.RetrofitClient;
import com.olgunyilmaz.eventbox.service.TicketmasterApiService;
import com.olgunyilmaz.eventbox.util.Categories;
import com.olgunyilmaz.eventbox.helper.DisplayHelper;
import com.olgunyilmaz.eventbox.util.LastSearchManager;
import com.olgunyilmaz.eventbox.util.LocalDataManager;
import com.olgunyilmaz.eventbox.util.RecommendedEventManager;
import com.olgunyilmaz.eventbox.util.UserManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DisplayFragment extends Fragment implements FilterDialogFragment.FilterDialogListener {
    private FragmentDisplayBinding binding;
    private SearchAdapter searchAdapter;
    private Runnable runnable;
    private Handler handler;
    private DisplayHelper helper;
    private LocalDataManager localDataManager;
    private int counter = 0;
    private boolean shouldCityShow;
    private final String fromWhere;

    public DisplayFragment(){
        this.fromWhere = "";
    }

    public DisplayFragment(String fromWhere) {
        this.fromWhere = fromWhere;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDisplayBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        localDataManager = new LocalDataManager(requireActivity());

        helper = new DisplayHelper(requireActivity());

        String city = localDataManager.getStringData
                (getString(R.string.city_key), UserManager.getInstance().city);

        binding.fragmentCityText.setText(city);

        String category = localDataManager.getStringData
                (getString(R.string.category_key), getString(R.string.default_category_name));

        binding.fragmentCategoryText.setText(category);

        binding.selectLayout.setOnClickListener(v -> {
            FilterDialogFragment fragment = new FilterDialogFragment();
            fragment.setTargetFragment(this, 0); // Fragment'e target olarak ayarla
            fragment.show(getParentFragmentManager(), "Filter");
        });


        String keyword = "";
        boolean searchByKeyword = false;
        boolean seeAll = false;

        Bundle args = getArguments();
        if (args != null) {
            seeAll = args.getBoolean(getString(R.string.see_all_key),false);
            searchByKeyword = args.getBoolean(getString(R.string.search_by_keyword_key), false);
            if (searchByKeyword) {
                keyword = args.getString(getString(R.string.keyword_key), "");
            }
        }

        if(fromWhere.isEmpty()){
            if(seeAll){ // show default 20 events
                keywordMode(getString(R.string.recommended_events_text));
                binding.fragmentResultText.setText("");
                LastSearchManager.getInstance().lastEvents = RecommendedEventManager.getInstance().recommendedEvents;
                searchAdapter = new SearchAdapter(RecommendedEventManager.getInstance().recommendedEvents, true);
                binding.recyclerView.setAdapter(searchAdapter);
                LastSearchManager.getInstance().sender = getString(R.string.from_recommended);

            }else{ // search events with api
                findEvent(city, category, "", keyword, searchByKeyword);
            }
            
        }else{ // back from details
            if(fromWhere.equals(getString(R.string.from_keyword))){
                shouldCityShow = true;
                keywordMode(localDataManager.getStringData(getString(R.string.keyword_key),""));

            } else if (fromWhere.equals(getString(R.string.from_api))) {
                selectMode();
                shouldCityShow = false;

            } else if (fromWhere.equals(getString(R.string.from_recommended))) {
                shouldCityShow = true;
                keywordMode(getString(R.string.recommended_events_text));
            }
            binding.fragmentResultText.setText("");
            searchAdapter = new SearchAdapter(LastSearchManager.getInstance().lastEvents, shouldCityShow);
            binding.recyclerView.setAdapter(searchAdapter);
        }
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = () -> {
            if (isAdded()) {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.fragmentResultText.setText(String.format("%s%s", getString(R.string.plain_loading_text), numPointText));
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void findEvent(String city, String category, String date,
                           String keyword, boolean searchByKeyword) {
        updateLoadingText();
        date = helper.formatDate(date);

        if (searchByKeyword) {
            keywordMode(keyword);
            city = ""; // don't save, just for this request
            category = "";
            shouldCityShow = true;
            LastSearchManager.getInstance().sender = getString(R.string.from_keyword);
        } else {
            shouldCityShow = false; // all events in one city
            selectMode();
            LastSearchManager.getInstance().sender = getString(R.string.from_api);
        }

        Categories categories = new Categories(requireContext());
        categories.loadCategories();

        TicketmasterApiService apiService = RetrofitClient.getApiService();

        apiService.getEvents(TICKETMASTER_API_KEY, city, categories.CATEGORIES.get(category), keyword, date)
                .enqueue(new Callback<>() {
                    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
                    @Override
                    public void onResponse(@NonNull Call<EventResponse> call, @NonNull Response<EventResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<EventResponse.Event> events = new ArrayList<>();
                                if (response.body().getEmbedded() != null) {
                                    events = response.body().getEmbedded().getEvents();
                                    LastSearchManager.getInstance().lastEvents = events;
                                }
                                handler.removeCallbacks(runnable);
                                binding.fragmentResultText.setText(String.format("%d %s", events.size(), getString(R.string.result_founded_text)));
                                searchAdapter = new SearchAdapter(events, shouldCityShow);
                                binding.recyclerView.setAdapter(searchAdapter);
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
                                System.out.println(e.getMessage());
                            }
                        }
                        searchAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.error_text) + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectMode() {
        binding.displayKeywordText.setVisibility(View.GONE);
        binding.fragmentCategoryText.setVisibility(View.VISIBLE);
        binding.fragmentCityText.setVisibility(View.VISIBLE);
    }

    private void keywordMode(String keyword) {
        localDataManager.updateStringData(getString(R.string.keyword_key),keyword);
        binding.displayKeywordText.setText(helper.capitalizeWords(keyword));
        binding.displayKeywordText.setVisibility(View.VISIBLE);
        binding.fragmentCategoryText.setVisibility(View.GONE);
        binding.fragmentCityText.setVisibility(View.GONE);
    }

    @Override
    public void onFiltersSelected(String date, String city, String category) {
        localDataManager.updateStringData(getString(R.string.city_key),city);
        localDataManager.updateStringData(getString(R.string.category_key),category);
        localDataManager.updateStringData(getString(R.string.event_date_key),date);

        binding.fragmentCityText.setText(city);
        binding.fragmentCategoryText.setText(category);

        findEvent(city,category,date,"",false);
    }
}