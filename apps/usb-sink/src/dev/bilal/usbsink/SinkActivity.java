package dev.bilal.usbsink;

import android.app.Activity;
import android.os.Bundle;

/** Claims the built-in USB camera's attach event and does nothing, so no other app is asked to handle it. */
public class SinkActivity extends Activity {
    @Override protected void onCreate(Bundle b) { super.onCreate(b); finish(); }
}
