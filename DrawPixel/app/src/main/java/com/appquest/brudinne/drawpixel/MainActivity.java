/**
 * V1
 * --
 * app is running, basic functions are working
 * bugs:
 * - eraser not working
 * - DrawingPixel as class abgrenzen
 */

package com.appquest.brudinne.drawpixel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private ImageButton currentBrush;
    private ImageButton currentUtil;

    private SharedPreferences appPreferences;
    private boolean isShortcutInstalled = false;

    public void eraseClicked(View view) {
        if (view != currentBrush) {
            ImageButton imgView = (ImageButton) view;
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.selected));
            currentBrush.setImageDrawable(null);
            currentBrush = (ImageButton) view;
        }
        drawingView.setErase(true);
    }

    public void switchUtil(View view){
        if(view != currentUtil) {
            currentUtil = (ImageButton) view;
            String util = (String) currentUtil.getContentDescription();
            if(util.equals(getString(R.string.brush)))
                drawingView.setUtil(0);
            else if(util.equals(getString(R.string.can)))
                drawingView.setUtil(1);
            else if(util.equals(getString(R.string.change)))
                drawingView.setUtil(2);
        }
    }

    public void createShortCut(){
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), MainActivity.class));
        sendBroadcast(shortcutintent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isShortcutInstalled = appPreferences.getBoolean("isShortcutInstalled",false);
        if(isShortcutInstalled == false){
            createShortCut();
            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isShortcutInstalled", true);
            editor.commit();
        }

        setContentView(R.layout.activity_main);
        drawingView = (DrawingView) findViewById(R.id.drawing);

        currentBrush = (ImageButton) findViewById(R.id.paintBlue);
        currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.selected));
        String color = currentBrush.getTag().toString();
        drawingView.setColor(color);
    }

    private void onCreateNewDrawingAction() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("New Drawing");
        newDialog.setMessage("Start a new drawing?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                drawingView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    public void paintClicked(View view) {
        if (view != currentBrush) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawingView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.selected));
            currentBrush.setImageDrawable(null);
            currentBrush = (ImageButton) view;
        }
        drawingView.setErase(false);
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
        MenuItem menuItem = menu.add("New");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                onCreateNewDrawingAction();
                return true;
            }
        });

        menuItem = menu.add("Log");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //log(drawingView.getList());
                log();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * send log message to Logbook
     */
    private void log() {
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();
        if (checkInstalled(intent, "Logbook")) {
            try {
                //makeJSONArray();
                // todo: replace with 'real' array
                //setPixels();

                JSONArray savedArray = new JSONArray();

                ArrayList<DrawingPixel> pixels = drawingView.getPixelList();
                int pixelAmount = pixels.size();
                int stepSizeX = drawingView.getStepSizeX();
                int stepSizeY = drawingView.getStepSizeY();

                for(int i = 0; i<pixelAmount; i++){
                    DrawingPixel bigPixel = pixels.get(i);
                    int color = bigPixel.getColor();
                    if(color == Color.WHITE){
                        continue;
                    }
                    Rect drawRect = bigPixel.getRect();
                    //todo: maybe easier way to do this
                    int x = (int)Math.floor(drawRect.left / stepSizeX);
                    int y = (int)Math.floor(drawRect.top / stepSizeY);

                    JSONObject pixel = new JSONObject();
                    pixel.put("y", ""+y);
                    pixel.put("x", ""+x);
                    pixel.put("color", String.format("#%08X", color));

                    savedArray.put(pixel);
                }

                /*
                ArrayList<ArrayList<Paint>> pixels = drawingView.getPixels();
                int xLength = pixels.size();
                for(int x = 0; x<xLength; x++){
                    int yLength = pixels.get(x).size();
                    for(int y = 0; y<yLength; y++){
                        int color = pixels.get(x).get(y).getColor();
                        if(color == Color.WHITE) {
                            continue;
                        }
                        JSONObject pixel = new JSONObject();
                        pixel.put("y", ""+y);
                        pixel.put("x", ""+x);
                        pixel.put("color", String.format("#%08X", color));

                        savedArray.put(pixel);
                    }
                }
                */

                if (savedArray != null && savedArray.length() > 0) {
                    log.put("task", "Pixelmaler");
                    log.put("pixels", savedArray);
                } else {
                    Toast.makeText(this, "No matches to log.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (JSONException e) {
                return;
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
