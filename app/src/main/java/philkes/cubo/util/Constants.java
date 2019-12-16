package philkes.cubo.util;

import android.graphics.Color;

import java.util.UUID;

public class Constants {
    /** Disables Bluetooth for debugging*/
    public final static boolean DEBUG=true;

    public static String TAG="Android";
    public static final int ARDUINO_DEFAULT_SPEED=50;
    public static final int MAX_BRIGHTNESS=255;
    public static final int MAX_SPEED=200;
    public static final int MAX_FRAMETIME=2000;
    public static final int BUTTON_SIZE=220;
    public static final int UNSELECTED_COLOR=Color.rgb(200, 200, 200),
            SELECTED_COLOR=Color.RED;
    public static final UUID myUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
