package com.dao.ioe.net.connection;

import android.net.wifi.aware.IdentityChangedListener;

public class OwnIdentityChangedListener extends IdentityChangedListener {

    @Override
    public void onIdentityChanged(byte[] mac) {
        super.onIdentityChanged(mac);
        WifiAwareSessionUtillities.setMac(mac);
    }
}
