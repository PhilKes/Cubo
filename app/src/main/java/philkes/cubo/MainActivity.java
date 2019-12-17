package philkes.cubo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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

import java.util.ArrayList;

import philkes.cubo.bluetooth.BTConnection;
import philkes.cubo.bluetooth.ConnectBT;
import philkes.cubo.bluetooth.DeviceList;
import philkes.cubo.util.Constants;
import philkes.cubo.util.Util;

import static philkes.cubo.util.Constants.ARDUINO_DEFAULT_SPEED;
import static philkes.cubo.util.Constants.BUTTON_SIZE;
import static philkes.cubo.util.Constants.DEBUG;
import static philkes.cubo.util.Constants.EXTRA_ADDRESS;
import static philkes.cubo.util.Constants.EXTRA_NAME;
import static philkes.cubo.util.Constants.MAX_BRIGHTNESS;
import static philkes.cubo.util.Constants.MAX_SPEED;
import static philkes.cubo.util.Util.pxToDP;

/**
 * Main Control Activity
 */
public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;

    private SeekBar brightBar, speedBar;
    private TextView brightText, speedText;
    private GridLayout grid;
    private EditText textCommand;
    private ArrayList<ImageButton> animButtons;
    private int selectedAnim=-1;

    private BTConnection btConnection;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.CONTEXT=getApplicationContext();
        String address=null;
        Intent newint=getIntent();
        address=newint.getStringExtra(EXTRA_ADDRESS); //receive the address of the bluetooth device
        setTitle(newint.getStringExtra(EXTRA_NAME));
        setContentView(R.layout.activity_main);
        //call the widgets
        textCommand=findViewById(R.id.text_command);
        brightBar=findViewById(R.id.brightBar);
        speedBar=findViewById(R.id.speedBar);
        brightText=findViewById(R.id.brightText);
        speedText=findViewById(R.id.speedText);

        btConnection=new BTConnection(this,address);
        if(!DEBUG) {
            new ConnectBT(this,btConnection).execute(); //Call the class to connect
        }
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

        //region ANIMATION GRID
        grid=findViewById(R.id.grid);
        //grid.setColumnCount(3);
        grid.setColumnCount(getWindowManager().getDefaultDisplay().getWidth() / BUTTON_SIZE);
        animButtons=new ArrayList<>();
        /** Fill Animation Button Grid*/
        for(int i=0; i<animRes.length; i++) {
            final int j=i;
            ImageButton btn=new ImageButton(this);
            btn.setId(View.generateViewId());
            LayerDrawable layerDrawable=new LayerDrawable(
                    new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_default)});
            layerDrawable.addLayer(getResources().getDrawable(animRes[i]));
            btn.setImageDrawable(layerDrawable);
            grid.addView(btn);
            animButtons.add(btn);
            GridLayout.LayoutParams params=(GridLayout.LayoutParams) btn.getLayoutParams();
            params.height=BUTTON_SIZE;
            params.width=BUTTON_SIZE;
            btn.setLayoutParams(params);
            btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setOnClickListener(view -> onAnimClicked(j));
        }
        //endregion
        ImageButton btnSnake=findViewById(R.id.btn_snake);
        btnSnake.setOnClickListener(v -> {
            Intent intent=new Intent(this, SnakeActivity.class);
            startActivity(intent);
        });
        //region BRIGHTNESS & SPEED
        brightBar.setMax(MAX_BRIGHTNESS);
        brightBar.setProgress(brightBar.getMax());
        brightText.setText(brightBar.getProgress() + "");
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
                Log.d(Constants.TAG, "NEW BRIGHTNESS :\t" + seekBar.getProgress());
                btConnection.sendBT(String.valueOf("B" + seekBar.getProgress()));
            }
        });
        speedBar.setMax(MAX_SPEED);
        speedBar.setProgress(ARDUINO_DEFAULT_SPEED);
        speedText.setText(speedBar.getProgress() + "");
        //SEND INITIAL SPEED AND BRIGHTNESS?
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<1) {
                    seekBar.setProgress(1);
                }
                speedText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Log.d(Constants.TAG, "NEW SPEED: " + seekBar.getProgress());
                btConnection.sendBT(String.valueOf("F" + seekBar.getProgress()));
            }
        });
        //endregion

        instance=this;
    }

    public static MainActivity getInstance(){
        return instance;
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
        int id=item.getItemId();

        if(id==R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        System.out.println("BACK");
        onDisconnect(null);
        //super.onBackPressed();
    }

    /**
     * OnClickListener for all Animation Buttons<br>
     * Sends Animation command
     */
    public void onAnimClicked(int anim) {
        Log.d(Constants.TAG, "onAnimClicked: " + anim);
        onResetSpeed(null);
        if(selectedAnim!=-1) {
            LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_default)
                    , getResources().getDrawable(animRes[selectedAnim])});
            animButtons.get(selectedAnim).setImageDrawable(layerDrawable);
        }
        selectedAnim=anim;
        LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.btn_anim_clicked)
                , getResources().getDrawable(animRes[selectedAnim])});
        animButtons.get(selectedAnim).setImageDrawable(layerDrawable);
        btConnection.sendBT(String.valueOf("A" + anim));
    }

    /**
     * OnClickListener for Text Button<br>
     * Opens dialog for Text input and sends it
     */
    public void onSendText(View view) {
        // AlertDialog.Builder textDialog=new AlertDialog.Builder(this);
        AlertDialog alert=new AlertDialog.Builder(this).create();
        alert.setTitle("Enter Text to scroll");
        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        EditText input=new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
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
        Button btnOK=new Button(new ContextThemeWrapper(this, R.style.ButtonBlue), null, R.style.ButtonBlue);
        //btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_blue));
        //btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_xml));
        btnOK.setText("⏎");
        btnOK.setTextSize(pxToDP(10));
        btnOK.setPadding(pxToDP(8), 0, 0, pxToDP(10));
        btnOK.setLayoutParams(new LinearLayout.LayoutParams(pxToDP(38), pxToDP(40)));
        btnOK.setOnClickListener(v -> {
            btConnection.sendBT("T" + input.getText().toString().toUpperCase());
            Log.d(Constants.TAG, "onSendText: " + input.getText().toString());
            alert.cancel();
        });
        linearLayout.addView(btnOK);
        alert.setView(linearLayout);
        alert.show();
    }

    /**
     * OnClickListener for Command Send button<br>
     * Sends String from EditText as Command
     */
    public void onSendCommand(View view) {
        hideKeyboard();
        btConnection.sendBT(String.valueOf(textCommand.getText()));
        textCommand.setText("");
    }

    /**
     * OnClickListener for Brightness SeekBar<br>
     * Resets the brightness to default and sends it
     */
    public void onResetBright(View view) {
        brightBar.setProgress(brightBar.getMax());
        brightText.setText("" + brightBar.getProgress());
        Log.d(Constants.TAG, "BRIGHTNESS: " + brightBar.getProgress());
        btConnection.sendBT(String.valueOf("B" + brightBar.getProgress()));
    }

    /**
     * OnClickListener for Speed SeekBar<br>
     * Resets the speed to default and sends it
     */
    public void onResetSpeed(View view) {
        speedBar.setProgress(ARDUINO_DEFAULT_SPEED);
        speedText.setText("" + speedBar.getProgress());
        Log.d(Constants.TAG, "NEW SPEED: " + speedBar.getProgress());
        btConnection.sendBT(String.valueOf("F" + speedBar.getProgress()));
    }

    /**
     * OnClickListener for onDisconnect Button<br>
     * Disconnects BT and returns to DeviceList
     */
    public void onDisconnect(View view) {
        btConnection.disconnect();
        finish(); //return to the first layout
    }

    private void hideKeyboard() {
        View view=this.getCurrentFocus();
        if(view!=null) {
            InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public BTConnection getBtConnection() {
        return btConnection;
    }


/*    public void testBTSend(View view) {
        try {
            InputStream inputStream=getResources().openRawResource(R.raw.cube_grow);
            //InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
            String line=reader.readLine();
            while(line!=null) {
                System.out.println(line);
                String[] vals=line.split(",");
                for(String s : vals) {
                    System.out.print(s);
                    btConnection.sendBT(s);
                }
                System.out.println();
                btConnection.sendBT("\n");
                line=reader.readLine();
            }
            reader.close();
            inputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }*/
}
