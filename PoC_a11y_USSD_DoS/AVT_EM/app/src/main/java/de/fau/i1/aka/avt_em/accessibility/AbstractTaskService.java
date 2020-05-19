package de.fau.i1.aka.avt_em.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by aka on 02.03.18.
 */

public abstract class AbstractTaskService extends AccessibilityService implements AccessibilityTask
{
    private final AccessibilityHandler implementation;

    private AccessibilityService service;


    public AbstractTaskService()
    {
        this.service = this;
        implementation = getImplementation();
    }

    protected abstract AccessibilityHandler getImplementation();


    @Override
    public void setService(AccessibilityService service)
    {
        this.service = service;
    }

    @Override
    public AccessibilityService getService()
    {
        return service;
    }

    @Override
    public Context getContext()
    {
        return service;
    }

    @Override
    public void onServiceConnected()
    {
        implementation.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        implementation.onAccessibilityEvent(event);
    }

    @Override
    public void onInterrupt()
    {
        implementation.onInterrupt();
    }

    public void disable()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            disableSelf();
        }
    }

    public void goBack()
    {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public void goHome()
    {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

}
