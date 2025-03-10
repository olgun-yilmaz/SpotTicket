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

import android.content.Context;

import com.olgunyilmaz.spotticket.R;

// stores static constants for API keys and base URLs used throughout the app.
public class Constants {
    public static String TICKETMASTER_BASE_URL;
    public static String TICKETMASTER_API_KEY;
    public static String MAPS_BASE_URL;
    public static String MAPS_API_KEY;

    public Constants(Context context) {
        TICKETMASTER_BASE_URL = context.getString(R.string.ticketmaster_base_url);
        TICKETMASTER_API_KEY = context.getString(R.string.ticketmaster_api_key);

        MAPS_BASE_URL = context.getString(R.string.maps_base_url);
        MAPS_API_KEY = context.getString(R.string.maps_api_key);
    }
}
