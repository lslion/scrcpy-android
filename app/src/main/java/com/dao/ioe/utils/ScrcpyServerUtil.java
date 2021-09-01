package com.dao.ioe.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.dao.ioe.scrcpy.Server;

import static com.dao.ioe.utils.utils.logd;

public class ScrcpyServerUtil {

    private static String TAG = "ScrcpyServerUtil";

    public static HandlerThread mScrcpyServerThread;
    static Handler sHandler;
    static Runnable sConnectRunnable;

    public static boolean runningScrcpyServer(Context context) {
        if (mScrcpyServerThread == null) {
            mScrcpyServerThread = new HandlerThread("ScreenCopyServer");
            mScrcpyServerThread.start();
            sHandler = new Handler(mScrcpyServerThread.getLooper());
            sConnectRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // default 720p, maxSize:1560, bitrate:4Mbps
                        logd(TAG, "enter runningScrcpyServer");
                        String maxSize = "0";
                        String bitRate = "6000000";

                        //ScrClenet.getInstance().stopCastServer();
                        Server.getInstance().main(maxSize, bitRate);
                        logd(TAG, "Launch screen record service successfully.");
                    }catch (Exception e) {
                        logd(TAG, "Launch screen record service failed." + e);
                    }
                }
            };
        }
        sHandler.removeCallbacks(sConnectRunnable);
        sHandler.post(sConnectRunnable);
        return true;
    }
}
