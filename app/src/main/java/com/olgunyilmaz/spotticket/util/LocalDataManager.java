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
import android.content.SharedPreferences;

import com.olgunyilmaz.spotticket.R;

public class LocalDataManager {
    private SharedPreferences sharedPreferences;
    private static String PREF_NAME;
    private Context context;

    public LocalDataManager(Context context) {
        this.context = context;

        if (context != null){
            PREF_NAME = context.getString(R.string.pref_name);
            sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        }

    }

    public void deleteAllData(){ // clear phone memory
        deleteData(context.getString(R.string.user_email_key));
        deleteData(context.getString(R.string.city_key));
        deleteData(context.getString(R.string.category_key));
        deleteData(context.getString(R.string.remember_me_key));
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

    public int getIntegerData(String key, int defaultValue){
        if (key != null){
            return sharedPreferences.getInt(key,defaultValue);
        }
        return 0;
    }

    public void updateIntegerData(String key, int value){
        if (key != null){
            sharedPreferences.edit().putInt(key,value).apply();
        }
    }

}
