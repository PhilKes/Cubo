package philkes.cubo;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static philkes.cubo.util.Constants.DEBUG;

public class SnakeActivity extends AppCompatActivity implements SensorEventListener {
    public static boolean stopWorker=false;
    public static ReceiveThread thread;
    private static final String TAG="SNAKE";
    private static final int SOUND_TURN=0, SOUND_APPLE=1, SOUND_DEATH=2;
    private static final int MESSAGE_APPLE=1, MESSAGE_DEAD=2;

    private BluetoothSocket btSocket;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int lastTilt=-1;
    private SoundPool soundPool;
    private ArrayList<Integer> soundIDs;
    private InputStream inputStream;
    private Thread workerThread;
    private TextView txtScore;
    private int score=0;

    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainActivity=MainActivity.getInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_snake);
        if(!DEBUG) {
            btSocket=MainActivity.getInstance().getBtConnection().getBtSocket();
            try {
                inputStream=btSocket.getInputStream();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        txtScore=findViewById(R.id.txt_score);
        ImageButton btnUp=findViewById(R.id.btn_up);
        ImageButton btnDown=findViewById(R.id.btn_down);
        ImageButton btnLeft=findViewById(R.id.btn_left);
        ImageButton btnRight=findViewById(R.id.btn_right);
        btnUp.setOnClickListener(v -> sendAction('f'));
        btnDown.setOnClickListener(v -> sendAction('b'));
        btnLeft.setOnClickListener(v -> sendAction('l'));
        btnRight.setOnClickListener(v -> sendAction('r'));

        //declaring Sensor Manager and sensor type
        sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //region SOUNDS
        /** Init SoundPool, Score Sounds **/
        AudioAttributes audioAttributes=new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool=new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        getSoundIDs();

        //endregion
        // beginListenForData();
        if(!DEBUG) {
            thread=new ReceiveThread();
            thread.start();
        }
    }

    void beginListenForData() {
        final Handler handler=new Handler();
        final byte delimiter=10; //This is the ASCII code for a newline character

        stopWorker=false;
        byte[] buffer=new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()
        workerThread=new Thread(new Runnable() {
            public void run() {
                int bytes;
                String data="";
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        bytes=inputStream.read(buffer);
                        data+=new String(buffer, 0, bytes);
                        if(data.charAt(data.length() - 1)=='\\') {
                            String finalData=data;
                            handler.post(new Runnable() {
                                public void run() {
                                    Log.d(TAG, "BTReceive: " + finalData);
                                }
                            });
                        }


                    }
                    catch(IOException ex) {
                        stopWorker=true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void sendAction(char action) {
        mainActivity.getBtConnection().sendBT("SA" + action);
        Log.d(TAG, "sendAction: " + action);
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                playSound(SOUND_TURN);
            }
        }, 180);

        lastTilt=-1;
    }

    private void getSoundIDs() {
        soundIDs=new ArrayList<>();
        soundIDs.add(getResources().getIdentifier("whoosh", "raw", getPackageName()));  // Turn Sound
        soundIDs.add(getResources().getIdentifier("eat", "raw", getPackageName()));  // Apple Sound
        soundIDs.add(getResources().getIdentifier("death", "raw", getPackageName()));  // Death Sound
    }

    private void playSound(int id) {
        //TODO PRIORTIES
        int prio=1;
        if(id!=SOUND_TURN) {
            prio=2;
        }
        final int sound=soundPool.load(this, soundIDs.get(id), prio);
        int finalPrio=prio;
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPool.play(sound, 1f, 1f, finalPrio, 0, 1f);
            }
        });
    }

    /** Handle Bluetooth messages from the Arduino Cube */
    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message.what==MESSAGE_APPLE) {
                Log.d(TAG, "Apple");
                playSound(SOUND_APPLE);
                txtScore.setText("Score:\n" + (++score));
            }
            else if(message.what==MESSAGE_DEAD) {
                Log.d(TAG, "DEAD");

                //Toast.makeText(SnakeActivity.this, "DEAD", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder=new AlertDialog.Builder(SnakeActivity.this);
                builder.setMessage("Score: " + score).setTitle("Dead")
                        .setPositiveButton("Replay", (dialog, id) -> {
                            dialog.dismiss();
                            score=0;
                            txtScore.setText("Score:\n" + score);
                            mainActivity.getBtConnection().sendBT("SS");
                        })
                        .setNegativeButton("Back", (dialog, id) -> {
                            dialog.dismiss();
                            onBackPressed();
                        });
                builder.create().show();
                playSound(SOUND_DEATH);
            }
            return true;
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
        mainActivity.getBtConnection().sendBT("SS");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        mainActivity.getBtConnection().sendBT("A0");
        super.onStop();
    }

    /** Send tilt movement to the Arduino */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x=event.values[0];
        float y=event.values[1];
        // if (Math.abs(x) > Math.abs(y)) {
        if(x<-1) {
            if(lastTilt!=0) {
                if(DEBUG) {
                    Log.d(TAG, "TILT DOWN");
                }
                sendAction('d');
                lastTilt=0;
            }
        }
        if(x>7) {

            if(lastTilt!=1) {
                if(DEBUG) {
                    Log.d(TAG, "TILT UP");
                }
                sendAction('u');
                lastTilt=1;
            }
        }
      /*  } else {
            if (y < -5) {
                Log.d(TAG, "TILT UP");
            }
            if (y > 5) {
                Log.d(TAG, "TILT DOWN");
            }
        }*/
        if(x>(-1) && x<(7) /*&& y > (-2) && y < (2)*/) {
            // Log.d(TAG, "NO TILT");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /** Thread for receiving bluetooth messages from Arduino Cube */
    public class ReceiveThread extends Thread {
        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;
            String temp="";
            while(true && !stopWorker) {
                try {
                    bytes=inputStream.read(buffer);
                    temp+=new String(buffer, 0, bytes);
                    if(temp.contains("Apple\\")) {
                        handler.obtainMessage(MESSAGE_APPLE, temp).sendToTarget();
                        temp="";
                    }
                    else if(temp.contains("Dead\\")) {
                        handler.obtainMessage(MESSAGE_DEAD, temp).sendToTarget();
                        temp="";
                    }
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
