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

package com.olgunyilmaz.spotticket.service;

import com.olgunyilmaz.spotticket.model.EventResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TicketmasterApiService {
    @GET("events.json")
    Call<EventResponse> getEvents(
            @Query("apikey") String apiKey,
            @Query("city") String city, // search by city name
            @Query("classificationId") String categoryId, // search by category
            @Query("keyword") String keyword, // search by keyword
            @Query("startDateTime") String startDateTime // filter by start date -> utc format : 2025-03-18T00:00:00Z
    );
    @GET("events/{eventId}.json")
    Call<EventResponse.Event> getEventDetails(
            @Path("eventId") String eventId, // get event details by unique event ID
            @Query("apikey") String apiKey
    );
}
