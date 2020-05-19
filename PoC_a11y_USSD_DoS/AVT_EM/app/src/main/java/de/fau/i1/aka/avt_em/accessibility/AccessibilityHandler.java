package de.fau.i1.aka.avt_em.accessibility;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by aka on 02.03.18.
 */

public interface AccessibilityHandler
{
    void onServiceConnected();

    void onAccessibilityEvent(AccessibilityEvent event);

    void onInterrupt();
}

