package com.example.gosu.geofencing;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceTransitionIntentService extends IntentService {

    public GeoFenceTransitionIntentService() {
        super("GeoFence Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handle errors
        if (geofencingEvent.hasError()) {
            String errorMessage = GetErrorMessages.getErrorString(this, geofencingEvent.getErrorCode());
            Log.e("Error with Geofencing: ", errorMessage);
            return;
        }

        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Create notification
            createNotification(geofenceTransitionDetails);

            // Log the transition details
            Log.i("Details: ", geofenceTransitionDetails);
        } else {
            // Log the error
            Log.e("Error: ", getString(R.string.geofence_transition_invalid_type +
                    geofenceTransition));
        }
    }

    // Get details in a String
    private String getGeofenceTransitionDetails(Context context, int transition, List<Geofence> list) {
        ArrayList<String> geofencesId = new ArrayList<String>();

        for (Geofence geofence : list) {
            geofencesId.add(geofence.getRequestId());
        }

        return TextUtils.join(", ", geofencesId);
    }

    // Show the notification
    private void createNotification(String message) {
        // Create Intent to open MainActivity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Create a task stack and add push
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        // Create PendingIntent for notification
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message)
                .setContentText("Click to return to the app")
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification after clicking it
        mBuilder.setAutoCancel(true);
    }
}
