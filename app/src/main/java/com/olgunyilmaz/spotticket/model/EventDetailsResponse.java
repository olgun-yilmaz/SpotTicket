package com.olgunyilmaz.spotticket.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventDetailsResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("dates")
    private Dates dates;

    @SerializedName("url")
    private String url;

    @SerializedName("venues")
    private List<Venue> venues;

    @SerializedName("classifications")
    private List<Classification> classifications;

    // Getter'lar
    public String getName() {
        return name;
    }

    public Dates getDates() {
        return dates;
    }

    public String getUrl() {
        return url;
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    @SerializedName("_embedded")
    private Embedded embedded;

    public Embedded getEmbedded() {
        return embedded;
    }

    public class Embedded {
        @SerializedName("venues")
        private List<Venue> venues;

        public List<Venue> getVenues() {
            return venues;
        }
    }

    public class Venue {
        @SerializedName("name")
        private String name;

        @SerializedName("city")
        private City city;

        public String getName() {
            return name;
        }

        public City getCity() {
            return city;
        }

        public class City {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
    }

    // Classification sınıfı (Etkinlik Türü)
    public class Classification {
        @SerializedName("segment")
        private Segment segment;

        public Segment getSegment() {
            return segment;
        }

        public class Segment {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
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
