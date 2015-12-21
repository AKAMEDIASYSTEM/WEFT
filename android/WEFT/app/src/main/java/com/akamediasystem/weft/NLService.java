package com.akamediasystem.weft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.akamediasystem.weft.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new  Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    // AKA note to self, this method creating sbn is the WEFT to notification link
                    // have a list/structure of packages (gmail, twitter, facebook, etc) to filter on
                    // and trigger a WEFT pattern when sbn has one of those notifs in it?
                    // note this also means setting up a struct for WEFT patterns, probably
                    // very much like the two-byte rfduino protocol right now,
                    // but with the eventual ability to layer/sum waveforms
                    // so, lo-freq square PLUS hi-freq sine, etc
                    Intent i2 = new  Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
//                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    // AKA added this
                    Log.i(TAG, sbn.getNotification().toString());
//                    String mys = (String)sbn.getNotification().tickerText.toString();
                    i2.putExtra("notification_event", i + " " + sbn.getNotification().toString() + "\n");

                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}