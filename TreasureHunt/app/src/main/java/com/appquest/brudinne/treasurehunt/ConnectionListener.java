package com.appquest.brudinne.treasurehunt;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Nadja on 23.11.2017.
 */

public abstract class ConnectionListener extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    protected Location location;
    protected GeoPoint startPoint;
    protected String provider;
    protected double latitude, longitude;
}
