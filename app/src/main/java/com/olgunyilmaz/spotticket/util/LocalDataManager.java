package com.olgunyilmaz.spotticket.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalDataManager {
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "com.olgunyilmaz.spotticket";
    private Context context;

    public LocalDataManager(Context context) {
        this.context = context;

        if (context != null){
            sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        }

    }

    public void deleteAllData(){ // clear phone memory
        deleteData("userEmail");
        deleteData("city");
        deleteData("category");
        deleteData("rememberMe");
    }

    public void deleteData(String key){
        if (key != null){
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public String getStringData(String key, String defaultValue){
        if (key != null){
            return sharedPreferences.getString(key,defaultValue);
        }
        return "";
    }

    public void updateStringData(String key, String value){
        if (key != null && value != null){
            sharedPreferences.edit().putString(key,value).apply();
        }
    }

    public boolean getBooleanData(String key){
        if (key != null){
            return sharedPreferences.getBoolean(key,false);
        }
        return false;
    }

    public void updateBooleanData(String key, Boolean value){
        if (key != null && value != null){
            sharedPreferences.edit().putBoolean(key,value).apply();
        }
    }

}
