package interdroid.swan.crossdevice.swanplus;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by vladimir on 12/1/15.
 */
public class WDReceiver extends AsyncTask<Void, String, Void> {

    private static final String TAG = "WDReceiver";
    private WDManager wdManager;
    private final static int PORT = 2222;

    public WDReceiver(WDManager wdManager) {
        this.wdManager = wdManager;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (true) {
                receive();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void receive() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(PORT);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            byte[] data = receivePacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);

            HashMap<String, String> dataMap = (HashMap<String, String>) is.readObject();
            String action = dataMap.get("action");
            Log.d(TAG, "received " + action);

            if(action.equals("initConnect")) {
                InetAddress remoteIp = receivePacket.getAddress();
                wdManager.connected(remoteIp.getHostAddress(), false);
            }

            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
