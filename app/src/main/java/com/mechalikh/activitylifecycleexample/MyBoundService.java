package com.mechalikh.activitylifecycleexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class MyBoundService extends Service {

    private static final String CHANNEL_ID = "ServiceChannel";
    private final IBinder binder = new LocalBinder();
    private BluetoothReceiver bluetoothReceiver;

    public class LocalBinder extends Binder {
        MyBoundService getService() {
            return MyBoundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true); // Stops the foreground service when activity binds
        updateNotification("Service paused"); // Update notification when service is paused
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        updateNotification("Service started"); // Update notification when service is started
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Create notification channel
        bluetoothReceiver = new BluetoothReceiver();
        // Register receiver for Bluetooth state changes
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true); // Remove the notification when the service stops
        unregisterReceiver(bluetoothReceiver); // Unregister the receiver
    }

    private void createNotificationChannel() {
        // Only for API level 26 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void updateNotification(String message) {
        Intent notificationIntent = new Intent(this, LifecycleDemoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service Status")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Start the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // For API 26 and above, we need to use the notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.notify(1, builder.build()); // Update the notification
            } else {
                // For API levels below 26
                notificationManager.notify(1, builder.build());
            }
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        updateNotification("Bluetooth is ON"); // Update notification when Bluetooth is ON
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        updateNotification("Bluetooth is OFF"); // Update notification when Bluetooth is OFF
                        break;
                }
            }
        }
    }
}
