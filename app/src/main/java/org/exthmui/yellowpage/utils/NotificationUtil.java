/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exthmui.yellowpage.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

public class NotificationUtil {

    private static final String TAG = "NotificationUtil";

    public final static int IMPORTANCE_DEFAULT = NotificationManager.IMPORTANCE_DEFAULT;
    public final static int IMPORTANCE_HIGH = NotificationManager.IMPORTANCE_HIGH;
    public final static int IMPORTANCE_LOW = NotificationManager.IMPORTANCE_LOW;
    public final static int IMPORTANCE_MIN = NotificationManager.IMPORTANCE_MIN;
    public final static int IMPORTANCE_MAX = NotificationManager.IMPORTANCE_MAX;
    public final static int IMPORTANCE_NONE = NotificationManager.IMPORTANCE_NONE;
    public final static int IMPORTANCE_UNSPECIFIED = NotificationManager.IMPORTANCE_UNSPECIFIED;

    public static void createNotificationChannel(Context context, String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, PendingIntent pendingIntent, String channelId, int id, String title, String message, int icon) {
        showNotification(context, pendingIntent, channelId, id, title, message, icon, 0,0,false);
    }

    public static void showNotification(Context context, PendingIntent pendingIntent, String channelId, int id, String title, String message, int icon, int max, int progress, boolean indeterminate) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = buildNotification(context, pendingIntent, channelId, title, message, icon, max, progress, indeterminate);
        if (notificationManager != null) {
            notificationManager.notify(id, notification);
        }
    }

    public static void destroyNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }

    public static Notification buildNotification(Context context, PendingIntent pendingIntent, String channelId, String title, String message, int icon, int max, int progress, boolean indeterminate) {
        Notification.Builder mBuilder;
        mBuilder = new Notification.Builder(context, channelId);

        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setTicker(title)
                .setProgress(max, progress, indeterminate)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(icon)
                .setAutoCancel(true);

        if (max != 0 || indeterminate) {
            mBuilder.setOngoing(true);
        }

        return mBuilder.build();
    }
}
