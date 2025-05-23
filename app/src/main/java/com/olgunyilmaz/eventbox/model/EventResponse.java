/*
 * Project: EventBox
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

package com.olgunyilmaz.eventbox.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventResponse { // model for event details from api

    @SerializedName("_embedded")
    private Embedded _embedded;

    public Embedded getEmbedded() {
        return _embedded;
    }

    public static class Embedded {
        @SerializedName("events")
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }
    }

    public static class Event {

        public String getHighQualityImage(){
            if (getImages() != null && !getImages().isEmpty()) {
                Image selectedImage = null;
                for(Image image : getImages()){
                    if(image.width > 255 && image.height > 255){
                        selectedImage = image;
                        break;
                    }
                }
                if (selectedImage == null){
                    selectedImage = getImages().get(0);
                }

                return selectedImage.url;
            }
            return null;
        }

        @SerializedName("_embedded")
        private Embedded embedded;

        public Embedded getEmbedded() {
            return embedded;
        }

        public static class Embedded {
            @SerializedName("venues")
            private List<Venue> venues;

            public List<Venue> getVenues() {
                return venues;
            }
        }

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }

        @SerializedName("venues")
        private List<Venue> venues;

        @SerializedName("classifications")
        private List<Classification> classifications;

        @SerializedName("images")
        private List<Image> images;

        @SerializedName("dates")
        private Dates dates;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Image> getImages() {
            return images;
        }
        public Dates getDates() {
            return dates;
        }

        public List<Classification> getClassifications() {
            return classifications;
        }
    }

    public static class Image {
        private int width;
        private int height;

        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }

    public static class Dates {
        @SerializedName("start")
        private Start start;

        public Start getStart() {
            return start;
        }

        public static class Start {
            @SerializedName("dateTime")
            private String dateTime; // Başlangıç zamanı

            public String getDateTime() {
                return dateTime;
            }
        }
    }

    public static class Classification {
        @SerializedName("segment")
        private Segment segment;

        public Segment getSegment() {
            return segment;
        }

        public static class Segment {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
    }

    public static class Venue {
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

        public static class City {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
    }
}