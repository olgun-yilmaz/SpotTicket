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
import android.widget.Toast;

import com.olgunyilmaz.spotticket.util.CircleTransform;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.EventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentHomePageBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.util.EventDetailsHelper;
import com.olgunyilmaz.spotticket.util.HomePageHelper;
import com.olgunyilmaz.spotticket.util.RecommendedEventManager;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.Random;


public class HomePageFragment extends Fragment {
    private FragmentHomePageBinding binding;
    private FragmentManager fragmentManager;
    private Runnable runnable;
    private Handler handler;
    private HomePageHelper helper;
    private EventDetailsHelper detailsHelper;


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

        helper = new HomePageHelper(requireActivity());

        fragmentManager = requireActivity().getSupportFragmentManager();

        detailsHelper = new EventDetailsHelper(requireActivity());

        Picasso.get().load(UserManager.getInstance().ppUrl)
                    .resize(1024,1024)
                    .onlyScaleDown() // if smaller don't resize
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.sample_profile_image)
                    .transform(new CircleTransform())
                    .into(binding.homeProfileImage);


        binding.homeUsernameText.setText("Olgun Yılmaz");

        binding.seeAllText.setOnClickListener(v -> seeAll());

        binding.homeProfileImage.setOnClickListener(v -> goToSettings());


        if (RecommendedEventManager.getInstance().recommendedEvents != null) {
            if (!RecommendedEventManager.getInstance().recommendedEvents.isEmpty()) {
                updateEvent(10); // update per 10 seconds
            }

        }

        binding.homeSearchIcon.setOnClickListener(v -> searchEventByKeyword());
        binding.homeSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onWriting) {
                if (onWriting) {
                    writingMode();
                } else {
                    normalMode();
                }
            }
        });

        binding.upcomingEventsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        displayUpcomingEvents();

    }

    private void goToSettings(){
        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.profileButton.setChecked(true);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfileFragment fragment = new ProfileFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView,fragment).commit();
    }

    private void displayUpcomingEvents(){
        EventAdapter eventAdapter = new EventAdapter(RecommendedEventManager.getInstance().recommendedEvents);
        binding.upcomingEventsRecyclerView.setAdapter(eventAdapter);
        eventAdapter.notifyDataSetChanged();
    }

    private void seeAll(){
        MainActivity activity = (MainActivity) requireActivity();
        activity.binding.displayButton.setChecked(true);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DisplayFragment fragment = new DisplayFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView,fragment).commit();
    }

    private void searchEventByKeyword() {
        String keyword = binding.homeSearchEditText.getText().toString();

        if (keyword.length() >= 3) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DisplayFragment fragment = new DisplayFragment();

            helper.setEnableHomeButton();

            Bundle args = new Bundle();
            args.putBoolean(getString(R.string.search_by_keyword_key), true);
            args.putString(getString(R.string.keyword_key), keyword);
            fragment.setArguments(args);

            fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
        } else {
            Toast.makeText(requireActivity(), getString(R.string.weak_search_error), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateEvent(int frequency) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
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
            }
        };
        handler.post(runnable);
    }

    private void seeEventDetails(String eventID, String imageUrl, String eventDate) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        EventDetailsFragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_key), eventID);
        args.putString(getString(R.string.image_url_key), imageUrl);
        args.putString(getString(R.string.event_date_key), eventDate);
        fragment.setArguments(args);

        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }


    private void writingMode() {
        helper.setEnableHomeButton();

        binding.profileLayout.setVisibility(View.INVISIBLE);
        binding.upcomingEventsLayout.setVisibility(View.INVISIBLE);
        binding.dateLayout.setVisibility(View.INVISIBLE);
        binding.upcomingEventsLayout.setVisibility(View.INVISIBLE);
        binding.recommendedEventLayout.setVisibility(View.INVISIBLE);
        binding.upcomingEventsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void normalMode() {
        binding.profileLayout.setVisibility(View.VISIBLE);
        binding.upcomingEventsLayout.setVisibility(View.VISIBLE);
        binding.dateLayout.setVisibility(View.VISIBLE);
        binding.upcomingEventsLayout.setVisibility(View.VISIBLE);
        binding.recommendedEventLayout.setVisibility(View.VISIBLE);
        binding.upcomingEventsRecyclerView.setVisibility(View.VISIBLE);
    }
}