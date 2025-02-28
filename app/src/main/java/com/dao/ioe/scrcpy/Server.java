package com.dao.ioe.scrcpy;

import com.dao.ioe.IOEApplication;
import com.dao.ioe.utils.Pair;

import java.io.IOException;

import static com.dao.ioe.utils.utils.logd;

public final class Server {

    private static String ip = null;
    private static Server mScrcpyServer;
    private String TAG = "scrcpyserver";

    private Server() {
        // not instantiable
    }

    private static void scrcpy(Options options) throws IOException {
        logd("scrcpyserver", "enter scrcpy");
        final Device device = new Device(options);
        DroidConnection connection =null;
        if(!IOEApplication.getInstance().mScrcpyServerVideoSocketOutputs.isEmpty()){
            logd("scrcpyserver", "socketOutputs is not empty.");
            for(Pair pair:IOEApplication.getInstance().mScrcpyServerVideoSocketOutputs) {
                logd("scrcpyserver", "enter DroidConnection.");
                connection = new DroidConnection(pair.socketOutput);
                break;
            }
        }

        logd("scrcpyserver", "connection 000.");
        if (connection != null) {
            logd("scrcpyserver", "connection 111.");
            ScreenEncoder screenEncoder = new ScreenEncoder(options.getBitRate());

            // asynchronous
            startEventController(device, connection);

            try {
                // synchronous
                screenEncoder.streamScreen(device, connection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                // this is expected on close
                Ln.d("Screen streaming stopped");


            }
        }
    }

    private static void startEventController(final Device device, final DroidConnection connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device, connection).control();
                } catch (IOException e) {
                    // this is expected on close
                    Ln.d("Event controller stopped");
                }
            }
        }).start();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Options createOptions(String... args) {
        Options options = new Options();

//        if (args.length < 1) {
//            return options;
//        }
//        ip = String.valueOf(args[0]);

        logd("scrcpyserver", "args.length is: " + args.length);
        if (args.length < 1) {
            return options;
        }
        int maxSize = Integer.parseInt(args[0]) & ~7; // multiple of 8
        options.setMaxSize(maxSize);

        if (args.length < 2) {
            return options;
        }
        int bitRate = Integer.parseInt(args[1]);
        options.setBitRate(bitRate);

        if (args.length < 3) {
            return options;
        }
        // use "adb forward" instead of "adb tunnel"? (so the server must listen)
        boolean tunnelForward = Boolean.parseBoolean(args[2]);
        options.setTunnelForward(tunnelForward);

        return options;
    }

    public static synchronized Server getInstance() {
        if (mScrcpyServer == null) {
            mScrcpyServer = new Server();
        }
        return mScrcpyServer;
    }

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Ln.e("Exception on thread " + t, e);
            }
        });

//        try {
//            Process cmd = Runtime.getRuntime().exec("rm /data/local/tmp/scrcpy-server.jar");
//            cmd.waitFor();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        }

        logd("scrcpyserver", "enter main: ");
        Options options = createOptions(args);
        scrcpy(options);
    }
}

