/**
 * Date:            25.11.2017
 * Version:         2.0
 * Author:          Natalie Stalder, Nadja Stadelmann
 * AppQuest 2017:
 * Team:            Brudinne
 * App 3:           Schatzkarte
 * Version Test:    Handy:           APK 23
 * Emulator:        APK 26
 * <p>
 * Version Changes
 * ---------------
 * <p>
 * V 2.0
 * -----
 * app improved significantly
 * changes:
 * - colors changed
 * - internet-connection-checker added
 * - dialog to internet-connection-settings added
 * - dialog for gps-settings added
 * - dialog for current-location-settings added
 * - app is now zooming in to the right current position
 * - permission problem fixed
 * bug on some phones (e.g. Samsung Galaxy S5):
 * - Error: "display overlay erkannt"
 * workaround: open settings, restart app - https://www.androidpit.de/display-overlay-erkannt-fehler-beheben-loesung
 * <p>
 * V 1.0
 * -----
 * app is running as wanted
 * bugs:
 * - no information to turn on gps
 * - gps not implemented properly it is zooming in to the wrong location (not current location of mobile)
 * - no information to about the connectivity status
 * - permission request stops app, it needs to be restarted after accepting the permission
 */
package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MainActivity extends LocationHandler {
    // view variables
    private MapView map;
    private IMapController controller;
    private ArrayList<Marker> items = new ArrayList();

    // JSON variables
    private SharedPreferences settings;
    private JSONArray latLongList = new JSONArray();
    public static final String FILE_NAME = "MyLatLonFile";
    public static final String STRING_NAME = "JSON";

    // internet handler
    private InternetHandler internetHandler = new InternetHandler(this);


    // android lifecycle handling
    // --------------------------

    /**
     * creation of the app - initialize views
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        // init map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMaxZoomLevel(30);
        map.setMultiTouchControls(true);

        controller = map.getController();
        controller.setZoom(18);

        getSavedJSONArray();
    }

    /**
     * resume app - check permission and update location manager
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionRequested) {
            if (checkPermission(this, true)) {
                //this will refresh the osmdroid configuration on resuming.
                Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
                internetHandler.checkInternetConnection(this);
                getLocation(this);
            }
        } else {
            // ending app if permission was not granted
            if (checkPermission(this, false)) {
                //this will refresh the osmdroid configuration on resuming.
                Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
                internetHandler.checkInternetConnection(this);
                getLocation(this);
            } else {
                Toast.makeText(this, "App cannot continue when GPS permission is not granted.", Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        }
    }

    /**
     * save our view on pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        super.removeLocationUpdates(this);
        
        dialogHasAlreadyOccured = false;
        internetHandler.setDialogHasAlreadyOccured(false);

        saveJSONArray();
    }


    // location handling
    // -----------------

    /**
     * get location and set view center to the location
     * @param context
     */
    @Override
    protected void getLocation(Context context) {
        super.getLocation(this);
        if (location != null) {
            onLocationChanged(location);
            gotoLocation(new View(this));
        } else {
            controller.setZoom(18);
            Toast.makeText(this, "Wait for GPS.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * update location and set view center to new location if it gets changed
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        if (setCenter) {
            controller.setCenter(geoPoint);
        }
    }

    /**
     * set view center to current location and set zoom back
     * @param view
     */
    public void gotoLocation(View view) {
        if (geoPoint != null) {
            controller.setCenter(geoPoint);
            controller.setZoom(18);
        } else {
            Toast.makeText(this, "Wait for GPS.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * add current location to the map
     * @param view
     */
    public void addLocationToMap(View view) {
        addMarkerToList(location);
        reloadMap();
    }

    /**
     * add a marker to the map at location
     * @param location
     */
    public void addMarkerToList(Location location) {
        if (location != null) {
            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(location));
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setIcon(getResources().getDrawable(R.drawable.location_icon));
            newMarker.setTitle("Posten " + (items.size() + 1));
            newMarker.setDraggable(true);
            newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker, MapView mapView) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Delete Location");
                    alertDialog.setMessage("Do you want to delete this location marker?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            items.remove(marker);
                            map.getOverlays().remove(marker);
                            reloadMap();
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                    return true;
                }
            });
            map.getOverlays().add(newMarker);
            items.add(newMarker);
        } else {
            Toast.makeText(this, "Wait for GPS.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * update map after adding markers
     */
    private void reloadMap() {
        controller.setZoom(map.getZoomLevel() - 1);
        controller.setZoom(map.getZoomLevel() + 1);
    }


    // save 'n load handling
    // ---------------------

    /**
     * save markers in internal storage
     */
    public void saveJSONArray() {
        settings = getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        makeJSONArray();
        editor.putString(STRING_NAME, latLongList.toString());
        editor.commit();
    }

    /**
     * get current locations into a JSONArray
     */
    public void makeJSONArray() {
        latLongList = new JSONArray();
        for (Marker marker : items) {
            GeoPoint position = marker.getPosition();
            Double lat = position.getLatitude() * Math.pow(10, 6);
            Double lon = position.getLongitude() * Math.pow(10, 6);
            JSONObject object = new JSONObject();
            try {
                object.put("lat", lat);
                object.put("lon", lon);
                latLongList.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * read JSONArray for loading saved locations
     */
    public void getSavedJSONArray() {
        //Get Json-String and build Json-Array
        settings = getSharedPreferences(FILE_NAME, 0);
        String latLongText = settings.getString("JSON", null);
        if (latLongText != null) {
            try {
                latLongList = new JSONArray(latLongText);
                addJSONObjectsToMarkerList();
            } catch (JSONException e) {
                Toast.makeText(this, "Problem with saved values.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No saved locations yet.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * add an object from JSONArray into the location list
     */
    public void addJSONObjectsToMarkerList() {
        for (int i = 0; i < latLongList.length(); ++i) {
            try {
                JSONObject object = (JSONObject) latLongList.get(i);
                double lat = object.getDouble("lat") / Math.pow(10, 6);
                double lon = object.getDouble("lon") / Math.pow(10, 6);
                Location location = new Location("");
                location.setLatitude(lat);
                location.setLongitude(lon);
                addMarkerToList(location);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // log message handling
    // --------------------

    /**
     * prepare log
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                log();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * send log message to Logbook
     */
    private void log() {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();
        if (checkInstalled(intent, "Logbook")) {
            try {
                makeJSONArray();
                if (latLongList != null && latLongList.length() > 0) {
                    log.put("task", "Schatzkarte");
                    log.put("points", latLongList);
                } else {
                    Toast.makeText(this, "No matches to log.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (JSONException e) {
                return;
            }
        }
        intent.putExtra("ch.appquest.logmessage", log.toString());
        startActivity(intent);
    }

    /**
     * checks, if an app is installed
     *
     * @param intent
     * @param appName
     * @return
     */
    private boolean checkInstalled(Intent intent, String appName) {
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, appName + " App not Installed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}