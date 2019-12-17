package philkes.cubo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;

import philkes.cubo.MainActivity;
import philkes.cubo.SnakeActivity;
import philkes.cubo.util.Constants;
import philkes.cubo.util.Util;

import static philkes.cubo.util.Constants.DEBUG;

public class BTConnection {

    private BluetoothSocket btSocket=null;
    private BluetoothAdapter btAdapter=null;
    private BluetoothLostReceiver bluetoothLostReceiver;
    private boolean isConnected=false;
    private MainActivity mainActivity;
    private String address;

    public BTConnection(MainActivity mainActivity, String address) {
        this.mainActivity=mainActivity;
        this.address=address;
        if(bluetoothLostReceiver==null) {
            bluetoothLostReceiver=new BluetoothLostReceiver();
            bluetoothLostReceiver.setMainActivity(mainActivity);
            IntentFilter filter=new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
            if(!DEBUG) {
                mainActivity.registerReceiver(bluetoothLostReceiver, filter);
            }
        }
    }

    /**
     * Writes String to the btSocket Stream, adds '\' end char to command
     */
    public void sendBT(String msg) {
        if(!DEBUG) {
            try {
                btSocket.getOutputStream().write((msg + "\\").getBytes());
                Log.d(Constants.TAG, "sendBT: " + msg);
            }
            catch(IOException e) {
                Log.d(Constants.TAG, ": Error sendBT");
            }
        }
    }

    public void disconnect() {
        if(btSocket!=null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            }
            catch(IOException e) {
                Util.msg("Error");
            }
        }
    }

    public BluetoothDevice getDevice() {
        return getBtAdapter().getRemoteDevice(address);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected=connected;
    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket=btSocket;
    }

    public BluetoothAdapter getBtAdapter() {
        return btAdapter;
    }

    public void setBtAdapter(BluetoothAdapter btAdapter) {
        this.btAdapter=btAdapter;
    }

    public class BluetoothLostReceiver extends BroadcastReceiver {

        MainActivity main=null;
        public void setMainActivity(MainActivity main) {
            this.main=main;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
                Log.d(Constants.TAG, "onReceive: Lost BT Connection!");
                if(SnakeActivity.thread!=null) {
                    SnakeActivity.stopWorker=true;
                }
                BTConnection.this.disconnect();
                main.onBackPressed();
            }
        }
    }
}
