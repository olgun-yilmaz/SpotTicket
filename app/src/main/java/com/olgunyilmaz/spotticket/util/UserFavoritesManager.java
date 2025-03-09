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

import com.olgunyilmaz.spotticket.model.FavoriteEventModel;

import java.util.ArrayList;

public class UserFavoritesManager {
    private static UserFavoritesManager instance;
    public ArrayList<FavoriteEventModel> userFavorites;

    private UserFavoritesManager() {
        userFavorites = new ArrayList<>();
    }

    public static UserFavoritesManager getInstance() {
        if (instance == null) {
            instance = new UserFavoritesManager();
        }
        return instance;
    }

    public void addFavorite(FavoriteEventModel event) {
        if (userFavorites != null) {
            if (!userFavorites.contains(event)) {
                userFavorites.add(event);
            }
        }
    }

    public void removeFavorite(String eventID) {
        userFavorites.removeIf(event -> eventID.equals(event.getEventId()));
    }
}
