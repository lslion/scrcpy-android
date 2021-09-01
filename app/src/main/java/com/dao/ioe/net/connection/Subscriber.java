package com.dao.ioe.net.connection;

import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;

import com.dao.ioe.IOEApplication;
import com.dao.ioe.ui.avaliablesstreamings.AvaliablesStreamingsView;
import com.dao.ioe.ui.main.MainActivity;
import com.dao.ioe.ui.watchstreaming.SubscriberObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static com.dao.ioe.utils.utils.logd;

public class Subscriber extends OwnDiscoverySessionCallback {

    private String TAG = "Subscriber";

    public static final SubscribeConfig CONFIGSUBS = new SubscribeConfig.Builder().setServiceName("CrowdStreaming").build();

    private SubscribeDiscoverySession session;

    private AvaliablesStreamingsView view;
    private MainActivity mainActivity;
    private SubscriberObserver observer;

    private Socket mScrcpyClientVideoSocket;
    private Socket mScrcpyClientControlSocket;

    public Subscriber(MainActivity activity, AvaliablesStreamingsView view){
        this.view = view;
        this.mainActivity = activity;
    }

    @Override
    public void onSubscribeStarted(SubscribeDiscoverySession session) {
        super.onSubscribeStarted(session);
        logd(TAG, "onSubscribeStarted: ");
        this.session = session;
    }

    @Override
    public void onMessageSendFailed(@SuppressWarnings("unused") int messageId) {
        super.onMessageSendFailed(messageId);
    }

    @Override
    public void onServiceDiscovered(PeerHandle peerHandle, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
        super.onServiceDiscovered(peerHandle,serviceSpecificInfo,matchFilter);
        logd(TAG, "onServiceDiscovered: ");
        this.setPeerHandle(peerHandle);
        session.sendMessage(peerHandle,1,"identifyrequest".getBytes());
    }


    @Override
    public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
        super.onMessageReceived(peerHandle,message);

        String messageString = new String(message);
        logd(TAG, "onMessageReceived: " + messageString);

        if(messageString.contains("identifyresponse")){
            String [] s = messageString.split(";");
            view.addDevice(s[2] , s[1], peerHandle);
        }
        else if(messageString.equals("connectionresponse")){
            setConnection(new SubscriberConnection(view.getConnectivityManager(),session, peerHandle));
            getConnection().connect(session, peerHandle);
            //view.saveFile();
            waitScrcpyServerConnect();
            session.sendMessage(peerHandle,1,"startstreaming".getBytes());
        }
        else if(messageString.contains("PORT:")){
            setPortToUse(Integer.parseInt(messageString.split(":")[1]));
        }
        else if(messageString.contains("streamingstarted")){
            view.cambiarVista();
        }
        else {
            setOtherIp(message);
        }

    }

    public void setObserver(SubscriberObserver observer){
        this.observer = observer;
    }

    public void realizaConexion(PeerHandle peerHandle){
        session.sendMessage(peerHandle,1,"connectionrequest".getBytes());
    }


    public AbstractConnection getConnection(){
        return super.getConnection();
    }

    private OutputStream nodoTransito;

    private void waitScrcpyServerConnect(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = getConnection().getServerSocket();
                    mScrcpyClientVideoSocket = serverSocket.accept();
                    mScrcpyClientControlSocket = serverSocket.accept();

                    IOEApplication.getInstance().mScrcpyClientVideoSocket = mScrcpyClientVideoSocket;
                    IOEApplication.getInstance().mScrcpyClientControlSocket = mScrcpyClientControlSocket;
                    //mainActivity.setScrcpyServerSocket(mScrcpyServerSocket);
                    //mainActivity.startScrcpyservice();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        t.start();
    }

    public void setNodoTransito(OutputStream nodoTransito){
        this.nodoTransito = nodoTransito;
    }

    public void closeSocket(){
        ServerSocket serverSocket = getConnection().getServerSocket();
        try {
            if(serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
