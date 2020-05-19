package de.fau.i1.aka.avt_em.accessibility.phone;


import de.fau.i1.aka.avt_em.accessibility.AbstractTaskService;
import de.fau.i1.aka.avt_em.accessibility.AccessibilityTask;

public abstract class AbstractPhoneService extends AbstractTaskService implements AccessibilityTask
{

    protected abstract String getPhoneNumber();

    protected abstract boolean isCallable();
}
