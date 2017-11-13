package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
/* TODO: check if needed
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
*/

//TODO: if nothing todo : https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library#layout

public class MainActivity extends AppCompatActivity implements LocationListener{
    private MapView map;
    private IMapController controller;

    private LocationManager locationManager;
    private Location location;
    private GeoPoint startPoint;
    private String provider;
    private double latitude, longitude;

    //private MyItemizedOverlay myItemizedOverlay = null;
    private Drawable drawable;
    private ArrayList<Marker> items = new ArrayList<>();
    Marker newMarker;
    //TODO: check error
    //ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

    // variables to save list in JSON
    private SharedPreferences   settings;
    private JSONArray           latLongList = new JSONArray();
    public static final String  FILE_NAME   = "MyLatLonFile";
    public static final String  STRING_NAME = "JSON";

    public static final int PERMISSIONS_REQUEST = 0;
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Todo: handle request better
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        }
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        // init map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMaxZoomLevel(30);
        map.setMultiTouchControls(true);

        controller = map.getController();
        controller.setZoom(18);

        //Get Json-String and build Json-Array
        settings           = getSharedPreferences(FILE_NAME, 0);
        String latLongText = settings.getString("JSON", null);
        if (latLongText != null) {
            try {
                latLongList = new JSONArray(latLongText);
            } catch (JSONException e) {
                Toast.makeText(this, "Problem with saved values.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No saved locations yet.", Toast.LENGTH_LONG).show();
        }

        //New drawable
        drawable = getResources().getDrawable(R.drawable.location_icon);
        drawable.setBounds(0, drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth(), 0);

        /*
        myItemizedOverlay = new MyItemizedOverlay(drawable);
        map.getOverlays().add(myItemizedOverlay);
        */
    }

    @Override
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Todo: handle request better
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        }

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
        //if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))return;

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria,false);

        locationManager.requestLocationUpdates(provider,400,1,this);
        location = locationManager.getLastKnownLocation(provider);
        if(location != null){
            onLocationChanged(location);
        }else{
            //todo: improve
            location = new Location(provider);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            onLocationChanged(location);
            //startPoint = new GeoPoint(latitude,longitude);
        }
        // todo: das an einem guten ort machen, entweder nur startup oder beim klick auf einen Button
        controller.setCenter(startPoint);
    }

    // todo: check if saveinstances is needed
    private String SAVE_LONGITUDE = "saveLongitude";
    private String SAVE_LATITUDE = "saveLatitude";
    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putDouble(SAVE_LATITUDE, latitude);
        outState.putDouble(SAVE_LONGITUDE, longitude);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        latitude = savedInstanceState.getDouble(SAVE_LATITUDE);
        longitude = savedInstanceState.getDouble(SAVE_LONGITUDE);
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean setCenter = false;
        if(latitude == 0 && longitude == 0){
            setCenter = true;
        }
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        if(startPoint != null){
            startPoint.setCoords(latitude,longitude);
        }else{
            startPoint = new GeoPoint(location);
        }
        if(setCenter) {
            // todo: in button!
            controller.setCenter(startPoint);
        }
        Toast.makeText(this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
    }

    /**
     * save our view on pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

        // Saving JSON-Array before app gets hidden
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        settings                        = getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(STRING_NAME, latLongList.toString());
        // Committing the edits
        editor.commit();
    }

    public void addLocationToMap(View view){
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            // Todo: handle request better
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        newMarker = new Marker(map);
        newMarker.setPosition(new GeoPoint(location));
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(getResources().getDrawable(R.drawable.location_icon_blue));
        newMarker.setTitle("Posten " + (items.size() + 1));
        newMarker.setDraggable(true);
        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker, MapView mapView) {
                //TODO: check if better / zuverlässigerer way exists
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Delete Locatione");
                alertDialog.setMessage("Do you want to delete this location marker?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        items.remove(marker);
                        map.getOverlays().remove(marker);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                AlertDialog alert=alertDialog.create();
                alert.show();
                return true;
            }
        });
        map.getOverlays().add(newMarker);
        items.add(newMarker);

        //TODO get current values from gps
        //location = locationManager.getLastKnownLocation(provider);
        //myItemizedOverlay.addItem(new GeoPoint(location), "Posten " + (myItemizedOverlay.size() + 1) , "Posten");
    }

    public void gotoLocation(View view){
        controller.setCenter(startPoint);
    }

    /*public void deleteLocationFromMap(View view){
        myItemizedOverlay.deleteItems();
    }*/

    public void addToJsonArray(int latitude, int longitude){
        JSONObject object = new JSONObject();
        try {
            object.put("lat", Integer.toString(latitude));
            object.put("lon", Integer.toString(longitude));
            latLongList.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
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
    //TODO save lat and len in factor 1'000'000
    private void log() {
        Intent intent   = new Intent("ch.appquest.intent.LOG");
        JSONObject log  = new JSONObject();
        if (checkInstalled(intent, "Logbook")) {
            try {
                // set up log JSONObject from saved array
                JSONArray resultArray = new JSONArray();
                int size = latLongList.length();
                for (int i = 0; i < size; ++i) {
                    /*
                    todo: improve this!!
                    log.put("lat", hierMarkerarray.get(i).getPosition().getLatitudeE6());
                    log.put("lon", hierMarkerarray.get(i).getPosition().getLongitudeE6());
                     */
                    //oooder
                    log.put("lat", ((JSONObject)latLongList.get(i)).getDouble("lat")*Math.pow(10,6));
                    log.put("lon", ((JSONObject)latLongList.get(i)).getDouble("lon")*Math.pow(10,6));
                }
                if (resultArray != null && resultArray.length() > 0) {
                    log.put("task", "Schatzkarte");
                    log.put("points", resultArray);
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

