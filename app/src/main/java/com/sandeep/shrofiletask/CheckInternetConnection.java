package com.sandeep.shrofiletask;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by sandeep on 25-07-2017.
 */

public class CheckInternetConnection
{
    public static boolean isConnecetedToInternet(Context context)
    {

        boolean networkStatus = false;

        // Get connect mangaer
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // check for wifi
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // check for mobile data
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( wifi.isConnected() ) {
            networkStatus = true;
        } else if( mobile.isConnected() ) {
            networkStatus = true;
        } else {
            networkStatus = false;
        }

        return networkStatus;


    }
}
