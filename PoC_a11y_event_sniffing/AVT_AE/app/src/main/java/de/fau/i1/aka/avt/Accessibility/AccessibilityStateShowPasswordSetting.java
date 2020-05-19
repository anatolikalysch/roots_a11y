package de.fau.i1.aka.avt.Accessibility;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 26.11.17.
 */

public class AccessibilityStateShowPasswordSetting extends AccessibilityState {
    public AccessibilityStateShowPasswordSetting(Context context) {
        super(context);
        context.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    @Override
    protected void windowContentChanged(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        List<AccessibilityNodeInfo> appName = source.findAccessibilityNodeInfosByText("show password");
        if (appName.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : appName) {
                AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                if (parent != null) {
                    parent = parent.getParent();
                    if (parent != null && parent.isClickable()) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        dispatchHomeScreenIntent();
                        rebuildEventStateHandler();
                    } else {
                        if (parent != null)
                            for (int y = 0; y < parent.getChildCount(); y++) {
                                AccessibilityNodeInfo child = parent.getChild(y);
                                if (child != null && child.isClickable()) {
                                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                    }
                }
            }
        } else {
            try {
                source.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                for (int i = 0; i < source.getChildCount(); i++) {
                    source.getChild(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }

            } catch (Exception ignored) {}
        }
    }





    @Override
    protected AccessibilityStateEnum getNextState() {
        return AccessibilityStateEnum.WATCHER;
    }
}
