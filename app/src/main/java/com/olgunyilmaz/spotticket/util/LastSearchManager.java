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

import com.olgunyilmaz.spotticket.model.EventResponse;

import java.util.ArrayList;
import java.util.List;

public class LastSearchManager {
    private static LastSearchManager instance;
    public List<EventResponse.Event> lastEvents;
    public String sender;

    private LastSearchManager() {
        lastEvents = new ArrayList<>();
        sender = "";
    }

    public static LastSearchManager getInstance() {
        if (instance == null) {
            instance = new LastSearchManager();
        }
        return instance;
    }
}
