package kr.playar.playar_2;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Erroneous on 2018-03-25.
 */

public class VibrationService {
    private Vibrator vibrator;
    private MainActivity activity;
    public VibrationService(MainActivity activity){
        this.activity = activity;
        this.vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
    }
    public void vibrate(){
        vibrator.vibrate(1000);
    }
    public void notify_() {
        NotificationCompat.Builder mBuilder = createNotification();
        NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
    private NotificationCompat.Builder createNotification(){
        Bitmap icon = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("앞길 알리미")
                .setContentText("앞길에 장애물이 있습니다.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }



}
