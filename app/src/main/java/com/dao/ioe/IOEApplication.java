package com.dao.ioe;

import android.app.Application;
import android.content.Context;

import com.dao.ioe.utils.Pair;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class IOEApplication extends Application {
    private static IOEApplication sInstance;
    private Context mContext;
    public volatile List<Pair> mScrcpyServerVideoSocketOutputs;
    public volatile List<Pair> mScrcpyServerControlSocketOutputs;
    public boolean streaming = false;
    public Socket mScrcpyClientVideoSocket;
    public Socket mScrcpyClientControlSocket;

    public static IOEApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mScrcpyServerVideoSocketOutputs = new ArrayList<>();
        mScrcpyServerControlSocketOutputs = new ArrayList<>();
        mContext = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public Context getContext() {
        return mContext;
    }

}
