package de.fau.i1.aka.avt_em.accessibility.phone;


import de.fau.i1.aka.avt_em.accessibility.AccessibilityHandler;
import de.fau.i1.aka.avt_em.accessibility.AccessibilityTask;

public abstract class MarshmallowPhoneService extends AbstractPhoneService implements AccessibilityTask
{

    @Override
    protected AccessibilityHandler getImplementation()
    {
        return new MarshmallowPhone(this);
    }
}
