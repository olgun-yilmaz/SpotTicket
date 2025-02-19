package com.olgunyilmaz.spotticket.service;

import static com.olgunyilmaz.spotticket.view.activities.MainActivity.TICKETMASTER_BASE_URL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(TICKETMASTER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static TicketmasterApiService getApiService() {
        return getClient().create(TicketmasterApiService.class);
    }
}
