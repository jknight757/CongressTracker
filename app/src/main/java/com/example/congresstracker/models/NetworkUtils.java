package com.example.congresstracker.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static boolean isConnected(Context _context){
        Log.i("TAG", "onHandleIntent: connection check");
        ConnectivityManager connectMgr = (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(connectMgr != null){
                NetworkCapabilities capabilities =
                        connectMgr.getNetworkCapabilities(connectMgr.getActiveNetwork());
                if(capabilities != null){
                    if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                        return true;
                    }
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getNetworkData(String _url) {

        String data = "";
        try {


            URL url = new URL(_url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-API-Key", "ewhK3e7sQB61DVlX2Q4KYjyQeofyme0ZjrQDCK3i");
            connection.connect();

            InputStream is = connection.getInputStream();
            data = IOUtils.toString(is);
            is.close();

            connection.disconnect();


        }catch (Exception e){
            e.printStackTrace();
            Log.i("TAG", "onHandleIntent: error on request");
        }
        return data;
    }

}
