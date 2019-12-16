package philkes.cubo.util;

import android.content.Context;
import android.util.TypedValue;
import android.widget.Toast;

public class Util {
    public static Context CONTEXT;

    // fast way to call Toast
    public static void msg(String s) {
        Toast.makeText(CONTEXT, s, Toast.LENGTH_LONG).show();
    }

    /**
     * Utility function
     * Returns Pixel to DP
     */
    public static int pxToDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, CONTEXT.getResources().getDisplayMetrics());
    }
}
