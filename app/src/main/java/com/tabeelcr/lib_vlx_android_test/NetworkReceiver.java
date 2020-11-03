package com.tabeelcr.lib_vlx_android_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ( activeNetInfo != null ) {
            Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            Log.i("Active Network Type", activeNetInfo.getTypeName());
        } else if ( mobNetInfo != null ) {
            Toast.makeText( context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
            Log.i("Active Network Type", mobNetInfo.getTypeName());
        } else {
            Toast.makeText( context, "No network", Toast.LENGTH_SHORT ).show();
            Log.i("Active Network Type", "No connection");
        }
    }
}
