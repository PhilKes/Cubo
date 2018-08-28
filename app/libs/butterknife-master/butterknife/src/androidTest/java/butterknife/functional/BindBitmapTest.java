package butterknife.functional;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import butterknife.BindBitmap;
import butterknife.BindBool;
import butterknife.Unbinder;
import butterknife.test.R;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

public final class BindBitmapTest {
  private final Context context = InstrumentationRegistry.getContext();

  static class Target {
    @BindBitmap(R.drawable.pixel) Bitmap actual;
  }

  @Test public void asBitmap() {
    Target target = new Target();
    Bitmap expected = BitmapFactory.decodeResource(context.getResources(), R.drawable.pixel);

    Unbinder unbinder = new BindBitmapTest$Target_ViewBinding(target, context);
    assertTrue(target.actual.sameAs(expected));

    unbinder.unbind();
    assertTrue(target.actual.sameAs(expected));
  }
}
