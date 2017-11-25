/**
 * Date:            03.11.2017
 * Version:         2.0
 * Author:          Natalie Stalder, Nadja Stadelmann
 * AppQuest 2017:
 * Team:            Brudinne
 * App 3:           Memory
 * Version Test:
 * Handy:           APK 23
 * Emulator:        APK 26
 * <p>
 * Version Changes
 * ---------------
 * V 2.0
 * -----
 * app improved significantly (with credits to Manuel Kohler!)
 * changes:
 * - button changed to ImageButton - shows picture now
 * - new concept with adding in the onClick method:
 * - new param Card to get the index
 * - var buttonIndex removed
 * - only 1 main launcher when installing the app (intentFilter in manifest removed)
 * <p>
 * V 1.0
 * -----
 * app is running as wanted
 * small bugs:
 * - button shows no picture
 * - click on a button in first row without finishing takePictureIntent properly adds a new empty row..
 * - 2 main launcher when installing the app
 */
package ch.appquest.brudinne.memory;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // variables for views
    private RecyclerView        rv;
    private GridLayoutManager   gridLayoutManager;
    private MyAdapter           adapter;
    private ArrayList<Card>     list;

    // variables to save list in JSON
    private SharedPreferences   settings;
    private JSONArray           pairValues;
    public static final String  FILE_NAME = "MyJsonFile";
    public static final String  STRING_NAME = "JSON";
    private String              pictureName;
    private String              picturePath;
    private File                pictureFile;

    // variable for position
    private Card card;

    // android lifecycle handling
    // --------------------------

    /**
     * creation of the app - initialize views
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();

        // get saved instance of JSON-Array with Name-Pairs and their links of pictures
        // and fill list with those values
        settings = getSharedPreferences(FILE_NAME, 0);
        String memoryPairs = settings.getString("JSON", null);
        if (memoryPairs != null) {
            try {
                pairValues = new JSONArray(memoryPairs);
                setUpList();
            } catch (JSONException e) {
                Toast.makeText(this, "Problem with saved values.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No saved pictures.", Toast.LENGTH_LONG).show();
        }

        // set up view
        setContentView(R.layout.activity_main);
        rv                  = (RecyclerView) findViewById(R.id.recyclerView);
        gridLayoutManager   = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);

        adapter = new MyAdapter(list, this);
        rv.setAdapter(adapter);
    }

    /**
     * resume app - notify adapter if needed
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateAndInsert();
    }

    /**
     * save our view on pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Saving JSON-Array before app gets hidden
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        JSONArray json                  = listToJson();
        settings                        = getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(STRING_NAME, json.toString());

        // Committing the edits
        editor.commit();
    }

    // photo handling
    // --------------

    /**
     * method is called by click on a button <br>
     * it calls the QR-Code-Scanner app internal to scan a new photo
     * @param card is necessary to get the position of a new photo
     */
    public void takeQrCodePicture(Card card) {
        this.card = card;
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan QR-Code");
        integrator.setBeepEnabled(false);
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    /**
     * set a new PictureCard and maybe a new row to the view <br>
     * with the photo which was taken by IntentIntegrator
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
                // get data
                Bundle extras   = data.getExtras();
                String path     = extras.getString(Intents.Scan.RESULT_BARCODE_IMAGE_PATH);
                String code     = extras.getString(Intents.Scan.RESULT);

                // load card if the scanner was able to decode the QR-Code
                if (code != null && !code.equals("")) {
                    try {
                        PictureCard newCard = savePicture(BitmapFactory.decodeFile(path), code);
                        if (newCard != null) {
                            int index = list.indexOf(card);
                            // changes index to zero if a new row needs to be instantiated<br>
                            // to be sure that it is set to the left-hand-side
                            if (index == 1) {
                                index--;
                            }
                            list.set(index, newCard);
                            updateAndInsert();
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "A problem occurred while saving the picture.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "QR-Code cannot be encoded correctly", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * update the view and insert a new row if needed
     */
    private void updateAndInsert() {
        boolean noEmptyPair = true;
        // check if a new row is needed
        int s = list.size();
        for (int i = 0; i < s; i = i + 2) {
            if (list.get(i) instanceof ButtonCard && list.get(i + 1) instanceof ButtonCard) {
                noEmptyPair = false;
            }
        }
        // add a new row at the beginning if its needed
        if (noEmptyPair) {
            list.add(0, new ButtonCard());
            list.add(1, new ButtonCard());
        }
        // update view and saved array
        pairValues = listToJson();
        adapter.notifyDataSetChanged();
    }

    // save 'n load handling
    // ---------------------

    /**
     * load saved array into the working list
     */
    private void setUpList() {
        Card c;
        try {
            // load array into list
            for (int i = 0; i < pairValues.length(); i++) {
                JSONObject object   = pairValues.getJSONObject(i);
                String description  = object.getString("name");
                picturePath         = object.getString("filepath");
                pictureName         = object.getString("filename");
                // check if the information are enough for a PictureCard
                if (!description.equals("null") && !pictureName.equals("null") && !picturePath.equals("null")) {
                    Bitmap picture = loadImageFromStorage();
                    if (picture != null) {
                        c = new PictureCard(picture, description, picturePath, pictureName);
                    } else {
                        c = new ButtonCard();
                    }
                } else {
                    c = new ButtonCard();
                }
                list.add(c);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Load saved information not possible.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * load working list into the saved array
     * @return
     */
    private JSONArray listToJson() {
        JSONArray json = new JSONArray();
        for (Card item : list) {
            JSONObject object = new JSONObject();
            // set PictureCard information or null values depending on the instance
            if (item instanceof PictureCard) {
                try {
                    PictureCard pictureCard = (PictureCard)item;
                    object.put("name",      pictureCard.getDescription());
                    object.put("filepath",  pictureCard.getFilepath());
                    object.put("filename",  pictureCard.getFilename());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    object.put("name",      "null");
                    object.put("filepath",  "null");
                    object.put("filename",  "null");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            json.put(object);
        }
        return json;
    }

    /**
     * get picture from internal storage
     * @return
     */
    private Bitmap loadImageFromStorage() {
        Bitmap picture = null;
        try {
            File f  = new File(picturePath, pictureName);
            picture = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return picture;
    }

    /**
     * save a picture and put it into a PictureCard
     * @param picture
     * @param word
     * @return
     * @throws IOException
     */
    private PictureCard savePicture(Bitmap picture, String word) throws IOException {
        // create file name for picture
        String timeStamp    = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        pictureName         = word + "_" + timeStamp + ".jpg";

        // save file
        ContextWrapper cw   = new ContextWrapper(getApplicationContext());
        File directory      = cw.getDir("imageDir", Context.MODE_PRIVATE);
        pictureFile         = new File(directory, pictureName);
        picturePath         = directory.getAbsolutePath();

        // load picture into PictureCard
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pictureFile);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                return new PictureCard(picture, word, picturePath, pictureName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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
        Intent intent   = new Intent("ch.appquest.intent.LOG");
        JSONObject log  = new JSONObject();
        if (checkInstalled(intent, "Logbook")) {
            try {
                // set up log JSONObject from saved array
                JSONArray resultArray   = new JSONArray();
                int size                = pairValues.length();
                for (int i = 0; i < size; ++i) {
                    JSONObject oLeft    = (JSONObject) pairValues.get(i);
                    String nameLeft     = (String) oLeft.get("name");
                    JSONObject oRight   = (JSONObject) pairValues.get(++i);
                    String nameRight    = (String) oRight.get("name");
                    // log card pairs only
                    if (!nameLeft.equals("null") && !nameRight.equals("null")) {
                        JSONArray pair = new JSONArray();
                        pair.put(nameLeft);
                        pair.put(nameRight);

                        resultArray.put(pair);
                    }
                }
                if(resultArray != null && resultArray.length() > 0) {
                    log.put("task",     "Memory");
                    log.put("solution", resultArray);
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
}