package com.dao.ioe.ui.avaliablesstreamings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dao.ioe.net.connection.Publisher;
import com.dao.ioe.net.connection.Subscriber;
import com.dao.ioe.net.connection.TSubscriber;
import com.dao.ioe.net.connection.WifiAwareSessionUtillities;
import com.dao.ioe.scrcpy.R;
import com.dao.ioe.ui.main.MainActivity;
import com.dao.ioe.ui.streaming.StreamingView;
import com.dao.ioe.ui.watchstreaming.SubscriberObserver;
import com.dao.ioe.ui.watchstreaming.WatchStreamingActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class AvaliablesStreamingsFragment extends Fragment implements AvaliablesStreamingsView, StreamingView, SubscriberObserver {

    private RecyclerView streamingList;
    private ArrayList<AvaliablesStreamingListData> devices;
    private PeerHandle currentDevice;
    private AvaliablesStreamingAdapter adapter;
    private Subscriber subscriber;
    private ProgressBar progressBar;
    private String videoFilePath;
    private SharedPreferences preferences;
    private TextView noDevicesFound;
    private Publisher publisher;
    private Socket clientSocket;
    private OutputStream socketOutput;
    private boolean streaming;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_avaliables, container, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        devices = new ArrayList<>();

        streamingList = getActivity().findViewById(R.id.streaminglist);

        adapter = new AvaliablesStreamingAdapter(devices,this);
        streamingList.setHasFixedSize(true);
        streamingList.setLayoutManager(new GridLayoutManager(getContext(), 1 ,GridLayoutManager.VERTICAL,false));
        streamingList.setAdapter(adapter);



        progressBar = getActivity().findViewById(R.id.progressBar);
        streamingList.setVisibility(View.VISIBLE);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        noDevicesFound = getActivity().findViewById(R.id.nodevice);

        WifiAwareSession session = WifiAwareSessionUtillities.getSession();
        subscriber = new Subscriber( (MainActivity) getActivity(), AvaliablesStreamingsFragment.this);

        session.subscribe(Subscriber.CONFIGSUBS,subscriber,null);
        TSubscriber.subscriber = subscriber;

    }

    public void pulsarDevice(PeerHandle peerHandle){
        if(preferences.getBoolean("compartir", false) || preferences.getBoolean("repetidor", false)){
            subscriber.realizaConexion(peerHandle);
            subscriber.setObserver(this);
            WifiAwareSession session = WifiAwareSessionUtillities.getSession();
            publisher = new Publisher(AvaliablesStreamingsFragment.this, getConnectivityManager());
            session.publish(Publisher.CONFIGPUBL,publisher,null);
            streamingList.setVisibility(View.INVISIBLE);
            noDevicesFound.setVisibility(View.VISIBLE);
            noDevicesFound.setText("Retransmitiendo automáticamente");
            currentDevice = peerHandle;
        }
        else{
            streamingList.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            subscriber.realizaConexion(peerHandle);
        }

    }

    public void cambiarVista(){

        if(!preferences.getBoolean("compartir", false) && !preferences.getBoolean("repetidor", false)) {
            progressBar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(getContext(), WatchStreamingActivity.class);
            getActivity().startActivity(intent);
        }

        //streamingList.setVisibility(View.VISIBLE);

    }


    @Override
    public void addDevice(String device, String mac, PeerHandle peerHandle) {
        if(preferences.getBoolean("repetidor", false) && !streaming){
            pulsarDevice(peerHandle);
            streaming = true;
        }
        else{
            devices.add(new AvaliablesStreamingListData(device,mac,peerHandle));
            if(!noDevicesFound.getText().toString().equals("Retransmitiendo automáticamente"))
                noDevicesFound.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    @Override
    public void stopStreaming() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(preferences.getBoolean("repetidor", false)){
                    streaming = false;
                    if(!devices.isEmpty())
                        pulsarDevice(devices.get(0).getPeerHandle());
                }
                else{
                    for(AvaliablesStreamingListData device: devices){
                        if(device.getPeerHandle().equals(currentDevice))
                            devices.remove(device);
                    }
                    currentDevice = null;
                    if(!devices.isEmpty()){
                        streamingList.setVisibility(View.VISIBLE);
                    }
                    else{
                        noDevicesFound.setVisibility(View.VISIBLE);
                        noDevicesFound.setText("Buscando retransmisiones cercanas...");
                    }
                }
            }
        });
    }
}
