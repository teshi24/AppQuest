package com.appquest.brudinne.treasurehunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;


public class InternetHandler {

    // dialog flag
    private boolean dialogHasAlreadyOccured = false;

    // context variable
    private Context context;

    public InternetHandler(Context context) {
        this.context = context;
    }


    // internet handling
    // -----------------

    /**
     * checks if the internet is accessible
     *
     * @param context
     */
    public void checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            if (!dialogHasAlreadyOccured) {
                internetSettingsDialog();
                dialogHasAlreadyOccured = true;
            } else {
                dialogHasAlreadyOccured = false;
            }
        }
    }

    /**
     * internet dialog <br>
     * cancel or go to wlan settings or go to internet settings
     */
    private void internetSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Enable Internet");
        alertDialog.setMessage("You have no internet connection. Please enabled it in settings menu.");
        alertDialog.setPositiveButton("Internet Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Wlan Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
