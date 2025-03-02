package com.olgunyilmaz.spotticket.service;

import com.olgunyilmaz.spotticket.model.EventResponse;

import java.util.ArrayList;
import java.util.List;

public class RecommendedEventManager {
    private static RecommendedEventManager instance;
    public List<EventResponse.Event> recommendedEvents;

    private RecommendedEventManager() {
        recommendedEvents = new ArrayList<>();
    }

    public static RecommendedEventManager getInstance() {
        if (instance == null) {
            instance = new RecommendedEventManager();
        }
        return instance;
    }
}
