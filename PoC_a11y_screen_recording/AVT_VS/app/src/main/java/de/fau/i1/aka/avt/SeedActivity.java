package de.fau.i1.aka.avt;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

public class SeedActivity extends AppCompatActivity {
    public final static int REQUEST_CODE = 0xab;


    /**
     * Check if an instance of a service is running.
     *
     * @param serviceClass - the service to check
     * @return true if there is an instance of that service running
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // getRunningServices is not available as of Oreo, which is perfectly fine for our use cases
        try {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (NullPointerException ignored) {}
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);
        // check if we have the read / write extrenal storage permission. It is not required,
        // however it makes the extraction of the video from non-rooted phones easier. If not granted
        // this application will use the app-internal files directory.
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE);
        else startAccessibilitySerrings();

    }


    /**
     * Open the accessibility settings activity.
     */
    private void startAccessibilitySerrings() {
        if (!isServiceRunning(AccessibilityServiceSM.class)) {
            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(i);
        }
    }

    /**
     * If we did not have the write external storage permission it was requested from the user. If
     * the user reacted to the dialog we can savely start the srevice. The actual presence of this
     * permission is handled in the {@link de.fau.i1.aka.avt.ScreenRecord.ScreenRecordActivity}.
     * If available a public path is chosen for the videos location.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            startAccessibilitySerrings();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
