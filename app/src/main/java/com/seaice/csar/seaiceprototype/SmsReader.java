package com.seaice.csar.seaiceprototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
                        //-----------------Borrar Mensaje
                        Thread.sleep(2000);
                        deleteSMS(context, "+50258228830");

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

    public void deleteSMS(Context context, String number) {
        try {
            System.out.println("Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address",
                            "person", "date", "body" }, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    if (address.equals(number)) {
                        System.out.println("Deleting SMS with id: " + threadId);
                        context.getContentResolver().delete(
                                Uri.parse("content://sms/" + id), null, null);
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            System.out.println("Could not delete SMS from inbox: " + e.getMessage());
        }
    }
}
