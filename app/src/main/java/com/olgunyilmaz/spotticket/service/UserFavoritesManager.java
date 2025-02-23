package com.olgunyilmaz.spotticket.service;

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
        if (!userFavorites.contains(event)) {
            userFavorites.add(event);
        }
    }

    public void removeFavorite(String eventID) {
        for (FavoriteEventModel event : userFavorites){
            if (eventID.equals(event.getEventId()) ){
                userFavorites.remove(event);
                break;
            }
        }
    }
}
