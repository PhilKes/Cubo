package butterknife.functional;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.TypedValue;
import butterknife.BindFloat;
import butterknife.BindInt;
import butterknife.Unbinder;
import butterknife.test.R;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class BindFloatTest {
  private final Context context = InstrumentationRegistry.getContext();

  static class Target {
    @BindFloat(R.dimen.twelve_point_two) float actual;
  }

  @Test public void asFloat() {
    Target target = new Target();
    TypedValue value = new TypedValue();
    context.getResources().getValue(R.dimen.twelve_point_two, value, true);
    float expected = value.getFloat();

    Unbinder unbinder = new BindFloatTest$Target_ViewBinding(target, context);
    assertThat(target.actual).isEqualTo(expected);

    unbinder.unbind();
    assertThat(target.actual).isEqualTo(expected);
  }
}
