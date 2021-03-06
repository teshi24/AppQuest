package com.appquest.brudinne.treasurehunt;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.util.GeoPoint;

public abstract class LocationHandler extends AppCompatActivity implements LocationListener {

    // request flags
    protected static final int PERMISSIONS_REQUEST = 0;
    protected boolean permissionRequested;

    // dialog flag
    protected boolean dialogHasAlreadyOccured = false;

    // location variables
    protected LocationManager locationManager;
    protected Location location;
    protected GeoPoint geoPoint;
    protected String provider;
    protected double latitude, longitude;
    protected boolean setCenter;


    // permission handling
    // -------------------

    /**
     * get GPS permission
     *
     * @param context
     * @param doRequest true if it should be asked for request if it doesn't exist, false if only the permission should be checked
     * @return true if permission granted <br>
     * false if permission denied
     */
    public boolean checkPermission(Context context, boolean doRequest) {
        boolean returnValue = true;
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            returnValue = false;
            if (doRequest) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            }
        }
        return returnValue;
    }

    /**
     * evaluate result of permission request
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permissionRequested = true;
    }

    // setting handling
    // ----------------

    /**
     * GPS dialog <br>
     * cancel or go to settings
     */
    private void gpsSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable Location");
        alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
        alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }


    // location handling
    // -----------------

    /**
     * remove updates from locationManager
     *
     * @param context
     */
    protected void removeLocationUpdates(Context context) {
        if (checkPermission(context, true)) {
            if (locationManager != null)
                locationManager.removeUpdates((LocationListener) context);
        }
    }

    /**
     * get current Location
     *
     * @param context
     */
    protected void getLocation(Context context) {
        if (checkPermission(context, true)) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);

            locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) context);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) context);

            location = locationManager.getLastKnownLocation(provider);
        }
    }

    /**
     * get new location information as soon as the location has changed
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        setCenter = false;
        this.location = location;
        if (latitude == 0 && longitude == 0) {
            setCenter = true;
        }
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        if (geoPoint != null) {
            geoPoint.setCoords(latitude, longitude);
        } else {
            geoPoint = new GeoPoint(location);
        }
    }

    /**
     * get dialog when GPS provider is / gets disabled
     *
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        if (!dialogHasAlreadyOccured) {
            gpsSettingsDialog();
            dialogHasAlreadyOccured = true;
        } else {
            dialogHasAlreadyOccured = false;
        }
    }

    // unused methods from LocationListener
    // ------------------------------------

    /**
     * do something when GPS provider status has changed <br>
     * not supported yet
     *
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // unused
    }

    /**
     * do something when GPS provider has been enabled <br>
     * not supported yet - is not properly called by the system
     *
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        // unused
    }
}