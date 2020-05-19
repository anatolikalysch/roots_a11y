package de.fau.i1.aka.avt.Accessibility;

import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 12.11.17.
 */

public class AccessibilityStateManager {
    private static final AccessibilityStateManager ourInstance = new AccessibilityStateManager();

    public static AccessibilityStateManager getInstance() {
        return ourInstance;
    }

    private AccessibilityStateManager() {
    }

    public AccessibilityState rebuildHandler(AccessibilityStateEnum wantedState, AccessibilityServiceSM context) {
        if (wantedState != null)
            switch (wantedState) {
                case WATCHER:
                    return new AccessibilityStateWatcher(context);
                case PASSWORD_SETTING:
                    return new AccessibilityStateShowPasswordSetting(context);
                default:
                    return new AccessibilityStateWatcher(context);
            }
        else return new AccessibilityStateWatcher(context);
    }
}
