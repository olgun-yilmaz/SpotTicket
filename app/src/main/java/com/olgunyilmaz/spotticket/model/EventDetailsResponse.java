package com.olgunyilmaz.spotticket.model;

import com.google.gson.annotations.SerializedName;

public class EventDetailsResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description; // Etkinlik açıklaması

    @SerializedName("dates")
    private Dates dates;

    @SerializedName("url")
    private String url; // Etkinlik ile ilgili web bağlantısı

    @SerializedName("info")
    private String info; // Ekstra bilgiler

    // Getter'lar
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Dates getDates() {
        return dates;
    }

    public String getUrl() {
        return url;
    }

    public String getInfo() {
        return info;
    }

    public class Dates {
        @SerializedName("start")
        private Start start;

        public Start getStart() {
            return start;
        }

        public class Start {
            @SerializedName("dateTime")
            private String dateTime; // Başlangıç zamanı

            public String getDateTime() {
                return dateTime;
            }
        }
    }
}
