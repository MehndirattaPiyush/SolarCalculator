package solar.piyush.com.solarcalculator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.shredzone.commons.suncalc.SunTimes;

import java.util.Calendar;

public class NotifReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


            Intent notificationIntent = new Intent(context, MapsActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MapsActivity.class);
            stackBuilder.addNextIntent(notificationIntent);

        Toast.makeText(context , "notification nb", Toast.LENGTH_LONG).show();

        //setting updated alarm for next day.

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent2 = new Intent(context, NotifReciever.class);
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent2, PendingIntent.FLAG_UPDATE_CURRENT);

        SunTimes sunTimes = SunTimes.compute().on(Calendar.getInstance().getTime())
                .at(intent.getDoubleExtra("lat",0.0),intent.getDoubleExtra("lng",0.0))
                .execute();
        Calendar c = Calendar.getInstance();
        c.setTime(sunTimes.getSet());
        c.add(Calendar.HOUR, -1);

        if(c.before(Calendar.getInstance())){
            c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            sunTimes = SunTimes.compute().on(c.getTime())
                    .at(intent.getDoubleExtra("lat",0.0),intent.getDoubleExtra("lng",0.0))
                    .execute();
        }
        c.setTime(sunTimes.getSet());
        c.add(Calendar.HOUR,-1);
//        Calendar test = Calendar.getInstance();
//        test.add(Calendar.SECOND,2);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), broadcast);

// Making Notification to be shown
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "asdf";
            String description = "notif channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "123")
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("Golden Hour!")
                .setContentText("Time to capture...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());

    }

}
