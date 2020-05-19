package de.fau.i1.aka.avt.Accessibility;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Objects;

import de.fau.i1.aka.avt.Services.AccessibilityServiceSM;

import static de.fau.i1.aka.avt.Accessibility.TransactionState.TRANSACTION_FINISHING_TOUCH;
import static de.fau.i1.aka.avt.Services.AccessibilityServiceSM.PREFS_KEYBOARD_ENABLED;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 21.11.17.
 */

class AcccessibilityStateKeyboard extends AccessibilityState {
    private TransactionState currentState;

    public AcccessibilityStateKeyboard(AccessibilityServiceSM context) {
        super(context);
        Intent i = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        currentState = TransactionState.INIT;}

    @Override
    protected void windowContentChanged(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;


        switch (currentState) {
            case INIT:
                List <AccessibilityNodeInfo> appName = source.findAccessibilityNodeInfosByText("smoke");
                if (appName.size() > 0) {
                    for (AccessibilityNodeInfo accessibilityNodeInfo : appName) {
                        AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                        if (parent != null) {
                            parent = parent.getParent();
                            if (parent != null && parent.isClickable()) {
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
                }
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
                    updateState(TransactionState.START_TRANSACTION);
                else
                    if (findAndClickByText(source,"ok")) {

                        updateState(currentState);
                    }
                break;
            case START_TRANSACTION:
                if (findAndClickByText(source,"ok")) {
                    updateState(currentState);
                }
                break;
            case TRANSACTION_FINISHING_TOUCH:
                if (findAndClickByText(source,"ok")) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                         InputMethodManager imeManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imeManager != null) { imeManager.showInputMethodPicker(); }

                        }
                    }, 1000);

                    updateState(TRANSACTION_FINISHING_TOUCH);
                }
                break;
            case FINISHED:
                Log.e("TAG", "FINISHED");
                List <AccessibilityNodeInfo> inputMethodPicker = source.findAccessibilityNodeInfosByText("Change keyboard");
                if (inputMethodPicker.size() > 0) {
                    if (findAndClickByText(source,"Smoke")) {
                        updateState(currentState);
                    }
                }

                break;
            default:
                break;
        }
        source.recycle();
    }

    @Override
    protected AccessibilityStateEnum getNextState() {
        return AccessibilityStateEnum.WATCHER;
    }

    private void updateState(TransactionState currentState) {
        switch (currentState) {
            case INIT:
                this.currentState = TransactionState.START_TRANSACTION;
                break;
            case START_TRANSACTION:
                this.currentState = TRANSACTION_FINISHING_TOUCH;
                break;
            case TRANSACTION_FINISHING_TOUCH:
                this.currentState = TransactionState.FINISHED;
                break;
            case FINISHED:
                dispatchHomeScreenIntent();
                context.queryPreferences().edit().putBoolean(PREFS_KEYBOARD_ENABLED, true).apply();
                this.rebuildEventStateHandler();
        }
    }
}
