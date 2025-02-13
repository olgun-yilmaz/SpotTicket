package com.olgunyilmaz.spotticket.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventResponse {

    @SerializedName("_embedded")
    private Embedded _embedded;

    public Embedded getEmbedded() {
        return _embedded;
    }

    public class Embedded {
        @SerializedName("events")
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }
    }

    public class Event {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("images")
        private List<Image> images;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Image> getImages() {
            return images;
        }
    }

    public class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }
}