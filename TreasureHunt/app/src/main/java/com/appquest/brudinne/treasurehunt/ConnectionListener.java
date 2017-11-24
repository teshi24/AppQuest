package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Nadja on 23.11.2017.
 */
public abstract class ConnectionListener extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    protected Location location;
    protected GeoPoint geoPoint;
    protected String provider;
    protected double latitude, longitude;

    public void removeLocationUpdates(Context context){
        // todo: remove this
        if(locationManager != null)
        locationManager.removeUpdates((LocationListener) context);
    }

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

    public abstract boolean checkPermission();

    // unused needed methods
    // ---------------------

    protected boolean setCenter;

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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // unused method
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    boolean dialogHasAlreadyOccured = false;
    boolean dialogWlanHasAlreadyOccured = false;

    @Override
    public void onProviderDisabled(String provider) {
        if(!dialogHasAlreadyOccured) {
            gpsSettingsDialog();
            dialogHasAlreadyOccured = true;
        }else{
            dialogHasAlreadyOccured = false;
        }
    }

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

    //wlan listener
    public void checkInternetConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null){
            if(!dialogWlanHasAlreadyOccured) {
                internetSettingsDialog();
                dialogWlanHasAlreadyOccured = true;
            }else{
                dialogWlanHasAlreadyOccured = false;
            }
        }else if(!activeNetwork.isConnectedOrConnecting()){
            internetConnectionInfo(context);
        }
    }

    public void internetSettingsDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable Internet");
        alertDialog.setMessage("You have no internet connection. Please enabled it in settings menu.");
        alertDialog.setPositiveButton("Internet Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        // todo: add 3rd button for internet
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public void internetConnectionInfo(Context context){
        // todo: add information dialog no connection
        Toast.makeText(context, "internet not working", Toast.LENGTH_LONG).show();
    }
}
