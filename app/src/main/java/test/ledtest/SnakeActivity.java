package test.ledtest;

import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class SnakeActivity extends AppCompatActivity {
    BluetoothSocket btSocket;
    char direction='u';
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);
        btSocket=MainActivity.btSocket;
        ImageButton btnUp=findViewById(R.id.btn_up);
        ImageButton btnDown=findViewById(R.id.btn_down);
        ImageButton btnLeft=findViewById(R.id.btn_left);
        ImageButton btnRight=findViewById(R.id.btn_right);
        btnUp.setOnClickListener(v->sendAction('u'));
        btnDown.setOnClickListener(v->sendAction('d'));
        btnLeft.setOnClickListener(v->sendAction('l'));
        btnRight.setOnClickListener(v->sendAction('r'));

    }
    private void sendAction(char action){
        MainActivity.sendBT("SA"+action);
    }
    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.sendBT("SS");

    }
}
