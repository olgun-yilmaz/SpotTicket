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

package com.olgunyilmaz.spotticket.model;

public class FavoriteEventModel {
    // is notification sent data saving in sharedPreferences
    // (eventId, true) -> example :
    // ("AzzsDvmnfdj",false) : never sent any remember notification about this event.
    public FavoriteEventModel(String eventId, String eventName, String imageUrl, String date) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
        this.eventName = eventName;
        this.date = date;
    }

    private final String eventId;
    private final String eventName;
    private final String imageUrl;
    private final String date;

    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }
}