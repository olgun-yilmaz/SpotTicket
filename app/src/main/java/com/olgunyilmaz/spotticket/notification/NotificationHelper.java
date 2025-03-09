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

package com.olgunyilmaz.spotticket.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class NotificationHelper {
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void sendNotification(Long daysLeft, String event, Long categoryIconId) {
        if(daysLeft != null){
            Intent intent = new Intent(context, OnBoardingActivity.class);
            intent.putExtra(context.getString(R.string.event_name_key),event);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            LocalDataManager localDataManager = new LocalDataManager(context);
            int id = localDataManager.getIntegerData(context.getString(R.string.notification_id_key), 1);

            String channelID = id+event;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelID, event, NotificationManager.IMPORTANCE_DEFAULT);

                notificationManager.createNotificationChannel(channel);
            }

            String content = (daysLeft == 0)
                    ? context.getString(R.string.notification_last_day_content, event) //if
                    : context.getString(R.string.notification_content, daysLeft); //else

            String title = (daysLeft == 0)
                    ? "Spot Ticket"
                    : event;

            categoryIconId = (categoryIconId == 0)
                    ? R.drawable.electro
                    : categoryIconId;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(Math.toIntExact(categoryIconId))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true) // notification gone on click
                    .setContentIntent(pendingIntent);

            notificationManager.notify(id, builder.build());
            id ++; // don't overwrite

            localDataManager.updateIntegerData(context.getString(R.string.notification_id_key),id);
        }
    }

    public Long calculateDaysLeft(String date){
        long days;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant eventTime = Instant.parse(date);
            Instant now = Instant.now();

            Duration duration = Duration.between(now, eventTime);
            days = duration.toDays();

        } else {
            Date fixedDate = new Date(date);
            Date now = new Date();

            long diffInMillis = fixedDate.getTime() - now.getTime();
            days = diffInMillis / (1000 * 60 * 60 * 24);
        }
        return days;
    }
}
