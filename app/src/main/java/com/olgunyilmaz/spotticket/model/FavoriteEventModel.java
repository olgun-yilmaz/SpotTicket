package com.olgunyilmaz.spotticket.model;

public class FavoriteEventModel {
    public FavoriteEventModel(String eventId, String eventName, String imageUrl) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
        this.eventName = eventName;
    }

    private final String eventId;
    private final String eventName;

    public String getEventName() {
        return eventName;
    }

    private final String imageUrl;

    public String getEventId() {
        return eventId;
    }


    public String getImageUrl() {
        return imageUrl;
    }


}