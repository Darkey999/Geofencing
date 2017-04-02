package com.example.gosu.geofencing;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, ResultCallback<Status> {

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Geofence> geofenceArrayList = new ArrayList<Geofence>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build client
        buildGoogleClient();

        // Get the geofences
        populateGeofenceList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    // Build GoogleApiClient
    private void buildGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Connection failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Problem with connection", String.valueOf(connectionResult));
    }

    // Client connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    // Connection suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.i("MainActivity", " ConnectionSuspended");
        mGoogleApiClient.connect();
    }

    // Get the geofences
    private void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.MY_MAP.entrySet()) {
            geofenceArrayList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, Constants.GEOFENCE_RADIUS)
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    // Create geofence request
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        /*
         A flag indicating that geofencing service should trigger GEOFENCE_TRANSITION_ENTER
          notification at the moment when the geofence is added and if the device is already
           inside that geofence.
          */
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add geofences to be monitored
        builder.addGeofences(geofenceArrayList);

        return builder.build();
    }

    // Create PendingIntent for geofences
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeoFenceTransitionIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add geofences button
    public void addGeofencesButtonHandler(View view) {

        // Check whether client is connected
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Add geofences
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object
                    getGeofencingRequest(),
                    // A pending intent that is reused when calling removeGeofences()
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult()
        } catch (SecurityException securityException) {
            Log.e("Invalid permission", String.valueOf(securityException));
        }
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            Toast.makeText(MainActivity.this, R.string.geofences_add, Toast.LENGTH_SHORT).show();
        } else {
            String error = GetErrorMessages.getErrorString(this, status.getStatusCode());
            Log.e("Problem occurred: ", error);
        }
    }
}