package ch.appquest.brudinne.memory;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private JSONArray jsonArray;
    private String currentPhotoPath;

    /**
     * on create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rv = (RecyclerView)findViewById(R.id.recyclerView);
        //adapter = new CustomAdapter(getApplicationContext(),ApplicationState.getGridElements());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this /* the activity */, 2);
        rv.setLayoutManager(gridLayoutManager);
        Button button1 = (Button)findViewById(R.id.newCard);
    }

    /**
     * onklick button "take picture"
     */
    public void takeQrCodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            String path = extras.getString(
                    Intents.Scan.RESULT_BARCODE_IMAGE_PATH);

            // Ein Bitmap zur Darstellung erhalten wir so:
            // Bitmap bmp = BitmapFactory.decodeFile(path)

            String code = extras.getString(
                    Intents.Scan.RESULT);
        }
    }

    private void setMemoryPicture(Bitmap bmp, String word){

    }


    private void savePicture(Bitmap picture, String word, int index) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String tempFilePath = Environment.getExternalStorageDirectory() + "/" + index + "_" + word + "_" + timeStamp +".jpg";

        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
        }

        tempFile.delete();
        tempFile.createNewFile();

        int quality = 100;
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
        picture.compress(Bitmap.CompressFormat.JPEG, quality, bos);

        bos.flush();
        bos.close();

        picture.recycle();

        //return tempFilePath;
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
                if(jsonArray != null){
                    String resultArray = jsonArray.toString();
                    log(resultArray);
                    return true;
                }
                Toast.makeText(MainActivity.this, "No results to log.", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * send log message to Logbook
     * @param resultArray
     */
    private void log(String resultArray) {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        if(checkInstalled(intent, "Logbook")) {
            try {
                log.put("task", "Memory");
                log.put("solution", resultArray);
            } catch (JSONException e) {
            }
            intent.putExtra("ch.appquest.logmessage", log.toString());
            startActivity(intent);
        }

    }

    /**
     * checks, if an app is installed
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