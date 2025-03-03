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

package com.olgunyilmaz.spotticket.util;

import static android.content.ContentValues.TAG;
import static com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity.MAPS_API_KEY;
import static com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity.MAPS_BASE_URL;

import android.os.Build;
import android.util.Log;

import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.model.GeocodingResponse;
import com.olgunyilmaz.spotticket.service.GeocodingService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventDetailsHelper {
    private double venueLatitude = 40.98780984859083;
    private double venueLongitude = 29.03689029646077;
    private String venueName = "Ülker Stadyumu";

    public String getVenueName() {
        return venueName;
    }

    public double getVenueLatitude() {
        return venueLatitude;
    }

    public double getVenueLongitude() {
        return venueLongitude;
    }

    public String getFormattedDate(String eventDate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && eventDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            ZonedDateTime dateTime = ZonedDateTime.parse(eventDate, formatter);
            ZonedDateTime localDateTime = dateTime.withZoneSameInstant(ZoneId.systemDefault());
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
            String formattedDate = localDateTime.format(outputFormatter);
            return formattedDate;
        }
        return "Tarih bulunamadı";
    }
    public String getVenueInfo(EventResponse.Event eventDetails, List venues) {
        if (venues != null) {
            EventResponse.Venue venue = eventDetails.getEmbedded().getVenues().get(0);

            venueName = venue.getName() + " " + venue.getCity().getName();

            findVenueLocation(getSearchAddress(venueName));

            return venueName;
        }
        return "";
    }
    private String getSearchAddress(String address) {
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
            return classification.getSegment().getName();
        }
        return "";
    }
    private void findVenueLocation(String address) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);
        Call<GeocodingResponse> call = service.getLatLng(address, MAPS_API_KEY);

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse != null && !geocodingResponse.getResults().isEmpty()) {
                        double lat = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLat();
                        double lng = geocodingResponse.getResults().get(0).getGeometry().getLocation().getLng();
                        venueLatitude = lat;
                        venueLongitude = lng;
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Failed to get LatLng: " + t.getMessage());
            }
        });
    }
}
