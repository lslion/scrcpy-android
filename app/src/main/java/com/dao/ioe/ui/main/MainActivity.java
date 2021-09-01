package com.dao.ioe.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dao.ioe.scrcpy.R;
import com.dao.ioe.ui.avaliablesstreamings.AvaliablesStreamingsFragment;
import com.dao.ioe.ui.streaming.StreamingFragment;

import static com.dao.ioe.net.connection.networkController.initWifiAware;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static int screenWidth;
    private static int screenHeight;
    private static boolean landscape = false;
    private static boolean first_time = true;
    private static boolean resultofRotation = false;
    private static boolean serviceBound = false;
    private Context context;
    private SurfaceView surfaceView;
    private long timestamp = 0;

    private LinearLayout[] mImageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (first_time) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_main);
//            ActionBar actionBar = getActionBar();
//            if (actionBar != null) {
//                actionBar.setHomeButtonEnabled(true);
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
            setStatusBarColor(this);
            //final Button startButton = (Button) findViewById(R.id.button_start);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lllayout);
            int viewCount = linearLayout.getChildCount();
            mImageViews = new LinearLayout[viewCount];
            for (int i = 0; i < viewCount; i++) {
                mImageViews[i] = (LinearLayout) linearLayout.getChildAt(i);
                mImageViews[i].setEnabled(true);
                mImageViews[i].setOnClickListener(this);
                mImageViews[i].setTag(i);
            }

        }

        this.context = this;

        checkPermission();

        initWifiAware();
        setCurPoint(0);

    }

    public static void setStatusBarColor(Activity act) {
        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            act.getWindow().setStatusBarColor(
                    act.getResources().getColor(R.color.stats_icon_color));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (timestamp == 0) {
            timestamp = SystemClock.uptimeMillis();
            Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show();
        } else {
            long now = SystemClock.uptimeMillis();
            if (now < timestamp + 1000) {
                timestamp = 0;
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
            timestamp = 0;
        }

    }







    private void checkPermission(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 2);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"No has permission granted.", Toast.LENGTH_LONG).show();
        }
        else{
            if(requestCode == 1)
                initWifiAware();
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int pos = (Integer) (v.getTag());
        setCurPoint(pos);
    }

    private void setCurPoint(int index) {

        if (index == 0) {
            ((ImageView) findViewById(R.id.connect_img)).setImageResource(R.drawable.wifi_select);
            //remove discover
            //((ImageView) findViewById(R.id.discover_img)).setImageResource(R.drawable.discovery_not_select);
            ((ImageView) findViewById(R.id.box_img)).setImageResource(R.drawable.box_not_select);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new StreamingFragment()).commit();

        } else {

            ((ImageView) findViewById(R.id.connect_img)).setImageResource(R.drawable.wifi_not_select);
            //remove discover
            //((ImageView) findViewById(R.id.discover_img)).setImageResource(R.drawable.discovery_not_select);
            ((ImageView) findViewById(R.id.box_img)).setImageResource(R.drawable.box_select);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new AvaliablesStreamingsFragment()).commit();

        }
    }
}
