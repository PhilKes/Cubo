package philkes.cubo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import philkes.cubo.MainActivity;
import philkes.cubo.R;

import static philkes.cubo.util.Constants.DEBUG;


/**
 * Starting Activity for connection to LED Cube BT Module
 */
public class DeviceList extends AppCompatActivity {

    public static String EXTRA_ADDRESS="device_address";
    //widgets
    private Button btnPaired;
    private ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth=null;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        //Calling widgets
        btnPaired=(Button) findViewById(R.id.button);
        devicelist=(ListView) findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth=BluetoothAdapter.getDefaultAdapter();

        if(!DEBUG) {
            if(myBluetooth==null) {
                //Show a msg. that the device has no bluetooth adapter
                Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
                //finish apk
                finish();
            }
            else if(!myBluetooth.isEnabled()) {
                //Ask to the user turn the bluetooth on
                Intent turnBTon=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }
        btnPaired.setOnClickListener(v -> pairedDevicesList());
    }

    /**
     * OnClickListener for Search Button
     * Shows all paired Devices in ListView
     */
    private void pairedDevicesList() {
        if(DEBUG) {
            goToMainActivity(btnPaired);
            return;
        }
        pairedDevices=myBluetooth.getBondedDevices();
        ArrayList list=new ArrayList();
        if(pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices)
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
        }
        else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter=new ArrayAdapter(this, R.layout.device_list_item, list);
        //devicelist.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener((av, v, arg2, arg3) -> goToMainActivity(v)); //Method called when the device from the list is clicked

    }

    /**
     * OnClickListener for DeviceList items
     * Starts MainActivity with selected Device
     */
    private void goToMainActivity(View v) {
        String address="";
        if(!DEBUG) {
            // Get the device MAC address, the last 17 chars in the View
            String info=((TextView) v).getText().toString();
            address=info.substring(info.length() - 17);
        }
        Intent i=new Intent(DeviceList.this, MainActivity.class);
        if(!DEBUG) {
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
        }
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        MenuItem itemDebug=menu.findItem(R.id.action_debug);
        itemDebug.setChecked(DEBUG);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_settings) {
            return true;
        }
        //TODO For Debugging on real phone
        /*if(id==R.id.action_debug) {
            item.setChecked(!item.isChecked());
            DEBUG=item.isChecked();
        }*/
        return super.onOptionsItemSelected(item);
    }
}
