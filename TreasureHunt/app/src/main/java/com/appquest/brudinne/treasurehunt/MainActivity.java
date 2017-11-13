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
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Marker;
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
    private GeoPoint startPoint;
    private String provider;
    private double latitude, longitude;
    Location location;

    private MyItemizedOverlay myItemizedOverlay = null;
    private ItemizedOverlay<OverlayItem> myLocationOverlay;
    private Drawable drawable;
    private ArrayList<OverlayItem> items = new ArrayList<>();
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

        // check if user has given the permission
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                // Todo: handle request better
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
        }

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // check if enabled and if not send user to the GSP settings
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // todo: change to dialog and do in background if possible (stichwort AlarmDialog)
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // init map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMaxZoomLevel(20);
        map.setMultiTouchControls(true);

        controller = map.getController();
        controller.setZoom(18);

        // init location
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria,false);
        locationManager.requestLocationUpdates(provider,400,1,this);
        location = locationManager.getLastKnownLocation(provider);
        if(location != null){
            onLocationChanged(location);
        }else{
            startPoint = new GeoPoint(0.247353,0.683354);
        }
        // todo: das an einem guten ort machen, etweder nur startup oder beim klick auf einen Button
        controller.setCenter(startPoint);


        /* OnTapListener for the Markers, shows a simple Toast. */
        /*
        this.myLocationOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast.makeText(
                                MainActivity.this,
                                "Item '" + item.getTitle() + "' (index=" + index
                                        + ") got single tapped up", Toast.LENGTH_LONG).show();
                        return true; // We 'handled' this event.
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        Toast.makeText(
                                MainActivity.this,
                                "Item '" + item.getTitle() + "' (index=" + index
                                        + ") got long pressed", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }, getApplicationContext());
        this.map.getOverlays().add(this.myLocationOverlay);
        */

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

        myItemizedOverlay = new MyItemizedOverlay(drawable);
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
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        if(startPoint != null){
            startPoint.setCoords(latitude,longitude);
        }else{
            startPoint = new GeoPoint(location);
        }
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
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        /*Marker newMarker = new Marker(map);
        newMarker.setPosition(new GeoPoint(location));
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(getResources().getDrawable(R.drawable.location_icon));
        newMarker.setTitle("Posten " + (map.getOverlays().size() + 1));
        map.getOverlays().add(newMarker);
        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                map.getOverlays().remove(marker);
                return false;
            }
        });*/

        //items.add(new OverlayItem("Posten " + (items.size() + 1) , "Posten", new GeoPoint(location)));

        //TODO get current values from gps
        location = locationManager.getLastKnownLocation(provider);
        myItemizedOverlay.addItem(new GeoPoint(location), "Posten " + (myItemizedOverlay.size() + 1) , "Posten");
    }

    public void deleteLocationFromMap(View view){
        myItemizedOverlay.deleteItems();
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

            txtTitle.setText("Title of my drawable");
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

