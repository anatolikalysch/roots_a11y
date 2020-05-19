package de.fau.i1.aka.avt.Accessibility;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Collections;
import java.util.List;

import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

import static android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT;
import static android.view.accessibility.AccessibilityEvent.TYPE_GESTURE_DETECTION_END;
import static android.view.accessibility.AccessibilityEvent.TYPE_GESTURE_DETECTION_START;
import static android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END;
import static android.view.accessibility.AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START;
import static android.view.accessibility.AccessibilityEvent.TYPE_TOUCH_INTERACTION_END;
import static android.view.accessibility.AccessibilityEvent.TYPE_TOUCH_INTERACTION_START;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_HOVER_ENTER;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_HOVER_EXIT;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_LONG_CLICKED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SELECTED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 12.11.17.
 */

public abstract class AccessibilityState {

    protected AccessibilityServiceSM context;
    private static final String TAG = "ACCEVENT";

    public AccessibilityState (Context context) {
        this.context = (AccessibilityServiceSM) context;
    }

    public void handleEvent(AccessibilityEvent event) throws NullPointerException {
        if (AccessibilityServiceSM.debug) {
            Log.v(TAG, String.valueOf(event.getEventType()));
            Log.v(TAG, String.valueOf(event.getPackageName()));
            Log.v(TAG, String.valueOf(event.getText()));
            Log.v(TAG, String.valueOf(event.toString()));
        }

        event = hookPreprocessEvent(event);

        switch (event.getEventType()) {
            case TYPE_ANNOUNCEMENT:
                announcement(event);
                break;
//            case TYPE_ASSIST_READING_CONTEXT:
//                assistReadingContext(event);
//                break;
            case TYPE_GESTURE_DETECTION_END:
                gestureDetectionEnd(event);
                break;
            case TYPE_GESTURE_DETECTION_START:
                gestureDetectionStart(event);
                break;
            case TYPE_NOTIFICATION_STATE_CHANGED:
                notificationStateChanged(event);
                break;
            case TYPE_TOUCH_EXPLORATION_GESTURE_END:
                touchExplorationGestureEnd(event);
                break;
            case TYPE_TOUCH_EXPLORATION_GESTURE_START:
                touchExplorationGestureStart(event);
                break;
            case TYPE_TOUCH_INTERACTION_END:
                touchInteractionEnd(event);
                break;
            case TYPE_TOUCH_INTERACTION_START:
                touchInteractionStart(event);
                break;
            case TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                viewAccessibilityFocusCleared(event);
                break;
            case TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                viewAccessibilityFocused(event);
                break;
            case TYPE_VIEW_CLICKED:
                viewClicked(event);
                break;
//            case TYPE_VIEW_CONTEXT_CLICKED:
//                viewContextClicked(event);
//                break;
            case TYPE_VIEW_FOCUSED:
                viewFocused(event);
                break;
            case TYPE_VIEW_HOVER_ENTER:
                viewHoverEnter(event);
                break;
            case TYPE_VIEW_HOVER_EXIT:
                viewHoverExit(event);
                break;
            case TYPE_VIEW_LONG_CLICKED:
                viewLongClicked(event);
                break;
            case TYPE_VIEW_SCROLLED:
                viewScrolled(event);
                break;
            case TYPE_VIEW_SELECTED:
                viewSelected(event);
                break;
            case TYPE_VIEW_TEXT_CHANGED:
                viewTextChanged(event);
                break;
            case TYPE_VIEW_TEXT_SELECTION_CHANGED:
                viewTextSelectionChanged(event);
                break;
            case TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                viewTextTraversedAtMovementGranularity(event);
                break;
            case TYPE_WINDOW_CONTENT_CHANGED:
                windowContentChanged(event);
                break;
            case TYPE_WINDOW_STATE_CHANGED:
                windowStateChanged(event);
                break;
            case TYPE_WINDOWS_CHANGED:
                windowsChanged(event);
                break;
        }
    }

    protected AccessibilityEvent hookPreprocessEvent(AccessibilityEvent event) {
        return event;
    }

    protected boolean focusEditText(AccessibilityNodeInfo source, String editTextContent) {
        List<AccessibilityNodeInfo> editTextList = source.findAccessibilityNodeInfosByText(editTextContent);
        if (editTextList.size() > 0) {
            for (AccessibilityNodeInfo eT : editTextList)
                return performFocus(eT);
        }
        return false;
    }

