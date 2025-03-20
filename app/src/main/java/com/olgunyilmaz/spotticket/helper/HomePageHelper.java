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

package com.olgunyilmaz.spotticket.helper;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentHomePageBinding;
import com.olgunyilmaz.spotticket.model.CitiesResponse;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.DisplayFragment;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HomePageHelper {
    private final Context context;
    private final FragmentHomePageBinding binding;

    public HomePageHelper(Context context, FragmentHomePageBinding binding) {
        this.context = context;
        this.binding = binding;
    }

    public ArrayList<View> getHomeViews(){
        if(binding != null){
            return new ArrayList<>(Arrays.asList(
                    binding.profileLayout,
                    binding.upcomingEventsLayout,
                    binding.dateLayout,
                    binding.upcomingEventsRecyclerView,
                    binding.recommendedEventLayout
            ));
        }
        return null;

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
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    public void replaceFragment(Fragment fragment){
        MainActivity activity = (MainActivity) context;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView,fragment).commit();
    }

    public void searchEventByKeyword() {
        String keyword = binding.homeSearchEditText.getText().toString();

        if (keyword.length() >= 3) {
            DisplayFragment fragment = new DisplayFragment();

            MainActivity activity = (MainActivity) context;
            activity.binding.displayButton.setChecked(true);

            Bundle args = new Bundle();
            args.putBoolean(context.getString(R.string.search_by_keyword_key), true);
            args.putString(context.getString(R.string.keyword_key), keyword);
            fragment.setArguments(args);

            replaceFragment(fragment);
        } else {
            Toast.makeText(context, context.getString(R.string.weak_search_error), Toast.LENGTH_SHORT).show();
        }

    }
}