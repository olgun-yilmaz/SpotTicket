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


        categories.add(new CategoryResponse(R.drawable.movie, context.getString(R.string.movie)));
        categories.add(new CategoryResponse(R.drawable.electro, context.getString(R.string.electro)));
        categories.add(new CategoryResponse(R.drawable.mma, context.getString(R.string.mma)));
        categories.add(new CategoryResponse(R.drawable.music, context.getString(R.string.music)));

        categories.add(new CategoryResponse(R.drawable.sports, context.getString(R.string.sports)));
        categories.add(new CategoryResponse(R.drawable.theater, context.getString(R.string.theater)));

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
