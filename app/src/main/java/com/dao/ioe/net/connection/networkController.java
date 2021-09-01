package com.dao.ioe.net.connection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.aware.WifiAwareManager;

import androidx.core.content.ContextCompat;

import com.dao.ioe.IOEApplication;

public class networkController {

    public static boolean checkWifiAwareAvaliability(){
        if (!IOEApplication.getInstance().getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            return false;
        }
        return true;
    }

    public static boolean initWifiAware(){
        if (ContextCompat.checkSelfPermission(IOEApplication.getInstance().getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(IOEApplication.getInstance().getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkWifiAwareAvaliability()) {
            WifiAwareManager wifiAwareManager = (WifiAwareManager) IOEApplication.getInstance().getContext().getSystemService(Context.WIFI_AWARE_SERVICE);
            WifiAwareSessionUtillities.setManager(wifiAwareManager);
            WifiAwareSessionUtillities.createSession();
            return true;
        }
        return false;
    }

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager)IOEApplication.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

}
