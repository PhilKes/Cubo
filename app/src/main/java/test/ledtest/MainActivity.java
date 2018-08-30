package test.ledtest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;



public class MainActivity extends AppCompatActivity {
    public static boolean DEBUG=true;

    private static final UUID myUUID = UUID.randomUUID();
    private static final int MAX_BRIGHTNESS = 4095,MAX_SPEED=100,MAX_FRAMETIME=2000,
                            BUTTON_SIZE=220;
    private static final int UNSELECTED_COLOR=Color.rgb(200,200,200),
                             SELECTED_COLOR=Color.RED;

    private Button btnDis,btnSend;
    private SeekBar brightBar,speedBar;
    private TextView brightText,speedText;
    private GridLayout grid;
    private EditText textAnim;
    private String address = null;
    private ArrayList<ImageButton> animButtons;
    private int selectedAnim=-1;
    private ProgressDialog progress;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private String[] anims=new String[]{"off","all","red","blue","green","random","stripes","cube","font4"};



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(!DEBUG) {
            Intent newint = getIntent();
            address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        }
        setContentView(R.layout.activity_main);
        //call the widgtes
        btnDis = findViewById(R.id.button4);
        btnSend= findViewById(R.id.sendBtn);
        textAnim= findViewById(R.id.animText);
        brightBar=findViewById(R.id.brightBar);
        speedBar=findViewById(R.id.speedBar);
        brightText = findViewById(R.id.brightText);
        speedText= findViewById(R.id.speedText);

        if(!DEBUG)
            new ConnectBT().execute(); //Call the class to connect

        //region ANIMATION GRID
        grid= findViewById(R.id.grid);
        //grid.setColumnCount(3);
        grid.setColumnCount(getWindowManager().getDefaultDisplay().getWidth()/BUTTON_SIZE);
        animButtons=new ArrayList<>();
        //View animButton=getLayoutInflater().inflate(R.layout.animbutton,null);
        for (int i = 0; i < anims.length; i++) {
            final int j=i;
            ImageButton btn=new ImageButton(this);
            //RadioButton btn=new RadioButton(this);
            btn.setId(View.generateViewId());
            btn.setImageDrawable(getResources().getDrawable(getResources().getIdentifier(anims[i],"drawable",MainActivity.this.getPackageName())));

            grid.addView(btn);
            animButtons.add(btn);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) btn.getLayoutParams();
            params.height=BUTTON_SIZE;
            params.width=BUTTON_SIZE;
            btn.setLayoutParams(params);
            btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            btn.setBackgroundColor(UNSELECTED_COLOR);
            btn.setOnClickListener(view->{

                animClicked(j);
            });
        }
        //endregion
        btnSend.setOnClickListener(view->
        {
            if(!DEBUG) {
                try {
                    btSocket.getOutputStream().write(String.valueOf(textAnim.getText()).getBytes());
                } catch (IOException e) {
                    Log.d("Handy", "onCreate: Erorr Anim Send");
                }
            }
        });
        btnDis.setOnClickListener(v -> {
            Disconnect(); //close connection
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
                if(DEBUG)
                    System.out.println("NEW BRIGHTNESS :\t"+seekBar.getProgress());
                if(!DEBUG) {
                    try {
                        btSocket.getOutputStream().write(String.valueOf("B"+seekBar.getProgress()).getBytes());
                    } catch (IOException e) {

                    }
                }

            }
        });
        speedBar.setMax(MAX_SPEED);
        speedBar.setProgress(speedBar.getMax()/2);
        speedText.setText(speedBar.getProgress()+"");

        //                                                                                          SEND INITIAL SPEED AND BRIGHTNESS?
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(DEBUG)
                    System.out.println("NEW SPEED :\t"+seekBar.getProgress());
                if(!DEBUG) {
                    try {
                        btSocket.getOutputStream().write(String.valueOf("S"+(MAX_FRAMETIME/(seekBar.getProgress()<1?1:seekBar.getProgress()))).getBytes());
                    } catch (IOException e) {

                    }
                }

            }
        });
        //endregion
    }

    private void animClicked(int anim)
    {
        if(DEBUG)
            System.out.println("Clicked Animation:\t"+anim);
        if(selectedAnim!=-1)
            animButtons.get(selectedAnim).setBackgroundColor(UNSELECTED_COLOR);
        selectedAnim=anim;
        animButtons.get(selectedAnim).setBackgroundColor(SELECTED_COLOR);
        if(!DEBUG) {
            try {
                btSocket.getOutputStream().write(String.valueOf("A"+anim).getBytes());
            } catch (IOException e) {
                Log.d("Android", "onCreate: Error Anim Clicked");
            }
        }
    }

    private void Disconnect()
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

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
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

    public void resetBright(View view) {
        brightBar.setProgress(brightBar.getMax());
        brightText.setText(""+brightBar.getProgress());
    }

    public void resetSpeed(View view) {
        speedBar.setProgress(speedBar.getMax()/2);
        speedText.setText(""+speedBar.getProgress());
    }

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
}
