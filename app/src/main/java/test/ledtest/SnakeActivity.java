package test.ledtest;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import static test.ledtest.MainActivity.DEBUG;

public class SnakeActivity extends AppCompatActivity  implements SensorEventListener {
    private static final String TAG = "SNAKE";
    BluetoothSocket btSocket;
    char direction='u';
    private SensorManager sensorManager;
    private Sensor sensor;
    private int lastTilt=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_snake);
        btSocket=MainActivity.btSocket;
        ImageButton btnUp=findViewById(R.id.btn_up);
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

    }
    private void sendAction(char action){
        if(!DEBUG)
            MainActivity.sendBT("SA"+action);
        else
            Log.d(TAG, "sendAction: "+action);
        lastTilt=-1;
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
