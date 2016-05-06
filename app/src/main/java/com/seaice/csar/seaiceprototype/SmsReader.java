package com.seaice.csar.seaiceprototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by Ivan on 23/04/2016.
 */
public class SmsReader extends BroadcastReceiver {
    final SmsManager mySmsManager = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    try
                    {
                        MapsActivity.getInstance().putDataMap(message);
                    }
                    catch(Exception e)
                    {

                    }

                    // Show Alert
                    //int duration = Toast.LENGTH_LONG;
                    //Toast toast = Toast.makeText(context,
                      //      "senderNum: "+ senderNum + ", message: " + message, duration);
                    //toast.show();


                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }

    }
}
