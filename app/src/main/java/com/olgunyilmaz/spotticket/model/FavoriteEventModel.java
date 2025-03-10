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

public class FavoriteEventModel { // stores important data about user-favorite events.

    // notification status is saved in SharedPreferences:
    // (eventId, true) -> Example:
    // ("AzzsDvmnfdj", false): No reminder notification has been sent for this event.

    public FavoriteEventModel(String eventId, String eventName, String imageUrl, String date, Long categoryIcon) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
        this.eventName = eventName;
        this.date = date;
        this.categoryIcon = categoryIcon;
    }

    // this class is designed to store important data about favorite events.


    private final String eventId;
    private final String eventName;
    private final String imageUrl; // event high quality image url
    private final String date;
    private final Long categoryIcon; // for notification icon

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

    public Long getCategoryIcon() {
        return categoryIcon;
    }
}