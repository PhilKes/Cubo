package test.ledtest;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static test.ledtest.MainActivity.DEBUG;

public class SnakeActivity extends AppCompatActivity  implements SensorEventListener {
    private static final String TAG = "SNAKE";
    private static final int SOUND_TURN=0;

    BluetoothSocket btSocket;
    char direction='u';
    private SensorManager sensorManager;
    private Sensor sensor;
    private int lastTilt=-1;
    private View btn;
    private SoundPool soundPool;
    private ArrayList<Integer> soundIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_snake);
        btSocket=MainActivity.btSocket;

        ImageButton btnUp=findViewById(R.id.btn_up);
        btn=btnUp;
        ImageButton btnDown=findViewById(R.id.btn_down);
        ImageButton btnLeft=findViewById(R.id.btn_left);
        ImageButton btnRight=findViewById(R.id.btn_right);
        btnUp.setOnClickListener(v->sendAction('f'));
        btnDown.setOnClickListener(v->sendAction('b'));
        btnLeft.setOnClickListener(v->sendAction('l'));
        btnRight.setOnClickListener(v->sendAction('r'));

        //declaring Sensor Manager and sensor type
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //region SOUNDS
        /** Init SoundPool, Score Sounds **/
        AudioAttributes audioAttributes=new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool=new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        getSoundIDs();
        //endregion

    }
    private void sendAction(char action){
        if(!DEBUG)
            MainActivity.sendBT("SA"+action);
        else
            Log.d(TAG, "sendAction: "+action);

        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
       new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                  playSound(SOUND_TURN);
            }
        }, 300);

        lastTilt=-1;
    }
    private void getSoundIDs() {
        soundIDs=new ArrayList<>();
        soundIDs.add(getResources().getIdentifier("whoosh", "raw", getPackageName()));  // Turn Sound
    }
    private void playSound(int id) {
        final int sound=soundPool.load(this,soundIDs.get(id),1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPool.play(sound,1f,1f,1,0,1f);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!DEBUG)
            MainActivity.sendBT("SS");

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
        if(!DEBUG)
            MainActivity.sendBT("A0");
        super.onStop();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
       // if (Math.abs(x) > Math.abs(y)) {
            if (x < -1) {
                if(lastTilt!=0) {
                    Log.d(TAG, "TILT DOWN");
                    sendAction('d');
                    lastTilt=0;
                }
            }
            if (x > 7) {

                if(lastTilt!=1) {
                    Log.d(TAG, "TILT UP");
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
        if (x > (-1) && x < (7) /*&& y > (-2) && y < (2)*/) {
            Log.d(TAG, "NO TILT");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
