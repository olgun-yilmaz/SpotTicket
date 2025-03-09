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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.olgunyilmaz.spotticket.R;

public class NotificationScheduler {
    private final String eventName;
    private final long daysLeft;
    private final long categoryIconId;

    public NotificationScheduler(Long daysLeft, String eventName, Long categoryIconId) {
        this.eventName = eventName;
        this.daysLeft = daysLeft;
        this.categoryIconId = categoryIconId;
    }

    @SuppressLint("MissingPermission")
    public void scheduleNotification(Context context, Long delayInSeconds) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(context.getString(R.string.days_left_key),this.daysLeft);
        intent.putExtra(context.getString(R.string.event_name_key),this.eventName);
        intent.putExtra(context.getString(R.string.category_icon_key),this.categoryIconId);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + (delayInSeconds * 1000);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent);


        if (alarmManager != null) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }

    }
}