    private boolean performFocus(AccessibilityNodeInfo eT) {
        if (eT.isFocusable()) {
            eT.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return true;
        }
        return false;
    }

    protected boolean clickButton(AccessibilityNodeInfo source, String buttonText) {
        List<AccessibilityNodeInfo> ButtonNodesList = source.findAccessibilityNodeInfosByText(buttonText);
        if (ButtonNodesList.size() > 0) {
            for (AccessibilityNodeInfo button : ButtonNodesList)
                return performClick(button);
        }
        return false;
    }

    protected boolean performClick(AccessibilityNodeInfo button) {
        if (button.isClickable()) {
            button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            button.recycle();
            return true;
        } else {
            try {
                return performClick(button.getParent());
            } catch (NullPointerException ignored) {}
        }
        return false;
    }

    protected boolean fillInPassword(AccessibilityNodeInfo source, String password) {
        AccessibilityNodeInfo field = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (field != null) {
            // find and fill in the Password field
            if (field.isPassword()) {
                fillIn(field, password);
                return true;
            }
        } else { // check the children
            try {
                boolean filledIn = false;
                for (int i = 0; i < source.getChildCount(); i++) {
                    if(fillInPassword(source.getChild(i), password)) filledIn = true;
                }
                return filledIn;
            } catch (NullPointerException ignored) {}
        }
        return false;
    }

    protected boolean fillInText(AccessibilityNodeInfo source, String text) {
        AccessibilityNodeInfo field = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (field != null) {
            // find and fill in the EditText field
            if (field.isEditable()) {
                fillIn(field, text);
                return true;
            }
        } else { // check the children
            try {
                boolean filledIn = false;
                for (int i = 0; i < source.getChildCount(); i++) {
                    if (fillInText(source.getChild(i), text)) filledIn = true;
                }
                return filledIn;
            } catch (NullPointerException ignored) {}
        }
        return false;
    }

    protected void fillIn(AccessibilityNodeInfo field, String string) {
        Bundle arguments = new Bundle();
        arguments.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, string);
        field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        //field.recycle();
    }

    protected boolean findAndClickByText(AccessibilityNodeInfo source, String searchString) {
        List <AccessibilityNodeInfo> button = source.findAccessibilityNodeInfosByText(searchString);
        if (button.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : button) {
                performClick(accessibilityNodeInfo);
            }
            return true;
        }
        return false;
    }

    protected void viewSelected(AccessibilityEvent event) {

    }

    protected void viewScrolled(AccessibilityEvent event) {

    }

    protected void viewLongClicked(AccessibilityEvent event) {

    }

    protected void viewHoverExit(AccessibilityEvent event) {

    }

    protected void viewHoverEnter(AccessibilityEvent event) {

    }

    protected void viewFocused(AccessibilityEvent event) {

    }

    protected void viewContextClicked(AccessibilityEvent event) {

    }

    protected void viewClicked(AccessibilityEvent event) {

    }

    protected void viewAccessibilityFocused(AccessibilityEvent event) {

    }

    protected void viewAccessibilityFocusCleared(AccessibilityEvent event) {}

    protected void touchInteractionStart(AccessibilityEvent event) {}

    protected void touchInteractionEnd(AccessibilityEvent event) {}

    protected void touchExplorationGestureStart(AccessibilityEvent event) {}

    protected void touchExplorationGestureEnd(AccessibilityEvent event) {}

    protected void notificationStateChanged(AccessibilityEvent event) {}

    protected void gestureDetectionStart(AccessibilityEvent event) {}

    protected void gestureDetectionEnd(AccessibilityEvent event) {}

    protected void assistReadingContext(AccessibilityEvent event) {}

    protected void viewTextChanged(AccessibilityEvent event) {}

    protected void viewTextSelectionChanged(AccessibilityEvent event) {}

    protected void viewTextTraversedAtMovementGranularity(AccessibilityEvent event) {}

    protected void windowContentChanged(AccessibilityEvent event) {}

    protected void windowStateChanged(AccessibilityEvent event) {}

    protected void windowsChanged(AccessibilityEvent event) {}

    protected void announcement(AccessibilityEvent event) {}

    protected abstract AccessibilityStateEnum getNextState();


    /**
     * Change the current state to the next state. Which state is initiated next is defined in the
     * classes getNextState().
     */
    void rebuildEventStateHandler() {
        this.context.setAccessibilityState(AccessibilityStateManager.getInstance().rebuildHandler(getNextState(), context));
    }

    void dispatchHomeScreenIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
