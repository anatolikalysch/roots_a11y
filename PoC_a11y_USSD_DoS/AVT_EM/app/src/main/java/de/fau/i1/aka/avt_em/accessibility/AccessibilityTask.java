package de.fau.i1.aka.avt_em.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;

/**
 * Created by aka on 02.03.18.
 */

public interface AccessibilityTask extends AccessibilityHandler
{
    void setService(AccessibilityService context);

    AccessibilityService getService();

    Context getContext();

    void onFinished();
}
