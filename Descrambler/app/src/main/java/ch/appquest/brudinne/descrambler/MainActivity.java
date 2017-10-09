package ch.appquest.brudinne.descrambler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import java.net.URI;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    //Class variables
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    ImageView imageView;
    EditText resultText;
    Button sendResult;
    Button takePhoto;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        resultText = (EditText) findViewById(R.id.resultText);
        resultText.setVisibility(View.GONE);
        sendResult = (Button) findViewById(R.id.sendResult);
        sendResult.setVisibility(View.GONE);
        takePhoto  = (Button) findViewById(R.id.takePhoto);
    }

    public void photograph(View view){
        dispatchTakePictureIntent();
    }

    //Get picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                imageView.setImageBitmap(applyFilter(readImageFile(Uri.parse(mCurrentPhotoPath))));
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    //Read picture
    private Bitmap readImageFile(Uri imageUri) {
        File file = new File(imageUri.getPath());
        InputStream iS = null;
        try {
            iS = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(iS);
            return bitmap;
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

    private Bitmap applyFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
  /*
        int[] data = new int[width * height];

        bitmap.getPixels(data, 0, width, 0, 0, width, height);
        return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
*/
        int redFilter = 0xFF0000;

        Canvas canvas = new Canvas();
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int pixelColor = bitmap.getPixel(x,y);
                int red = Color.red(pixelColor);
                int blue = Color.blue(pixelColor);
                int green = Color.green(pixelColor);

                int newRed = red*redFilter;
                int newBlue = blue*redFilter;
                int newGreen = green*redFilter;

                int newColor = Color.rgb(newRed,newGreen,newBlue);
                //int newColor = pixelColor*redFilter;
                bitmap.setPixel(x,y,newColor);
            }
        }
        // Hier können die Pixel im data-array bearbeitet und
        // anschliessend damit ein neues Bitmap erstellt werden
        return bitmap;
    }

    // Logbucheintrag
    // --------------

    public void sendResultMethod(View view){
        log(resultText.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                resultText.setVisibility(View.VISIBLE);
                sendResult.setVisibility(View.VISIBLE);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

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

    private boolean checkInstalled(Intent intent, String appName){
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, appName + " App not Installed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}