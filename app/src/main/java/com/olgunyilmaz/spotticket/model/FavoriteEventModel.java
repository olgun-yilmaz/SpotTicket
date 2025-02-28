package com.olgunyilmaz.spotticket.model;

public class FavoriteEventModel {
    public FavoriteEventModel(String eventId, String imageUrl, String eventName) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
        this.eventName = eventName;
    }

    private String eventId;
    private String eventName;

    public String getEventName() {
        return eventName;
    }

    private String imageUrl;

    public String getEventId() {
        return eventId;
    }


    public String getImageUrl() {
        return imageUrl;
    }


}
