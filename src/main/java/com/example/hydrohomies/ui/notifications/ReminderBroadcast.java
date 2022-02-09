package com.example.hydrohomies.ui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.hydrohomies.ui.notifications.NotificationUtils;

import java.util.Random;

public class ReminderBroadcast extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        NotificationUtils _notificationUtils = new NotificationUtils(context);
        NotificationCompat.Builder _builder = _notificationUtils.setNotification("HydroHomies", "Stay hydrated!");
        _notificationUtils.getManager().notify(m, _builder.build());
    }
}