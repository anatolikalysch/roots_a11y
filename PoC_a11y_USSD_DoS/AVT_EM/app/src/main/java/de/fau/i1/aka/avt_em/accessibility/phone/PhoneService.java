package de.fau.i1.aka.avt_em.accessibility.phone;

import android.os.Build;

import de.fau.i1.aka.avt_em.accessibility.AccessibilityHandler;
import de.fau.i1.aka.avt_em.accessibility.AccessibilityTask;


public abstract class PhoneService extends AbstractPhoneService implements AccessibilityTask
{
    private static final String TAG = PhoneService.class.getName();


    @Override
    protected AccessibilityHandler getImplementation()
    {
        switch (Build.VERSION.SDK_INT)
        {
            case Build.VERSION_CODES.M:
            case Build.VERSION_CODES.N:
            case Build.VERSION_CODES.N_MR1:
            case Build.VERSION_CODES.O:
                return new MarshmallowPhone(this);
            case Build.VERSION_CODES.CUR_DEVELOPMENT:
            default:
                return new MarshmallowPhone(this);
        }
    }
}
