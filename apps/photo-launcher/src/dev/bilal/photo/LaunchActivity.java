package dev.bilal.photo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/** Home-screen icon for the unit's hidden stock camera app. */
public class LaunchActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            startActivity(new Intent(Intent.ACTION_MAIN)
                .setComponent(new ComponentName("com.android.camera2", "com.android.camera.CameraActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Toast.makeText(this, "Camera app not available", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
