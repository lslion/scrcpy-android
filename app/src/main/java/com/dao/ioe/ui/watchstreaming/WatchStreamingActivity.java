package com.dao.ioe.ui.watchstreaming;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.dao.ioe.IOEApplication;
import com.dao.ioe.net.connection.Publisher;
import com.dao.ioe.net.connection.Subscriber;
import com.dao.ioe.net.connection.TSubscriber;
import com.dao.ioe.net.connection.WifiAwareSessionUtillities;
import com.dao.ioe.scrcpy.R;
import com.dao.ioe.scrcpyclient.ScrcpyClient;
import com.dao.ioe.ui.streaming.StreamingView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.dao.ioe.utils.utils.logd;

public class WatchStreamingActivity extends AppCompatActivity implements ScrcpyClient.ServiceCallbacks, SensorEventListener, SubscriberObserver, StreamingView {

    private Subscriber subscriber;
    private Publisher publisher;

    private Socket clientSocket;
    private OutputStream socketOutput;

    private boolean first_time = true;
    private static boolean resultofRotation = false;
    private static boolean serviceBound = false;
    private static int screenWidth;
    private static int screenHeight;
    private static boolean landscape = false;
    private ScrcpyClient scrcpy;
    private String TAG = "WatchStreamingActivity";

    private SharedPreferences preferences;
    private SurfaceView surfaceView;
    private Surface surface;
    private Socket mScrcpyClientSocket;
    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.surface_nav);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        subscriber = TSubscriber.subscriber;
        subscriber.setObserver(this);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor proximity;
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);


        mScrcpyClientSocket = IOEApplication.getInstance().mScrcpyClientVideoSocket;

        if(preferences.getBoolean("compartirVer", false)){
            WifiAwareSession session = WifiAwareSessionUtillities.getSession();

            publisher = new Publisher(WatchStreamingActivity.this, getConnectivityManager());
            session.publish(Publisher.CONFIGPUBL,publisher,null);
        }
        //this.getWindow().setStatusBarColor(Color.argb(255,0,0,0));
        //this.getWindow().setNavigationBarColor( Color.argb(255,0,0,0));
        startwithoutNav();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            scrcpy = ((ScrcpyClient.MyServiceBinder) iBinder).getService();
            scrcpy.setServiceCallbacks(WatchStreamingActivity.this);
            if (surface != null) {
                logd(TAG, "surface is not null");
            }
            if (mScrcpyClientSocket != null) {
                logd(TAG, "mScrcpyServerSocket is not null");
            }
            screenWidth = 1080;
            screenHeight = 2340;
            logd(TAG, "screenHeight is:" + screenWidth + ":" + screenHeight);
            if (first_time) {
                scrcpy.start(surface, mScrcpyClientSocket, screenHeight, screenWidth);
            } else {
                scrcpy.setParms(surface, screenWidth, screenHeight);
            }
            first_time = false;
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void startwithoutNav() {
        setContentView(R.layout.surface_no_nav);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        surfaceView = (SurfaceView) findViewById(R.id.decoder_surface);
        surface = surfaceView.getHolder().getSurface();
        startScrcpyserver();
        DisplayMetrics metrics = new DisplayMetrics();

        if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
            getWindowManager().getDefaultDisplay().getMetrics(metrics);


        } else {
            final Display display = getWindowManager().getDefaultDisplay();
            display.getRealMetrics(metrics);
        }
        final int height = metrics.heightPixels;
        final int width = metrics.widthPixels;


        logd(TAG, "width and height is: " + width + ":" + height);
        logd(TAG, "screenWidth and screenHeight is: " + screenWidth + ":" + screenHeight);
        screenWidth = 1080;
        screenHeight = 2340;
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                logd(TAG, "onTouch is: " + event);
                return scrcpy.touchevent(event, width, height);
            }
        });


    }

    @SuppressLint("ClickableViewAccessibility")
    private void startwithNav() {

        setContentView(R.layout.surface_nav);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        final Button backButton = (Button) findViewById(R.id.back_button);
        final Button homeButton = (Button) findViewById(R.id.home_button);
        final Button appswitchButton = (Button) findViewById(R.id.appswitch_button);

        surfaceView = (SurfaceView) findViewById(R.id.decoder_surface);
        surface = surfaceView.getHolder().getSurface();
        startScrcpyserver();
        DisplayMetrics metrics = new DisplayMetrics();
        int offset = 0;

        if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
            final Display display = getWindowManager().getDefaultDisplay();
            display.getRealMetrics(metrics);
            offset = 100;

        } else {
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }

        final int height = metrics.heightPixels - offset;
        final int width = metrics.widthPixels;
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return scrcpy.touchevent(event, width, height);

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrcpy.sendKeyevent(4);

            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrcpy.sendKeyevent(3);

            }
        });

        appswitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrcpy.sendKeyevent(187);

            }
        });


    }

    public void startScrcpyserver() {
        Intent intent = new Intent(this, ScrcpyClient.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] == 0) {
                if (serviceBound) {
                    scrcpy.sendKeyevent(28);
                }
            } else {
                if (serviceBound) {
                    scrcpy.sendKeyevent(29);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void loadNewRotation() {
        unbindService(serviceConnection);
        serviceBound = false;
        resultofRotation = true;
        landscape = !landscape;
        swapDimensions();
        if (landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    private void swapDimensions() {
        int temp = screenHeight;
        screenHeight = screenWidth;
        screenWidth = temp;
    }
    public void stopScrcpyClientServer() {
        if (serviceBound) {
            scrcpy.StopService();
            unbindService(serviceConnection);
        }
        serviceBound = false;
        Socket clientSocket = IOEApplication.getInstance().mScrcpyClientVideoSocket;
        if (clientSocket != null && clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopStreaming() {
        stopScrcpyClientServer();
    }



    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //streamProxy.stop();
        stopStreaming();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopStreaming();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!first_time && !resultofRotation) {
            final View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (serviceBound) {
                scrcpy.resume();
            }
        }
        resultofRotation = false;
    }


    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void startStreamingPublic() {
        clientSocket = null;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket( publisher.getAddress() , publisher.getPort() );
                    socketOutput = clientSocket.getOutputStream();
                    subscriber.setNodoTransito(socketOutput);
                    publisher.callSubscriber();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        t.start();
    }



}
