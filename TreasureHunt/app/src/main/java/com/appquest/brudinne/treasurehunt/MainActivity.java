package com.appquest.brudinne.treasurehunt;

import android.content.Context;
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
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.HashMap;
/* TODO: check if needed
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
*/

public class MainActivity extends AppCompatActivity implements LocationListener{
    private MapView map;
    private IMapController controller;

    private LocationManager locationManager;
    private GeoPoint startPoint;
    private String provider;
    private int latitute, longitude;

    private MyItemizedOverlay myItemizedOverlay = null;
    private Drawable marker;
    //TODO: check error
    //ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

    // variables to save list in JSON
    private SharedPreferences   settings;
    private JSONArray           latLongList = new JSONArray();
    public static final String  FILE_NAME   = "MyJsonFile";
    public static final String  STRING_NAME = "JSON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        if ( Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission( this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( this.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    return;
        }

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            // todo: change to dialog and do in background if possible (stichwort AlarmDialog)
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMaxZoomLevel(20);

        // default zoom buttons and ability to zoom with 2 fingers
        map.setMultiTouchControls(true);
        //map.setBuiltInZoomControls(true);

        map.setOnClickListener(new MapView. {
            @Override
            public void onClick(View map) {

            }
        });

        controller = map.getController();
        controller.setZoom(18);

        //todo: something like this:
        // move map on default view point
        // todo: this is paris, should be current position via GPS
        startPoint = new GeoPoint(48.8583, 2.2944);
        //startPoint = new GeoPoint(location);
        controller.setCenter(startPoint);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria,false);

        Location location = null;
        //location = locationManager.getLastKnownLocation(provider);
        if(location != null){
            onLocationChanged(location);
        }else{
            //todo: error because of latitudes..
        }



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



        //New marker
        marker  = getResources().getDrawable(R.drawable.location_icon);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        myItemizedOverlay = new MyItemizedOverlay(marker);
        map.getOverlays().add(myItemizedOverlay);

        GeoPoint myPoint1 = new GeoPoint(48.8583, 2.2944);
        myItemizedOverlay.addItem(myPoint1, "myPoint1", "myPoint1");
        GeoPoint myPoint2 = new GeoPoint(48.85, 2.1989);
        myItemizedOverlay.addItem(myPoint2, "myPoint2", "myPoint2");
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
            return  ;
        }

        locationManager.requestLocationUpdates(provider,400,1,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        startPoint.setLatitude(location.getLatitude());
        startPoint.setLongitude(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //todo: iwas auto-generated method stub http://www.vogella.com/tutorials/AndroidLocationAPI/article.html
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

    public void addLocationToMap(){
        //TODO get current values from gps
        //myItemizedOverlay.addItem(new GeoPoint(), "Posten " + (myItemizedOverlay.size() + 1) , "Posten");
    }

    public void deleteLocationFromMap(OverlayItem overlayItem){
        myItemizedOverlay.deleteItem(overlayItem);
    }

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
                if(latLongList != null && latLongList.length() > 0) {
                    log.put("task", "Schatzkarte");
                    log.put("points", latLongList);
                } else {
                    Toast.makeText(this, "No matches to log.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (JSONException e) {
            }
            intent.putExtra("ch.appquest.logmessage", log.toString());
            startActivity(intent);
        }
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

    private class MyInfoWindow extends InfoWindow{
        public MyInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }
        public void onClose() {
        }

        public void onOpen(Object arg0) {
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
            Button btnMoreInfo = (Button) mView.findViewById(R.id.bubble_moreinfo);
            TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
            TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);
            TextView txtSubdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);

            txtTitle.setText("Title of my marker");
            txtDescription.setText("Click here to view details!");
            txtSubdescription.setText("You can also edit the subdescription");
            layout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Override Marker's onClick behaviour here
                }
            });
        }
    }
}

