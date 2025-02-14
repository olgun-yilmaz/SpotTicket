package com.olgunyilmaz.spotticket.service;

import com.olgunyilmaz.spotticket.model.GeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {

    @GET("geocode/json")
    Call<GeocodingResponse> getLatLng(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}
