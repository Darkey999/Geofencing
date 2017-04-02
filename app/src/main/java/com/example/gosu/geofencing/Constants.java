package com.example.gosu.geofencing;


import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public final class Constants {

    // Private constructor
    private Constants() {
    }

    public static final HashMap<String, LatLng> MY_MAP = new HashMap<String, LatLng>();

    // Radius in meters
    public static float GEOFENCE_RADIUS = 2000;

    // Expiration in miliseconds (twelve hours)
    public static long GEOFENCE_EXPIRATION = 43200000;

    // Add some locations
    static {
        MY_MAP.put("my", new LatLng(50.063250, 19.32337));
    }
}

