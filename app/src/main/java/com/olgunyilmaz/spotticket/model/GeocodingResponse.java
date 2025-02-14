package com.olgunyilmaz.spotticket.model;

import java.util.List;

public class GeocodingResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }
    }

    public static class Geometry {
        private Location location;

        public Location getLocation() {
            return location;
        }
    }

    public static class Location {
        private double lat;
        private double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}

