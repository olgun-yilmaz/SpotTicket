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

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.spotticket.util.CircleTransform;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentHomePageBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.helper.EventDetailsHelper;
import com.olgunyilmaz.spotticket.helper.HomePageHelper;
import com.olgunyilmaz.spotticket.util.RecommendedEventManager;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.Random;


public class HomePageFragment extends Fragment {
    public FragmentHomePageBinding binding;
    private Runnable runnable;
    private Handler handler;
    private HomePageHelper helper;
    private EventDetailsHelper detailsHelper;
    private MainActivity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomePageBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) requireActivity();

        helper = new HomePageHelper(activity,binding);

        detailsHelper = new EventDetailsHelper(activity);

        if(UserManager.getInstance().profileImage == null){
            binding.homeProfileImage.setImageResource(R.drawable.sample_profile_image);
        }else{
            binding.homeProfileImage.setImageBitmap(UserManager.getInstance().profileImage);
        }

        binding.homeUsernameText.setText(
                String.format("%s %s", UserManager.getInstance().name, UserManager.getInstance().surname));

        binding.seeAllText.setOnClickListener(v -> seeAll());

        binding.homeProfileImage.setOnClickListener(v -> goToProfile());


        if (RecommendedEventManager.getInstance().recommendedEvents != null) {
            if (!RecommendedEventManager.getInstance().recommendedEvents.isEmpty()) {
                updateEvent(10); // update per 10 seconds
            }

        }

        binding.homeSearchIcon.setOnClickListener(v -> helper.searchEventByKeyword());
        binding.homeSearchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                writingMode();
            } else {
                normalMode();
            }
        });

        binding.upcomingEventsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        displayUpcomingEvents();

    }

    private void goToProfile(){
        activity.binding.profileButton.setChecked(true);
        helper.replaceFragment(new ProfileFragment());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayUpcomingEvents(){
        EventAdapter eventAdapter = new EventAdapter(RecommendedEventManager.getInstance().recommendedEvents);
        binding.upcomingEventsRecyclerView.setAdapter(eventAdapter);
        eventAdapter.notifyDataSetChanged();
    }

    private void seeAll(){
        activity.binding.displayButton.setChecked(true);
        Bundle args = new Bundle();
        args.putBoolean(getString(R.string.see_all_key),true);
        DisplayFragment fragment = new DisplayFragment();
        fragment.setArguments(args);
        helper.replaceFragment(fragment);
    }

    private void updateEvent(int frequency) {
        handler = new Handler();
        runnable = () -> {
            if (isAdded()) {
                Random random = new Random();
                int randomID = random.nextInt(RecommendedEventManager.getInstance().recommendedEvents.size());
                EventResponse.Event event = RecommendedEventManager.getInstance().recommendedEvents.get(randomID);

                binding.recommendedEventName.setText(event.getName());

                Picasso.get()
                        .load(event.getHighQualityImage())
                        .resize(1024,1024)
                        .onlyScaleDown() // if smaller don't resize
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .into(binding.recommendedEventImage);

                String date = event.getDates().getStart().getDateTime();
                binding.homeDateText.setText(detailsHelper.getFormattedDate(date,false));


                binding.recommendedEventLayout.setOnClickListener(v ->
                        seeEventDetails(event.getId(), event.getHighQualityImage(), event.getDates().getStart().getDateTime()));


                handler.postDelayed(runnable, 1000L * frequency);

            }
        };
        handler.post(runnable);
    }

    private void seeEventDetails(String eventID, String imageUrl, String eventDate) {
        EventDetailsFragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_key), eventID);
        args.putString(getString(R.string.image_url_key), imageUrl);
        args.putString(getString(R.string.event_date_key), eventDate);
        fragment.setArguments(args);

        helper.replaceFragment(fragment);
    }


    private void writingMode() {
        helper.setEnableHomeButton();

        for(View view : helper.getHomeViews()){
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void normalMode() {
        for(View view : helper.getHomeViews()){
            view.setVisibility(View.VISIBLE);
        }
    }
}