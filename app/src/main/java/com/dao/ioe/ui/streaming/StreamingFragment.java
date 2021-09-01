package com.dao.ioe.ui.streaming;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.dao.ioe.IOEApplication;
import com.dao.ioe.net.connection.Publisher;
import com.dao.ioe.net.connection.WifiAwareSessionUtillities;
import com.dao.ioe.scrcpy.R;
import com.dao.ioe.utils.Pair;
import com.dao.ioe.utils.ScrcpyServerUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.dao.ioe.net.connection.networkController.getConnectivityManager;
import static com.dao.ioe.utils.ScrcpyServerUtil.runningScrcpyServer;
import static com.dao.ioe.utils.utils.logd;

public class StreamingFragment extends Fragment implements StreamingView{

    private String TAG = "StreamingFragment";
    private FloatingActionButton recButton;
    private Publisher publisher;
    private MediaRecorder mMediaRecorder;
    private boolean streaming = false;
    private Thread mScrcpyServerThread, mPublisherThread;
    private Socket mScrcpyServerVideoSocket;
    private Socket mScrcpyServerControlSocket;
    private SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_streaming, container, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initRecButton();

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //getActivity().getWindow().setStatusBarColor(Color.argb(255,0,0,0));
        getActivity().getWindow().setNavigationBarColor( Color.argb(255,0,0,0));
    }

    public void startStreamingPublic(){
        mScrcpyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                startStreaming();
            } catch (IOException e) {
                System.out.println("Subscriber desconectado");
            }
            }
        });
        mScrcpyServerThread.start();
    }



    private void startStreaming() throws IOException {
        logd(TAG, "startStreaming: ");
        mScrcpyServerVideoSocket = null;
        mScrcpyServerControlSocket = null;
        OutputStream outs = null;

        mScrcpyServerVideoSocket = new Socket( publisher.getAddress() , publisher.getPort());
        mScrcpyServerControlSocket = new Socket( publisher.getAddress() , publisher.getPort());

//        outs = clientSocket.getOutputStream();
//        DataOutputStream outputStream = new DataOutputStream(outs);
        IOEApplication.getInstance().mScrcpyServerVideoSocketOutputs.add(new Pair(mScrcpyServerVideoSocket,false));
        IOEApplication.getInstance().mScrcpyServerControlSocketOutputs.add(new Pair(mScrcpyServerControlSocket,false));

        runningScrcpyServer(IOEApplication.getInstance().getApplicationContext());

        publisher.callSubscriber();


    }

    private void stopStreaming(){
        mPublisherThread.interrupt();
        streaming = false;
        IOEApplication.getInstance().streaming = streaming;
        //mMediaRecorder.release();
        WifiAwareSessionUtillities.restart();
        try {
            if(mScrcpyServerVideoSocket != null && !mScrcpyServerVideoSocket.isClosed()) mScrcpyServerVideoSocket.close();
            if(mScrcpyServerControlSocket != null && !mScrcpyServerControlSocket.isClosed()) mScrcpyServerControlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ScrcpyServerUtil.mScrcpyServerThread != null) {
            ScrcpyServerUtil.mScrcpyServerThread.quit();
        }
    }

    private void initRecButton(){
        recButton = getActivity().findViewById(R.id.rec);
        recButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!streaming){
                    streaming = true;
                    IOEApplication.getInstance().streaming = streaming;
                    recButton.setImageResource(R.drawable.square);
                    mPublisherThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                        try {
                            WifiAwareSession session = WifiAwareSessionUtillities.getSession();
                            publisher = new Publisher(StreamingFragment.this, getConnectivityManager());
                            session.publish(Publisher.CONFIGPUBL,publisher,null);

                            //startRecording();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                    });
                    mPublisherThread.start();
                }
                else{
                    stopStreaming();
                    recButton.setImageResource(R.drawable.videocam);
                }

            }
        });
    }
}
