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

import com.google.gson.Gson;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.CategoryAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentHomePageBinding;
import com.olgunyilmaz.spotticket.model.CategoryResponse;
import com.olgunyilmaz.spotticket.model.CitiesResponse;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.service.RecommendedEventManager;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
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
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localDataManager = new LocalDataManager(requireActivity());

        categories = new ArrayList<>();
        getCategories();

        fragmentManager = requireActivity().getSupportFragmentManager();

        if (RecommendedEventManager.getInstance().recommendedEvents != null) {
            if (!RecommendedEventManager.getInstance().recommendedEvents.isEmpty()) {
                updateImage(10); // update per 10 seconds
            }

        }

        String city = localDataManager.getStringData("city", "Ankara");
        binding.selectCityText.setText(city);

        ArrayList<String> cities = getCities();
        binding.selectCityButton.setOnClickListener(v -> showCityPicker(cities));

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
        args.putString("eventID", eventID);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);

        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    private ArrayList<String> getCities() {
        try {
            Reader reader = new InputStreamReader(getActivity().getAssets().open("cities.json"));
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
        args.putStringArrayList("cities", cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(getActivity().getSupportFragmentManager(), "cityPicker");
    }

    @Override
    public void onCitySelected(String city) {
        binding.selectCityText.setText(city);
        localDataManager.updateStringData("city", city);
    }

    private void getCategories() {
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
}