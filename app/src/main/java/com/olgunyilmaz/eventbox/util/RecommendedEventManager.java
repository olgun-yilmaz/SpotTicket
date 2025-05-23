/*
 * Project: EventBox
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

package com.olgunyilmaz.eventbox.util;

import com.olgunyilmaz.eventbox.model.EventResponse;

import java.util.ArrayList;
import java.util.List;

public class RecommendedEventManager {
    private static RecommendedEventManager instance;
    public List<EventResponse.Event> recommendedEvents;

    private RecommendedEventManager() {
        recommendedEvents = new ArrayList<>();
    }

    public static RecommendedEventManager getInstance() {
        if (instance == null) {
            instance = new RecommendedEventManager();
        }
        return instance;
    }
}
