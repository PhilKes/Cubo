package philkes.cubo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import philkes.cubo.MainActivity;
import philkes.cubo.R;

import static philkes.cubo.util.Constants.DEBUG;
import static philkes.cubo.util.Constants.EXTRA_ADDRESS;
import static philkes.cubo.util.Constants.EXTRA_NAME;


/**
 * Starting Activity for connection to LED Cube BT Module
 */
public class DeviceList extends AppCompatActivity {

    //widgets
    private Button btnPaired;
    private ListView devicelist;
    //Bluetooth
    private BluetoothAdapter bluetoothAdapter=null;
    private Set<BluetoothDevice> pairedDevices;

    private ArrayList<String> list;
    private DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        //Calling widgets
        btnPaired=(Button) findViewById(R.id.button);
        devicelist=(ListView) findViewById(R.id.listView);

        btnPaired.setOnClickListener(v -> pairedDevicesList());
        list=new ArrayList<>();
        deviceAdapter=new DeviceAdapter(this, list);
        devicelist.setAdapter(deviceAdapter);
        devicelist.setOnItemClickListener((av, v, arg2, arg3) -> goToMainActivity(v)); //Method called when the device from the list is clicked
        //new Thread(()->checkBTAdapter()).start();
        checkBTAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void checkBTAdapter(){
        if(bluetoothAdapter==null) {
            bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
            if(!DEBUG) {
                if(bluetoothAdapter==null) {
                    //Show a msg. that the device has no bluetooth deviceAdapter
                    Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
                    //finish apk
                    finish();
                }
                else if(!bluetoothAdapter.isEnabled()) {
                    //Ask to the user turn the bluetooth on
                    Intent turnBTon=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnBTon, 1);
                }
            }
        }
    }
    /**
     * OnClickListener for Search Button
     * Shows all paired Devices in ListView
     */
    private void pairedDevicesList() {
        list.clear();
        if(DEBUG) {
            list.add("Debug test device 1\nAB:CD:EF:00:01:02");
            list.add("LED Cube\n2F:56:DE:F4:45:D3");
        }
        else {
            pairedDevices=bluetoothAdapter.getBondedDevices();
            if(pairedDevices.size()>0) {
                for(BluetoothDevice bt : pairedDevices)
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
            else {
                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            }
        }
        deviceAdapter.notifyDataSetChanged();
    }

    /**
     * OnClickListener for DeviceList items
     * Starts MainActivity with selected Device
     */
    private void goToMainActivity(View v) {
        String address="";
        // Get the device MAC address, the last 17 chars in the View
        String info=((TextView) ((ConstraintLayout) v).findViewById(R.id.txtDevice)).getText().toString();
        address=info.substring(info.length() - 17);
        Intent i=new Intent(DeviceList.this, MainActivity.class);
        /** Send Device Address and Name to MainActivity*/
        i.putExtra(EXTRA_ADDRESS, address);
        i.putExtra(EXTRA_NAME, info.substring(0, info.length() - 18));
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
