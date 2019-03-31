package test.ledtest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Main Control Activity
 */
public class MainActivity extends AppCompatActivity
{
    /**
     * DEBUG Flag (Disable BT)
     */
    public static boolean DEBUG=false;
//TODO LOOSE BT CONNECTION
   // private static final UUID myUUID = UUID.randomUUID();
   private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final int ARDUINO_DEFAUL_SPEED=50;
    private static final int MAX_BRIGHTNESS = 255,MAX_SPEED=200,MAX_FRAMETIME=2000,
                            BUTTON_SIZE=220;
    private static final int UNSELECTED_COLOR=Color.rgb(200,200,200),
                             SELECTED_COLOR=Color.RED;

    /**
     * Android
     */
    public static final String TAG="Android";
    private SeekBar brightBar,speedBar;
    private TextView brightText,speedText;
    private GridLayout grid;
    private EditText textCommand;
    private String address = null;
    //CHANGE BACK FOR IMAGE BUTTONS
    private ArrayList<ImageButton> animButtons;
    //private ArrayList<Button> animButtons;
    private int selectedAnim=-1;
    private ProgressDialog progress;
    private BluetoothAdapter btAdapter = null;
    public static BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //private String[] anims=new String[]{"rand","red","green","blue","anim","randfull","text","music","rgb","all","off"};
    private int[] animRes=new int[]{
            R.drawable.cube,
            R.drawable.red,
            R.drawable.green,
            R.drawable.blue,
            R.drawable.rain,
            R.drawable.randfull,
            R.drawable.all,
            R.drawable.music,
            R.drawable.waterfall,
            R.drawable.waves,
            R.drawable.cubemove,
    };
    private SoundPool soundPool;
    private ArrayList<Integer> soundIDs;
    private BluetoothLostReceiver bluetoothLostReceiver;
    //{"off","all","red","blue","green","random","stripes","cube","font4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!DEBUG) {
            Intent newint = getIntent();
            address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        }
        setContentView(R.layout.activity_main);
        //call the widgtes
        textCommand= findViewById(R.id.text_command);
        brightBar=findViewById(R.id.brightBar);
        speedBar=findViewById(R.id.speedBar);
        brightText = findViewById(R.id.brightText);
        speedText= findViewById(R.id.speedText);

        if(!DEBUG)
            new ConnectBT().execute(); //Call the class to connect
        Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();

        //region ANIMATION GRID
        grid= findViewById(R.id.grid);
        //grid.setColumnCount(3);
        grid.setColumnCount(getWindowManager().getDefaultDisplay().getWidth()/BUTTON_SIZE);
        animButtons=new ArrayList<>();
        //View animButton=getLayoutInflater().inflate(R.layout.animbutton,null);
        for (int i = 0; i < animRes.length; i++) {
            final int j=i;
            //CHANGE BACK FOR IMAGE BUTTONS

            ImageButton btn=new ImageButton(this);
            //RadioButton btn=new RadioButton(this);
            btn.setId(View.generateViewId());

            LayerDrawable layerDrawable=new LayerDrawable(
                    new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_default)});
            layerDrawable.addLayer(getResources().getDrawable(animRes[i]));
            btn.setImageDrawable(layerDrawable);
            grid.addView(btn);
            animButtons.add(btn);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) btn.getLayoutParams();
            params.height=BUTTON_SIZE;
            params.width=BUTTON_SIZE;
            btn.setLayoutParams(params);
            //CHANGE BACK FOR IMAGE BUTTONS
            btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setOnClickListener(view->{
                animClicked(j);
            });
        }
        //endregion

        ImageButton btnSnake=findViewById(R.id.btn_snake);

        btnSnake.setOnClickListener(v->{
            Intent intent=new Intent(this,SnakeActivity.class);
            startActivity(intent);
        });
        //region BRIGHTNESS & SPEED
        brightBar.setMax(MAX_BRIGHTNESS);
        brightBar.setProgress(brightBar.getMax());
        brightText.setText(brightBar.getProgress()+"");
        brightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "NEW BRIGHTNESS :\t"+seekBar.getProgress());
                if(!DEBUG)
                        sendBT(String.valueOf("B"+seekBar.getProgress()));

            }
        });
        speedBar.setMax(MAX_SPEED);
        speedBar.setProgress(ARDUINO_DEFAUL_SPEED);
        speedText.setText(speedBar.getProgress()+"");
        //                                                                                          SEND INITIAL SPEED AND BRIGHTNESS?
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<1)
                    seekBar.setProgress(1);
                speedText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Log.d(TAG, "NEW SPEED: "+seekBar.getProgress());
                if(!DEBUG)
                    sendBT(String.valueOf("F"+seekBar.getProgress()));
            }
        });
        //endregion
        if (bluetoothLostReceiver == null)
        {
            bluetoothLostReceiver = new BluetoothLostReceiver();
            bluetoothLostReceiver.setMainActivity(this);
            IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
            registerReceiver(bluetoothLostReceiver, filter);
        }

    }

    @Override
    protected void onDestroy() {
       // unregisterReceiver(bluetoothLostReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed()
    {
        System.out.println("BACK");
        Disconnect(null);
        //super.onBackPressed();
    }

    /**
     * Writes String to the btSocket Stream, adds '\' end char to command
     * @param msg String message to send
     *
     */
    public static void sendBT(String msg)
    {
        try {
            btSocket.getOutputStream().write((msg+"\\").getBytes());
            Log.d(TAG, "sendBT: "+msg);
        } catch (IOException e) {
            Log.d(TAG, ": Error sendBT");
        }
    }

    /**
     * OnClickListener for all Animation Buttons<br>
     * Sends Animation command
     * @param anim Clicked Animation number
     *
     */
    public void animClicked(int anim)
    {
        Log.d(TAG, "animClicked: "+anim);
        resetSpeed(null);
        if(selectedAnim!=-1)
        {
            LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_default)
                    ,getResources().getDrawable(animRes[selectedAnim])});
            animButtons.get(selectedAnim).setImageDrawable(layerDrawable);
        }
        selectedAnim=anim;
        LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_clicked)
                ,getResources().getDrawable(animRes[selectedAnim])});
        animButtons.get(selectedAnim).setImageDrawable(layerDrawable);
        if(!DEBUG)
                sendBT(String.valueOf("A"+anim));
    }

    /**
     * OnClickListener for Text Button<br>
     * Opens dialog for Text input and sends it
     * @param view
     */
    public void sendText(View view)
    {
        // AlertDialog.Builder textDialog=new AlertDialog.Builder(this);
        AlertDialog alert= new AlertDialog.Builder(this).create();
        alert.setTitle("Enter Text to scroll");
        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        EditText input=new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setScroller(new Scroller(this));
        input.setVerticalScrollBarEnabled(true);
        input.setMaxLines(1);
        input.setMaxEms(10);
        input.setMovementMethod(new ScrollingMovementMethod());
        input.setHint(R.string.text_hint);
        input.setEms(10);
        input.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        linearLayout.addView(input);
        Button btnOK=new Button(new ContextThemeWrapper(this, R.style.ButtonBlue),null,R.style.ButtonBlue);
        //btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_blue));
        //btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_xml));
        btnOK.setText("⏎");
        btnOK.setTextSize(pxToDP(10));
        btnOK.setPadding(pxToDP(8),0,0,pxToDP(10));
        btnOK.setLayoutParams(new LinearLayout.LayoutParams(pxToDP(38),pxToDP(40)));
        btnOK.setOnClickListener(v->
        {
            if(!DEBUG)
                sendBT("T"+input.getText().toString().toUpperCase());
            Log.d(TAG, "sendText: "+input.getText().toString());
            alert.cancel();
        });
        linearLayout.addView(btnOK);
        alert.setView(linearLayout);
        alert.show();
    }

    /**
     * OnClickListener for Command Send button<br>
     * Sends String from EditText as Command
     * @param view
     */
    public void sendCommand(View view)
    {
        hideKeyboard();
        if(!DEBUG)
            sendBT(String.valueOf(textCommand.getText()));
        textCommand.setText("");
    }

    /**
     * OnClickListener for Brightness SeekBar<br>
     * Resets the brightness to default and sends it
     * @param view
     */
    public void resetBright(View view) {
        brightBar.setProgress(brightBar.getMax());
        brightText.setText(""+brightBar.getProgress());
        Log.d(TAG, "BRIGHTNESS: "+brightBar.getProgress());
        if(!DEBUG)
            sendBT(String.valueOf("B"+brightBar.getProgress()));
    }
    /**
     * OnClickListener for Speed SeekBar<br>
     * Resets the speed to default and sends it
     * @param view
     *
     */
    public void resetSpeed(View view) {
        speedBar.setProgress(ARDUINO_DEFAUL_SPEED);
        speedText.setText(""+speedBar.getProgress());
        Log.d(TAG, "NEW SPEED: "+speedBar.getProgress());
        if(!DEBUG)
            sendBT(String.valueOf("F"+speedBar.getProgress()));
    }

    /**
     * OnClickListener for Disconnect Button<br>
     * Disconnects BT and returns to DeviceList
     * @param view
     */
    public void Disconnect(View view)
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }
    private void hideKeyboard()
    {
        View view= this.getCurrentFocus();
        if(view!=null)
        {
            InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    /**
     * Utility function
     * Returns Pixel to DP
     * @param px Pixel value
     * @return DP Value
     */
    public int pxToDP(int px)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
    // fast way to call Toast
    public void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public void testBTSend(View view) {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.cube_grow);
            //InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                String[] vals=line.split(",");
                for (String s : vals) {
                    System.out.print(s);
                    sendBT(s);
                }
                System.out.println();
                sendBT("\n");
                line = reader.readLine();
            }
            reader.close();
            inputStream.close();
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * AsyncTask for connecting to BT device
     */
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    public class BluetoothLostReceiver extends BroadcastReceiver {

        MainActivity main = null;

        public void setMainActivity(MainActivity main)
        {
            this.main = main;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()))
            {
                Log.d(TAG, "onReceive: Lost BT Connection!");
                if(SnakeActivity.thread!=null)
                    SnakeActivity.stopWorker=true;
                onBackPressed();
            }
        }
    }
}
