/**
 * Date:            10.10.2017
 * Version:         1.1
 * Author:          Natalie Stalder, Nadja Stadelmann
 * AppQuest:        Team:       Brudinne
 *                  App 1:      Metalldetektor
 * Version Test:    Handy:      APK 23
 *                  Emulator:   APK 26
 */
package ch.appquest.brudinne.metalldetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * uses magnetic sensor to find metal
 * QR-Code reader to get result string
 */
public class MainActivity extends Activity implements SensorEventListener {

    // variables for view
    private SensorManager sensorManager;
    private Sensor metalDetector;
    private ProgressBar metallic;
    private TextView status;

    // variables for intents
    private static final int SCAN_QR_CODE_REQUEST_CODE = 0;


    // app lifecycle handling
    // ----------------------

    /**
     * creation of the view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        metallic = (ProgressBar) findViewById(R.id.metallic);
        status = (TextView) findViewById(R.id.status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            metallic.setMin(10);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        metalDetector = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
    }

    /**
     * unregister sensor listener - not used when activity is on pause
     */
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * register sensor listener again on resume
     */
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, metalDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }


    // magnetic sensor handling
    // ------------------------

    /**
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] met = sensorEvent.values;
        double betrag = Math.sqrt(met[0] * met[0] + met[1] * met[1] + met[2] * met[2]);
        metallic.setProgress((int)betrag);
        if(betrag < 30){
            status.setText("You are far away of your treasure.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                metallic.setProgressTintList(ColorStateList.valueOf(Color.RED));
            }
        }else if(betrag < 60){
            status.setText("You are getting nearer to your treasure.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                metallic.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
            }
        }
        else if(betrag < 85){
            status.setText("You have almost found your treasure!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                metallic.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
            }
        }else{
            status.setText("Treasure found! You made it!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                metallic.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }
    }

    /**
     * not important for our topic
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // unused
    }


    // log message handling
    // --------------------

    /**
     * prepare log button with QR-Code Intent
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
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                if(checkInstalled(intent, "Zxing Barcode Scanner")){
                    startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * get result from QR-Code Intent
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                log(intent.getStringExtra("SCAN_RESULT"));
            }
        }
    }

    /**
     * send log message to Logbook
     * @param qrCode
     */
    private void log(String qrCode) {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        if(checkInstalled(intent, "Logbook")) {
            try {
                log.put("task", "Metalldetektor");
                log.put("solution", qrCode);
            } catch (JSONException e) {
            }
            intent.putExtra("ch.appquest.logmessage", log.toString());
            startActivity(intent);
        }
    }

    /**
     * checks if an application is installed
     * @param intent
     * @param appName
     * @return
     */
    private boolean checkInstalled(Intent intent, String appName){
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, appName + " App not Installed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}