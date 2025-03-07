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

import com.google.gson.Gson;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.CategoryResponse;
import com.olgunyilmaz.spotticket.model.CitiesResponse;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;

public class HomePageHelper {
    private final Context context;

    public HomePageHelper(Context context) {
        this.context = context;
    }

    public void setEnableHomeButton(){
        MainActivity activity = (MainActivity) context;
        activity.binding.displayButton.setEnabled(false);
        activity.binding.homeButton.setEnabled(true);
    }

    public ArrayList<CategoryResponse> getCategories() {
        ArrayList<CategoryResponse> categories = new ArrayList<>();

        categories.add(new CategoryResponse(R.drawable.all_categories,context.getString(R.string.all_categories)));
        categories.add(new CategoryResponse(R.drawable.family, context.getString(R.string.family)));
        categories.add(new CategoryResponse(R.drawable.basketball, context.getString(R.string.basketball)));
        categories.add(new CategoryResponse(R.drawable.baseball, context.getString(R.string.baseball)));
        categories.add(new CategoryResponse(R.drawable.blues, context.getString(R.string.blues)));
        categories.add(new CategoryResponse(R.drawable.ice_hockey, context.getString(R.string.ice_hockey)));
        categories.add(new CategoryResponse(R.drawable.dancing, context.getString(R.string.dancing)));
        categories.add(new CategoryResponse(R.drawable.disney, context.getString(R.string.disney)));
        categories.add(new CategoryResponse(R.drawable.movie, context.getString(R.string.movie)));
        categories.add(new CategoryResponse(R.drawable.folk, context.getString(R.string.folk)));

        categories.add(new CategoryResponse(R.drawable.electro, context.getString(R.string.electro)));
        categories.add(new CategoryResponse(R.drawable.entertainment,context.getString(R.string.entertainment)));
        categories.add(new CategoryResponse(R.drawable.hip_hop, context.getString(R.string.hip_hop)));
        categories.add(new CategoryResponse(R.drawable.jazz, context.getString(R.string.jazz)));
        categories.add(new CategoryResponse(R.drawable.classical_music, context.getString(R.string.classical_music)));
        categories.add(new CategoryResponse(R.drawable.conference, context.getString(R.string.conference)));
        categories.add(new CategoryResponse(R.drawable.cultural, context.getString(R.string.cultural)));
        categories.add(new CategoryResponse(R.drawable.mma, context.getString(R.string.mma)));
        categories.add(new CategoryResponse(R.drawable.music, context.getString(R.string.music)));
        categories.add(new CategoryResponse(R.drawable.musical, context.getString(R.string.musical)));

        categories.add(new CategoryResponse(R.drawable.opera, context.getString(R.string.opera)));
        categories.add(new CategoryResponse(R.drawable.rb, context.getString(R.string.rb)));
        categories.add(new CategoryResponse(R.drawable.rock, context.getString(R.string.rock)));
        categories.add(new CategoryResponse(R.drawable.pop_music, context.getString(R.string.pop_music)));
        categories.add(new CategoryResponse(R.drawable.theater_art, context.getString(R.string.theater_art)));
        categories.add(new CategoryResponse(R.drawable.exhibition,context.getString(R.string.exhibition)));
        categories.add(new CategoryResponse(R.drawable.sports, context.getString(R.string.sports)));
        categories.add(new CategoryResponse(R.drawable.tennis, context.getString(R.string.tennis)));
        categories.add(new CategoryResponse(R.drawable.theater, context.getString(R.string.theater)));
        categories.add(new CategoryResponse(R.drawable.food, context.getString(R.string.food)));

        return categories;

    }

    public ArrayList<String> getCities() {
        try {
            Reader reader = new InputStreamReader(context.getAssets().open(context.getString(R.string.cities_json_file)));
            Gson gson = new Gson();
            CitiesResponse response = gson.fromJson(reader, CitiesResponse.class);

            if (response != null && response.getCities() != null) {
                ArrayList<String> cities = response.getCities();
                Collections.sort(cities);
                return cities;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
