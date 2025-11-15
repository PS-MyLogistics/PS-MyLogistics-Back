package com.mylogisticcba.core.service.rest;

public class LatLng {
    private final double lat;
    private final double lon;

    public LatLng(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return "LatLng{" + "lat=" + lat + ", lon=" + lon + '}';
    }
}

