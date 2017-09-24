package ch.appquest.brudinne.metalldetector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor metallDetector;
    private ProgressBar metallic;

    private Menu mainMenu;
    private String menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        metallic = (ProgressBar) findViewById(R.id.metallic);
        mainMenu = (Menu) findViewById(R.id.menu);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        metallDetector = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, metallDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] met = sensorEvent.values;
        double betrag = Math.sqrt(met[0] * met[0] + met[1] * met[1] + met[2] * met[2]);
        metallic.setProgress((int)betrag);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private static final int SCAN_QR_CODE_REQUEST_CODE = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                log(intent.getStringExtra("SCAN_RESULT"));
                // Weiterverarbeitung..
            }
        }
    }

    private void log(String qrCode) {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        // Achtung, je nach App wird etwas anderes eingetragen
        String logmessage = ...
        intent.putExtra("ch.appquest.logmessage", logmessage);

        startActivity(intent);
    }

    public void log(String)
    JSONObject log = JSONObject();
    log.put
    log.put
    intent.putExtra(log.toString())
}