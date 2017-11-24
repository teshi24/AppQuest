package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Nadja on 24.11.2017.
 */

public class InternetHandler {

    boolean dialogWlanHasAlreadyOccured = false;

    Context context;

    public InternetHandler(Context context){
        this.context = context;
    }

    /**
     * checks if the internet is accessible
     * @param context
     */
    public void checkInternetConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null){
            if(!dialogWlanHasAlreadyOccured) {
                internetSettingsDialog();
                dialogWlanHasAlreadyOccured = true;
            }else{
                dialogWlanHasAlreadyOccured = false;
            }
        }else if(!activeNetwork.isConnectedOrConnecting()){
            internetConnectionInfo(context);
        }
    }

    /**
     * internet dialog <br>
     * cancel or go to wlan settings or go to internet settings
     */
    public void internetSettingsDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Enable Internet");
        alertDialog.setMessage("You have no internet connection. Please enabled it in settings menu.");
        alertDialog.setPositiveButton("Internet Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        });
        // todo: add 3rd button for internet
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    // TODO: maybe delete
    /**
     * information when internet is enabled but there is no access
     * @param context
     */
    public void internetConnectionInfo(Context context){
        // todo: add information dialog no connection
        Toast.makeText(context, "internet not working", Toast.LENGTH_LONG).show();
    }
}
