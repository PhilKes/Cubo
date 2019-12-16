package philkes.cubo.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;

import philkes.cubo.MainActivity;
import philkes.cubo.util.Util;

import static philkes.cubo.util.Constants.myUUID;

/**
 * AsyncTask for connecting to BT device
 */
public class ConnectBT extends AsyncTask<Void, Void, Void> {
    private boolean connectSuccess; //if it's here, it's almost connected
    private MainActivity mainActivity;
    private BTConnection btConnection;
    private ProgressDialog progress;

    public ConnectBT(MainActivity mainActivity, BTConnection btConnection) {
        this.mainActivity=mainActivity;
        this.btConnection=btConnection;
        this.connectSuccess=true;
    }

    @Override
    protected void onPreExecute() {
        progress=ProgressDialog.show(mainActivity, "Connecting...", "Please wait!!!");  //show a progress dialog
    }

    //while the progress dialog is shown, the connection is done in background
    @Override
    protected Void doInBackground(Void... devices) {
        try {
            if(btConnection.getBtSocket()==null || !btConnection.isConnected()) {
                btConnection.setBtAdapter(BluetoothAdapter.getDefaultAdapter());//get the mobile bluetooth device
                BluetoothDevice dispositivo=btConnection.getDevice();//connects to the device's address and checks if it's available
                btConnection.setBtSocket(dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID));//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btConnection.getBtSocket().connect();//start connection
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            connectSuccess=false;//if the try failed, you can check the exception here
        }
        return null;
    }

    //after the doInBackground, it checks if everything went fine
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(!connectSuccess) {
            Util.msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            mainActivity.finish();
        }
        else {
            Util.msg("Connected.");
            btConnection.setConnected(true);
        }
        progress.dismiss();
    }
}
