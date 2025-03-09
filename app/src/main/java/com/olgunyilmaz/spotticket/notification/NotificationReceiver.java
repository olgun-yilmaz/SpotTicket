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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.olgunyilmaz.spotticket.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper helper = new NotificationHelper(context);
        Long daysLeft = intent.getLongExtra(context.getString(R.string.days_left_key),0L);
        String eventName = intent.getStringExtra(context.getString(R.string.event_name_key));
        Long categoryIconId = intent.getLongExtra(context.getString(R.string.category_icon_key),R.drawable.mma);

        helper.sendNotification(daysLeft,eventName,categoryIconId);
    }
}