package ch.appquest.brudinne.memory;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

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

    private RecyclerView rv;
    private MyAdapter adapter;
    private ArrayList<View> list;
    //private String currentPhotoPath;

    private SharedPreferences settings;

    private JSONArray pairValues;
    public static  final String FILE_NAME = "MyJsonFile";
    public static final String STRING_NAME = "JSON";

    private String PICTURE_NAME; // name of picture file
    private String PICTURE_PATH; // path of saved picture (without name)
    private File PICTURE_FILE;   // picture file with standard app internal storage path and picture name

    // TODO Natalie: change string to picture bitmap save variable
    //private String PICTURE_NAME = "hello world!";

    /**
     * on create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get saved instance of JSON-Array with Name-Pairs and their links of pictures
        settings = getSharedPreferences(FILE_NAME, 0);
        String memoryPairs = settings.getString("JSON", null);
        if(memoryPairs != null){
            try {
                pairValues = new JSONArray(memoryPairs);
                setPictruesAndNames();
            } catch(JSONException e){
                //TODO Natalie: check errorhandling
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No saved pictures.", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);
        rv = (RecyclerView)findViewById(R.id.recyclerView);
        //adapter = new CustomAdapter(getApplicationContext(),ApplicationState.getGridElements());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this /* the activity */, 2);
        rv.setLayoutManager(gridLayoutManager);

        list = new ArrayList<>();

        adapter = new MyAdapter(list, this);
        rv.setAdapter(adapter);

        createFirstRow();


        //createNewCards(2,3);
    }

    @Override
    protected void onStop(){
        super.onStop();
        // Saving JSON-Array before app gets hidden
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        settings = getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        // TODO: entcomment!
        editor.putString(STRING_NAME, pairValues.toString());

        // Committing the edits
        editor.commit();
    }

    public void onStart(){
        super.onStart();
        //adapter.deleteButton(rv, 2);
    }

    private void createFirstRow(){
        list.add(new View(this));
        list.add(new View(this));
        View l0 = list.get(0);
        View l1 = list.get(1);
        overrideOnClick((Button) l0.findViewById(R.id.newCard));
        overrideOnClick((Button) l0.findViewById(R.id.newCardPhoto));
        overrideOnClick((Button) l1.findViewById(R.id.newCard));
        overrideOnClick((Button) l1.findViewById(R.id.newCardPhoto));

    }
    private void overrideOnClick(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int listSize = list.size();
                createNewCards(listSize++, listSize++);
            }
        });
    }

    public void createNewCard(int ind){
        if(ind > 1){
            list.add(ind, new View(this));
        }
    }

    public void createNewCards(int ind1, int ind2){
        if(ind1 > 1 || ind2 > 1){
            list.add(ind1, new View(this));
            list.add(ind2, new View(this));
        }
    }

    /**
     * onklick newCard "take picture"
     */
    public void takeQrCodePicture(View view) {
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
            try {
                savePicture(BitmapFactory.decodeFile(path), code /*, 1*/);
            } catch (IOException e) {
                //TODO Natalie: check errorhandling
                e.printStackTrace();
            }
        }

    }

    private void setMemoryPicture(Bitmap bmp, String word){
        /*
        ImageView imageView = new ImageView();
        TextView textView   = new TextView();
        CardView cardView   = new CardView();
        cardView.addView(imageView);
        cardView.addView(textView);
        */
    }

    private void setPictruesAndNames(){
        try {
            for(int i = 0; i < pairValues.length(); i++){
                JSONObject object = pairValues.getJSONObject(i);
                String description = object.getString("name");
                PICTURE_PATH       = object.getString("filepath");
                PICTURE_NAME       = object.getString("filename");
                // TODO Natalie: implement insert from picture and description in app
                if(description != "null" && PICTURE_NAME != "null" && PICTURE_PATH != "null"){
                    Bitmap picture = loadImageFromStorage();
                    //TODO : insert in view
                    adapter.deleteButton(new ViewGroup(this) {
                        @Override
                        protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
                        }
                    }, i + 2);
                } else {
                    // TODO Natalie: check that nothing is inserted where the empty picture should bee...
                }
            }
        } catch(JSONException e) {
            //TODO Natalie: check errorhandling
            e.printStackTrace();
        }

    }

    private void savePicturesAndJson(){
        try{
            pairValues.toString();
        } catch(Exception e){
            //TODO Natalie: check errorhandling
            e.printStackTrace();
        }
    }


    private void savePicture(Bitmap picture, String word /*, int index*/) throws IOException {
        // Create Filename for picture
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        PICTURE_NAME = Environment.getExternalStorageDirectory() + "/" + word + "_" + timeStamp +".jpg";

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        PICTURE_FILE = new File(directory, PICTURE_NAME);

        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(PICTURE_FILE);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e){
            //TODO Natalie: check errorhandling
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                //TODO Natalie: check errorhandling
                e.printStackTrace();
            }
            PICTURE_PATH = directory.getAbsolutePath();
        }
        /*File tempFile = new File(tempFilePath);
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
        */

        //return tempFilePath;
    }

    private Bitmap loadImageFromStorage()
    {
        Bitmap picture = null;
        try {
            File f = new File(PICTURE_PATH, PICTURE_NAME);
            picture = BitmapFactory.decodeStream(new FileInputStream(f));
            /*
            ImageView img=(ImageView)findViewById(R.id.image);
            img.setImageBitmap(picture);
            */
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return picture;
    }


























    // log message handling
    // --------------------

    /**
     * prepare log newCard with QR-Code Intent
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
                if(pairValues != null){
                    String resultArray = pairValues.toString();
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