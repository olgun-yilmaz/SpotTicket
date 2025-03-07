/*
 * Project: SpotTicket
 * Copyright 2025 Olgun Yılmaz
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

package com.olgunyilmaz.spotticket.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.olgunyilmaz.spotticket.R;

public class NotificationHelper {
    private final Context context;
    private final NotificationManager notificationManager;
    private final String eventName;

    public NotificationHelper(Context context, String eventName) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.eventName = eventName;
    }

    public void sendNotification(){
        String channelID = "channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID,"Example Channel", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelID)
                .setSmallIcon(R.drawable.entertainment)
                .setContentTitle(eventName)
                .setContentText(context.getString(R.string.notification_context));

        notificationManager.notify(1,builder.build());
    }
}
