package com.olgunyilmaz.spotticket.model;

public class MyEventModel {
    public MyEventModel(String eventId, String imageUrl, String eventName) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
        this.eventName = eventName;
    }

    private String eventId;
    private String eventName;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    private String imageUrl;


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
