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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.gson.Gson;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.CategoryAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentHomePageBinding;
import com.olgunyilmaz.spotticket.model.CategoryResponse;
import com.olgunyilmaz.spotticket.model.CitiesResponse;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RecommendedEventManager;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class HomePageFragment extends Fragment implements SelectCityFragment.CitySelectListener {
    private FragmentHomePageBinding binding;
    private FragmentManager fragmentManager;
    private CategoryAdapter categoryAdapter;
    private ArrayList<CategoryResponse> categories;
    private Runnable runnable;
    private Handler handler;
    private LocalDataManager localDataManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomePageBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localDataManager = new LocalDataManager(requireActivity());

        getCategories();

        fragmentManager = requireActivity().getSupportFragmentManager();

        if (RecommendedEventManager.getInstance().recommendedEvents != null) {
            if (!RecommendedEventManager.getInstance().recommendedEvents.isEmpty()) {
                updateImage(10); // update per 10 seconds
            }

        }

        String city = localDataManager.getStringData(getString(R.string.city_key), getString(R.string.default_city_name));
        binding.selectCityText.setText(city);

        ArrayList<String> cities = getCities();
        binding.cityLayout.setOnClickListener(v -> showCityPicker(cities));

        binding.homeSearchIcon.setOnClickListener(v -> searchEventByKeyword());
        binding.homeSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onWriting) {
                if(onWriting){
                    writingMode();
                }else{
                    normalMode();
                }
            }
        });


        GridLayoutManager gridLayoutManager = new GridLayoutManager
                (requireContext(), 3, GridLayoutManager.HORIZONTAL, false);

        binding.categoryRecyclerView.setLayoutManager(gridLayoutManager);

        binding.categoryRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.categoryRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = binding.categoryRecyclerView.getWidth();
                int itemWidth = width / 3;

                categoryAdapter = new CategoryAdapter(categories, itemWidth);
                binding.categoryRecyclerView.setAdapter(categoryAdapter);
            }
        });
    }

    private void searchEventByKeyword(){
        String keyword = binding.homeSearchEditText.getText().toString();

        if (keyword.length() >= 3){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DisplayFragment fragment = new DisplayFragment();

            setEnableHomeButton();

            Bundle args = new Bundle();
            args.putBoolean(getString(R.string.search_by_keyword_key), true);
            args.putString(getString(R.string.keyword_key), keyword);
            fragment.setArguments(args);

            fragmentTransaction.replace(R.id.fragmentContainerView,fragment).commit();
        }else{
            Toast.makeText(requireActivity(),getString(R.string.weak_search_error),Toast.LENGTH_SHORT).show();
        }


    }

    private void setEnableHomeButton(){
        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.displayButton.setEnabled(false);
        activity.binding.homeButton.setEnabled(true);
    }

    private void updateImage(int frequency) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                Random random = new Random();
                int randomID = random.nextInt(RecommendedEventManager.getInstance().recommendedEvents.size());
                EventResponse.Event event = RecommendedEventManager.getInstance().recommendedEvents.get(randomID);

                binding.recommendedEventName.setText(event.getName());

                Picasso.get()
                        .load(event.getHighQualityImage())
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .into(binding.recommendedEventImage);


                binding.recommendedEventLayout.setOnClickListener(v ->
                        seeEventDetails(event.getId(), event.getHighQualityImage()));


                handler.postDelayed(runnable, 1000L * frequency);
            }
        };
        handler.post(runnable);
    }

    private void seeEventDetails(String eventID, String imageUrl) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        EventDetailsFragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_key), eventID);
        args.putString(getString(R.string.image_url_key), imageUrl);
        fragment.setArguments(args);

        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    private ArrayList<String> getCities() {
        try {
            Reader reader = new InputStreamReader(getActivity().getAssets().open(getString(R.string.cities_json_file)));
            Gson gson = new Gson();
            CitiesResponse response = gson.fromJson(reader, CitiesResponse.class);

            if (response != null && response.getCities() != null) {
                ArrayList<String> cities = response.getCities();
                Collections.sort(cities);
                return cities;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showCityPicker(ArrayList<String> cities) {
        SelectCityFragment fragment = new SelectCityFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.cities_key), cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(getActivity().getSupportFragmentManager(), getString(R.string.city_picker_tag));
    }

    @Override
    public void onCitySelected(String city) {
        binding.selectCityText.setText(city);
        localDataManager.updateStringData(getString(R.string.city_key), city);
    }

    private void getCategories() {
        categories = new ArrayList<>();

        categories.add(new CategoryResponse(R.drawable.family, "Aile"));
        categories.add(new CategoryResponse(R.drawable.basketball, "Basketbol"));
        categories.add(new CategoryResponse(R.drawable.baseball, "Beyzbol"));
        categories.add(new CategoryResponse(R.drawable.blues, "Blues"));
        categories.add(new CategoryResponse(R.drawable.ice_hockey, "Buz Hokeyi"));
        categories.add(new CategoryResponse(R.drawable.dancing, "Dans"));
        categories.add(new CategoryResponse(R.drawable.disney, "Disney"));
        categories.add(new CategoryResponse(R.drawable.movie, "Film"));

        categories.add(new CategoryResponse(R.drawable.folk, "Folk"));
        categories.add(new CategoryResponse(R.drawable.electro, "Elektronik Müzik"));
        categories.add(new CategoryResponse(R.drawable.hip_hop, "Hip-Hop/Rap"));
        categories.add(new CategoryResponse(R.drawable.jazz, "Jazz"));
        categories.add(new CategoryResponse(R.drawable.classical_music, "Klasik Müzik"));
        categories.add(new CategoryResponse(R.drawable.conference, "Konferans"));
        categories.add(new CategoryResponse(R.drawable.cultural, "Kültür Festivali"));
        categories.add(new CategoryResponse(R.drawable.mma, "MMA/Dövüş Sporları"));
        categories.add(new CategoryResponse(R.drawable.music, "Müzik"));
        categories.add(new CategoryResponse(R.drawable.musical, "Müzikal"));

        categories.add(new CategoryResponse(R.drawable.opera, "Opera"));
        categories.add(new CategoryResponse(R.drawable.rb, "R&B"));
        categories.add(new CategoryResponse(R.drawable.rock, "Rock"));
        categories.add(new CategoryResponse(R.drawable.pop_music, "Pop"));
        categories.add(new CategoryResponse(R.drawable.theater_art, "Sanat & Tiyatro"));
        categories.add(new CategoryResponse(R.drawable.sports, "Spor"));
        categories.add(new CategoryResponse(R.drawable.tennis, "Tenis"));
        categories.add(new CategoryResponse(R.drawable.theater, "Tiyatro"));
        categories.add(new CategoryResponse(R.drawable.food, "Yemek Festivali"));

    }

    private void writingMode(){
        setEnableHomeButton();
        binding.recommendedEventLayout.setVisibility(View.INVISIBLE);
        binding.cityLayout.setVisibility(View.INVISIBLE);
        binding.categoryRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void normalMode(){
        binding.recommendedEventLayout.setVisibility(View.VISIBLE);
        binding.cityLayout.setVisibility(View.VISIBLE);
        binding.categoryRecyclerView.setVisibility(View.VISIBLE);

    }
}