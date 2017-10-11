/**
 * Date:        11.10.2017
 * Version:     1.0
 * Author:      Natalie Stalder, Nadja Stadelmann
 * AppQuest:    App 2 - Dechiffrierer
 */
package ch.appquest.brudinne.descrambler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v4.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * unscramble a red white picture to be able to see a word which is written behind the color
 */
public class MainActivity extends Activity {

    // variables for taking a picture
    private Button takePhoto;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private String photoPath;
    private ImageView imageView;

    // variables for sending log message
    private EditText resultText;
    private Button sendResult;

    // variables for saving state
    private static String PHOTO_PATH = "1";
    private static String RESULT_TEXT = "2";


    // android lifecycle handling
    // --------------------------

    /**
     * creation of the app - initializes view
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        resultText = (EditText) findViewById(R.id.resultText);
        resultText.setVisibility(View.GONE);
        sendResult = (Button) findViewById(R.id.sendResult);
        sendResult.setVisibility(View.GONE);
        takePhoto  = (Button) findViewById(R.id.takePhoto);
    }

    /**
     * saves state for app restart on pause
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTO_PATH, photoPath);
        outState.putString(RESULT_TEXT, resultText.getText().toString());

        super.onSaveInstanceState(outState);
    }

    /**
     * set state for app restart to state before on pause
     * @param savedInstanceState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        photoPath = savedInstanceState.getString(PHOTO_PATH);
        resultText.setText(savedInstanceState.getString(RESULT_TEXT));

        if(photoPath != null){
            imageView.setImageBitmap(readImageFile(Uri.parse(photoPath)));
        }
    }


    // photo handling
    // --------------

    /**
     * method for Button takePhoto - will be executed on click
     * @param view
     */
    public void takePhoto(View view){
        dispatchTakePictureIntent();
    }

    /**
     * get a picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "File Creation failed", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ch.appquest.brudinne.descrambler.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * getting picture from intent and set it to imageView with a red filter
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                imageView.setImageBitmap(readImageFile(Uri.parse(photoPath)));
            }
        }
    }

    /**
     * create image and get its path
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        photoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * put file into a bitmap and apply the red filter
     * @param imageUri
     * @return
     */
    private Bitmap readImageFile(Uri imageUri) {
        File file = new File(imageUri.getPath());
        InputStream iS = null;
        try {
            iS = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(iS);
            return applyFilter(bitmap);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Could not find image.", Toast.LENGTH_LONG).show();
            return null;
        } finally {
            if(iS != null) {
                try {
                    iS.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * set a red filter to bitmap to make its text visible
     * @param bitmap
     * @return bitmap with red filter
     */
    private Bitmap applyFilter(Bitmap bitmap) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        ColorFilter redFilter = new LightingColorFilter(Color.RED, 0);
        paint.setColorFilter(redFilter);
        canvas.drawBitmap(mutableBitmap, new Matrix(), paint);

        return mutableBitmap;
    }


    // log book handling
    // ------------------

    /**
     * prepare log menu button
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
                if(imageView != null && imageView.getDrawable() != null){
                    resultText.setVisibility(View.VISIBLE);
                    sendResult.setVisibility(View.VISIBLE);
                    return true;
                }else{
                    Toast.makeText(MainActivity.this, "Take a picture first to get the answer.", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * method for Button sendResult - will be executed on click
     * @param view
     */
    public void sendResultMethod(View view){
        log(resultText.getText().toString());
    }

    /**
     * send log message to Logbook
     * @param result
     */
    private void log(String result) {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        if(checkInstalled(intent, "Logbook")) {
            try {
                //to test log
                //log.put("task", "TEST");
                //log.put("solution", "482ae9 " + result);
                //Bitte einkommentieren
                log.put("task", "Dechiffrierer");
                log.put("solution", result);
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