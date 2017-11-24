package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Nadja on 23.11.2017.
 */
public abstract class LocationHandler extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    protected Location location;
    protected GeoPoint geoPoint;
    protected String provider;
    protected double latitude, longitude;

    /**
     * remove updates from locationManager
     * @param context
     */
    public void removeLocationUpdates(Context context){
        // todo: remove this
        if(locationManager != null)
        locationManager.removeUpdates((LocationListener) context);
    }

    /**
     * get current Location
     * @param context
     */
    public void getLocation(Context context) {
        if(checkPermission()) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);

            locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) context);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) context);

            location = locationManager.getLastKnownLocation(provider);
        }
    }

    /**
     * implements check off all needed permissions <br>
     * at least GPS permission must get granted
     * @return true if permission granted <br>
     *     false if permission denied
     */
    public abstract boolean checkPermission();

    // unused needed methods
    // ---------------------

    protected boolean setCenter;

    /**
     * get new location information as soon as the location has changed
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
        // todo: delete this before upload to manuel
        Toast.makeText(this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
    }

    /**
     * do something when GPS provider status has changed <br>
     * not supported yet
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
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        // unused
    }

    boolean dialogHasAlreadyOccured = false;
    boolean dialogWlanHasAlreadyOccured = false;

    /**
     * get dialog when GPS provider is / gets disabled
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        if(!dialogHasAlreadyOccured) {
            gpsSettingsDialog();
            dialogHasAlreadyOccured = true;
        }else{
            dialogHasAlreadyOccured = false;
        }
    }

    /**
     * GPS dialog <br>
     * cancel or go to settings
     */
    public void gpsSettingsDialog(){
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
}
