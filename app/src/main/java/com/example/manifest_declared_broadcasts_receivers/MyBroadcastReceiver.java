package com.example.manifest_declared_broadcasts_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by aruna on 1/21/18.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private  int lastState = TelephonyManager.CALL_STATE_IDLE;
    private  Date callStartTime;
    private  boolean isIncoming;
    private  String savedNumber;  //because the passed incoming is only valid in ringing


    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);

            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor c = context.getContentResolver().query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME},null,null,null);
            try {
                c.moveToFirst();
                String  displayName = c.getString(0);
                String contact = displayName;

                Log.e("Name",contact);
            } catch (Exception e) {
                Log.e("Contact Name", e.getMessage());
            }finally{
                c.close();
            }
        }

    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                Log.e("Number",savedNumber.toString());

                Toast.makeText(context,"Number", Integer.parseInt(savedNumber.toString()));
//                onIncomingCallReceived(context, number, callStartTime);
                break;
        }
        lastState = state;
    }
}
