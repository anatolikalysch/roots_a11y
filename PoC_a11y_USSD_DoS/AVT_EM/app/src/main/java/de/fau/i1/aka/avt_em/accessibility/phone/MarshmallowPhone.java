package de.fau.i1.aka.avt_em.accessibility.phone;

import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import de.fau.i1.aka.avt_em.accessibility.AccessibilityHandler;
import de.fau.i1.aka.avt_em.accessibility.IntentUtil;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
import static de.fau.i1.aka.avt_em.accessibility.NodeInfoUtil.click;
import static de.fau.i1.aka.avt_em.accessibility.NodeInfoUtil.eventTypeIs;
import static de.fau.i1.aka.avt_em.accessibility.NodeInfoUtil.findNodeById;
import static de.fau.i1.aka.avt_em.accessibility.NodeInfoUtil.findNodeByText;
import static de.fau.i1.aka.avt_em.accessibility.NodeInfoUtil.packageNameIs;


class MarshmallowPhone implements AccessibilityHandler
{
    private static final String INITIAL_NUMBER = "0";
    private static final String ANDROID_DIALER = "com.android.dialer";
    private static final String GOOGLE_DIALER = "com.google.android.dialer";
    private static final String ANDROID_SETTINGS = "com.android.settings";

    private static final String ID_INTERFIX = ":id/";

    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";
    private static final String FOUR = "four";
    private static final String FIVE = "five";
    private static final String SIX = "six";
    private static final String SEVEN = "seven";
    private static final String EIGHT = "eight";
    private static final String NINE = "nine";
    private static final String ZERO = "zero";
    private static final String STAR = "star";
    private static final String POUND = "pound";
    private static final String DELETE = "deleteButton";

    private static final String PHONE_INFO = "Phone information";
    private static final String RADIO_POWER = "radio_power";
    private static final String PREFERREDNETWORKTYPE = "preferredNetworkType";


    private static final String CALL = "dialpad_floating_action_button";
    private static final String TAG = "ACCSERV";

    private final AbstractPhoneService service;

    private int state;
    private String packageName;

    public MarshmallowPhone(AbstractPhoneService service) {
        this.service = service;
        state = 0;
    }

    @Override
    public void onServiceConnected() {
        Context context = service.getContext();
        IntentUtil.openPhone(context, INITIAL_NUMBER);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


        AccessibilityNodeInfo root = event.getSource();
        if (root == null) {
            return;
        }

        switch (state) {
            case 0:
                if (eventTypeIs(event, TYPE_WINDOW_CONTENT_CHANGED)
                        && packageNameIs(event, ANDROID_DIALER, GOOGLE_DIALER)) {
                    onDialer(root);
                }
                break;
            case 5:
                state = 0xbada11;
                dispatchHomeScreenIntent();
                break;
            case 0xbada11:
                service.onFinished();
                service.disable();
                break;
            default:
                if (packageNameIs(event, ANDROID_SETTINGS)) {
                    onTestingSettings(root);
                }
                break;
        }
    }

    private void onTestingSettings(AccessibilityNodeInfo root) {
        switch (state){
            case 1:
                packageName = ANDROID_SETTINGS;
                AccessibilityNodeInfo phoneInfoButton = findNodeByText(root, PHONE_INFO);
                if (phoneInfoButton != null) {
                    state++;
                    click(phoneInfoButton);
                }
                break;
            case 2:
                AccessibilityNodeInfo radioPower = findNodeById(root, getId(RADIO_POWER));
                if (radioPower != null) {
                    state++;
                    click(radioPower);
                }
                break;
            case 3:
                AccessibilityNodeInfo prefNetworkType = findNodeById(root, getId(PREFERREDNETWORKTYPE));
                if (prefNetworkType != null) {
                    state++;
                    click(prefNetworkType);
                }
                break;
            case 4:
                if (root.isScrollable()) {
                    root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    AccessibilityNodeInfo gsmOnly = findNodeByText(root, "GSM only");
                    if (gsmOnly != null) {
                        click(gsmOnly);
                        state++;
                    }
                }

                break;

        }
    }


    private void onDialer(AccessibilityNodeInfo node) {
        if (!chooseCorrectPackageName(node)) {
            return;
        }
        state++;

        String phoneNumber = service.getPhoneNumber();
        for (int i = 0; i < phoneNumber.length(); i++) {
            char character = phoneNumber.charAt(i);
            dialNumber(node, character);
        }

        boolean callable = service.isCallable();
        if (callable) {
            clickDialer(node, CALL);
        }
    }

    private boolean chooseCorrectPackageName(AccessibilityNodeInfo node) {
        packageName = ANDROID_DIALER;
        String id = getId(DELETE);
        AccessibilityNodeInfo deleteButton = findNodeById(node, id);
        if (deleteButton != null) {
            click(deleteButton);
            deleteButton.recycle();
            return true;
        }

        packageName = GOOGLE_DIALER;
        id = getId(DELETE);
        deleteButton = findNodeById(node, id);
        if (deleteButton != null) {
            click(deleteButton);
            deleteButton.recycle();
            return true;
        }

        return false;
    }

    private void dialNumber(AccessibilityNodeInfo node, char c) {
        switch (c) {
            case '1':
                clickDialer(node, ONE);
                break;
            case '2':
                clickDialer(node, TWO);
                break;
            case '3':
                clickDialer(node, THREE);
                break;
            case '4':
                clickDialer(node, FOUR);
                break;
            case '5':
                clickDialer(node, FIVE);
                break;
            case '6':
                clickDialer(node, SIX);
                break;
            case '7':
                clickDialer(node, SEVEN);
                break;
            case '8':
                clickDialer(node, EIGHT);
                break;
            case '9':
                clickDialer(node, NINE);
                break;
            case '0':
                clickDialer(node, ZERO);
                break;
            case '+':
                clickDialer(node, ZERO);
                clickDialer(node, ZERO);
                break;
            case '*':
                clickDialer(node, STAR);
                break;
            case '#':
                clickDialer(node, POUND);
                break;
            case ' ':
            case '(':
            case ')':
                break;
            default:
                throw new IllegalArgumentException("Phone number contains invalid characters.");
        }
    }

    private void clickDialer(AccessibilityNodeInfo node, String key) {
        String id = getId(key);
        AccessibilityNodeInfo keyButton = findNodeById(node, id);
        if (keyButton != null) {
            click(keyButton);
            keyButton.recycle();
        }
    }

    private String getId(String key) {
        return packageName + ID_INTERFIX + key;
    }

    @Override
    public void onInterrupt() {

    }

    void dispatchHomeScreenIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        service.startActivity(intent);
    }
}
