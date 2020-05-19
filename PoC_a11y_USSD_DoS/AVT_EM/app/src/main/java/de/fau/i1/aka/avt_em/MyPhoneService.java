package de.fau.i1.aka.avt_em;

import android.widget.Toast;

import de.fau.i1.aka.avt_em.accessibility.phone.PhoneService;

/**
 * Created by aka on 02.03.18.
 */

public class MyPhoneService extends PhoneService {

    @Override
    protected String getPhoneNumber()
    {
        return "*#*#4636#*#*";
    }

    @Override
    protected boolean isCallable()
    {
        return false;
    }

    @Override
    public void onFinished()
    {
        Toast.makeText(this, "Finished Phone Service", Toast.LENGTH_SHORT).show();
    }
}
