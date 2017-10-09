package ch.appquest.brudinne.metalldetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor metallDetector;
    private ProgressBar metallic;
    private TextView status;
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        metallic = (ProgressBar) findViewById(R.id.metallic);
        status = (TextView) findViewById(R.id.status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            metallic.setMin(10);
        }
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
        if(betrag < 30){
            status.setText("You are far away of your treasure.");
        }else if(betrag < 60){
            status.setText("You are getting nearer to your treasure.");
        }
        else if(betrag < 85){
            status.setText("You have almost found your treasure!");
        }else{
            status.setText("Treasure found! You made it!");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not important
    }


    private static final int SCAN_QR_CODE_REQUEST_CODE = 0;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                log(intent.getStringExtra("SCAN_RESULT"));
            }
        }
    }

    private void log(String qrCode) {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        if(checkInstalled(intent, "Logbook")) {
            try {
                //to test log
                //log.put("task", "TEST");
                //log.put("solution", "482ae9");
                log.put("task", "Metalldetektor");
                log.put("solution", qrCode);
            } catch (JSONException e) {
            }
            intent.putExtra("ch.appquest.logmessage", log.toString());
            startActivity(intent);
        }
    }

    private boolean checkInstalled(Intent intent, String appName){
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, appName + " App not Installed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}