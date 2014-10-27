package ua.pp.dimoshka.jw_stand_report;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Created by dimoshka on 26.10.14.
 */
public class class_send_sms extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(context, context.getString(R.string.sms_send), Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(context, context.getString(R.string.unknown_problems), Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(context, context.getString(R.string.modul_is_down), Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:

                break;
        }
    }

}