package de.fau.i1.aka.avt_em.accessibility;

/**
 * Created by aka on 02.03.18.
 */

public abstract class AbstractPhoneService extends AbstractTaskService implements AccessibilityTask
{

    protected abstract String getPhoneNumber();

    protected abstract boolean isCallable();
}
