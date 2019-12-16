package philkes.cubo.util;

import android.widget.SeekBar;

public class BarBinding {
    private SeekBar bar;

    public static BarBinding get(SeekBar seekBar) {
        return new BarBinding(seekBar);
    }

    public BarBinding(SeekBar seekBar) {
        this.bar=seekBar;
    }

    public String getProgress() {
        return "" + bar.getProgress();
    }
}
