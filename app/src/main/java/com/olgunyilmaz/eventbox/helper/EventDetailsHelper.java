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

package com.olgunyilmaz.eventbox.helper;

import static android.content.ContentValues.TAG;
import static com.olgunyilmaz.eventbox.util.Constants.MAPS_API_KEY;
import static com.olgunyilmaz.eventbox.util.Constants.MAPS_BASE_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.olgunyilmaz.eventbox.R;
import com.olgunyilmaz.eventbox.model.EventResponse;
import com.olgunyilmaz.eventbox.model.FavoriteEventModel;
import com.olgunyilmaz.eventbox.model.GeocodingResponse;
import com.olgunyilmaz.eventbox.service.GeocodingService;
import com.olgunyilmaz.eventbox.util.LastSearchManager;
import com.olgunyilmaz.eventbox.util.LocalDataManager;
import com.olgunyilmaz.eventbox.util.UserFavoritesManager;
import com.olgunyilmaz.eventbox.view.activities.MainActivity;
import com.olgunyilmaz.eventbox.view.activities.MapsActivity;
import com.olgunyilmaz.eventbox.view.fragments.DisplayFragment;
import com.olgunyilmaz.eventbox.view.fragments.FavoritesFragment;
import com.olgunyilmaz.eventbox.view.fragments.HomePageFragment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventDetailsHelper { // simplifies event data management, reducing clutter in the Event Details fragment.
    private double venueLatitude;
    private double venueLongitude;
    private String venueName;
    private final MainActivity activity;
    public EventDetailsHelper(MainActivity activity) {
        this.activity = activity;
        venueName = activity.getString(R.string.default_venue_name);
        venueLatitude = Double.parseDouble(activity.getString(R.string.default_venue_latitude));
        venueLongitude = Double.parseDouble(activity.getString(R.string.default_venue_longitude));
    }

    private String getVenueName() {
        return venueName;
    }

    private double getVenueLatitude() {
        return venueLatitude;
    }

    private double getVenueLongitude() {
        return venueLongitude;
    }

    public String getFormattedDate(String eventDate, boolean shouldShowHours) { // return formatted date
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && eventDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            ZonedDateTime dateTime = ZonedDateTime.parse(eventDate, formatter);
            ZonedDateTime localDateTime = dateTime.withZoneSameInstant(ZoneId.systemDefault());


            String format = (shouldShowHours)
                    ? "dd/MM/yyyy - HH:mm"
                    : "dd MMMM yyyy";

            String code = new LocalDataManager(activity).
                    getStringData(activity.getString(R.string.language_code_key),"tr");

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(format,new Locale(code));
            return localDateTime.format(outputFormatter);
        }
        return activity.getString(R.string.date_not_founded_text);
    }
    public String getVenueInfo(EventResponse.Event eventDetails, List venues) {
        if (venues != null) {
            EventResponse.Venue venue = eventDetails.getEmbedded().getVenues().get(0);

            venueName = venue.getName() + " " + venue.getCity().getName();

            venueName = removeDuplicateWords(venueName); // show this in TextView

            findVenueLocation(getSearchAddress(venueName)); // find venue location

            return venueName;
        }
        return "";
    }
    private String getSearchAddress(String address) { // encode it to url format example " " -> "%"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return URLEncoder.encode(address, StandardCharsets.UTF_8);
            } else {
                return URLEncoder.encode(address, "UTF-8");
            }
        } catch (Exception e) {
            return address;
        }
    }
    public String getEventSegmentInfo(EventResponse.Event eventDetails, List classifications) {
        if (classifications != null) {
            EventResponse.Classification classification = eventDetails.getClassifications().get(0);
            return classification.getSegment().getName(); // like -> Music,Sports etc.
        }
        return "";
    }
    private void findVenueLocation(String address) { // get location using by maps api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);
        Call<GeocodingResponse> call = service.getLatLng(address, MAPS_API_KEY);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse != null && !geocodingResponse.getResults().isEmpty()) {

                        // get(0) -> get first result

                        double lat = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLat();
                        double lng = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLng();
                        venueLatitude = lat;
                        venueLongitude = lng;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to get LatLng: " + t.getMessage());
            }
        });
    }

    private String removeDuplicateWords(String str) { // example : stadium istanbul istanbul -> stadium istanbul
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        String previousWord = "";

        for (String word : words) {
            if (!word.equals(previousWord)) {
                result.append(word).append(" ");
                previousWord = word;
            }
        }

        return result.toString().trim();
    }

    public Long getCategoryIconId(String category){ // get notification icon by category name
        long id = R.drawable.electro; // default icon
        switch (category){
            case "Film"  : id = R.drawable.movie; break;
            case "Music" : id = R.drawable.music; break;
            case "Arts & Theatre" : id = R.drawable.theater; break;
            case "Sports" : id = R.drawable.sports;
        }
        return id;
    }

    public void buyTicket(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }

    public void goToEvent() {
        Intent intent = new Intent(activity, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(activity.getString(R.string.venue_latitude_key), getVenueLatitude());
        intent.putExtra(activity.getString(R.string.venue_longitude_key), getVenueLongitude());
        intent.putExtra(activity.getString(R.string.venue_name_key), getVenueName());
        activity.startActivity(intent);
    }

    public boolean isLiked(String eventId) {
        for (FavoriteEventModel event : UserFavoritesManager.getInstance().userFavorites) {
            if (eventId.equals(event.getEventId())) {
                return true;
            }
        }
        return false;
    }

    public void goBack(String fromWhere) {
        Fragment fragment = null;
        if (fromWhere.equals(activity.getString(R.string.from_home))) {
            fragment = new HomePageFragment();
            activity.binding.homeButton.setChecked(true);
        } else if (fromWhere.equals(activity.getString(R.string.from_favourite))) {
            fragment = new FavoritesFragment();
            activity.binding.myEventsButton.setChecked(true);
        } else if (fromWhere.equals(activity.getString(R.string.from_display))) {
            fragment = new DisplayFragment(LastSearchManager.getInstance().sender);
            activity.binding.displayButton.setChecked(true);
        }

        if(fragment != null){
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            activity.binding.fixedBar.setVisibility(View.VISIBLE);
            fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
        }
    }
}
