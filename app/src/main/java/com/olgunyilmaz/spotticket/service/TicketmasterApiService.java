package com.olgunyilmaz.spotticket.service;

import com.olgunyilmaz.spotticket.model.EventDetailsResponse;
import com.olgunyilmaz.spotticket.model.EventResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TicketmasterApiService {
    @GET("events.json")
    Call<EventResponse> getEvents(
            @Query("apikey") String apiKey,
            @Query("city") String city
    );
    @GET("events/{eventId}.json")
    Call<EventDetailsResponse> getEventDetails(
            @Path("eventId") String eventId,
            @Query("apikey") String apiKey
    );
}
