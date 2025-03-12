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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.view.activities.EmailPasswordActivity;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.EventDetailsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainHelper {
    private final MainActivity activity;
    private List<ImageView> menuButtons;

    public MainHelper(MainActivity activity) {
        this.activity = activity;
        getMenuButtons();
    }

    public void disableButton(View selectedButton) { // disable chosen button
        for (ImageView button : menuButtons) {
            if (button == selectedButton) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
            }

        }
    }

    public void goToLoginActivity() {
        Intent intent = new Intent(activity, EmailPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    private void getMenuButtons() {
        menuButtons = new ArrayList<>();
        menuButtons.add(activity.binding.profileButton);
        menuButtons.add(activity.binding.myEventsButton);
        menuButtons.add(activity.binding.homeButton);
        menuButtons.add(activity.binding.displayButton);
    }

    public void replaceFragment(Fragment fragment, View sender, FragmentManager fragmentManager) {
        disableButton(sender);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    public void directToEventDetails(String eventName, FragmentManager fragmentManager) {
        if (eventName != null) {
            for (FavoriteEventModel event : UserFavoritesManager.getInstance().userFavorites) {
                if (event.getEventName().equals(eventName)) {

                    Bundle bundle = new Bundle();
                    bundle.putString(activity.getString(R.string.event_id_key), event.getEventId());
                    bundle.putString(activity.getString(R.string.event_name_key), event.getEventName());
                    bundle.putString(activity.getString(R.string.image_url_key), event.getImageUrl());
                    bundle.putString(activity.getString(R.string.event_date_key), event.getDate());
                    bundle.putLong(activity.getString(R.string.category_icon_key), event.getCategoryIcon());

                    EventDetailsFragment fragment = new EventDetailsFragment();
                    fragment.setArguments(bundle);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();

                    break;
                }
            }

        }
    }

    public Integer calculateSendDelayInHours(Long daysLeft) {
        Random random = new Random();

        int lowerLimit, upperLimit;

        if(daysLeft < 0){ // past event
            return null;
        }
        else if (daysLeft == 0) { // last day -> sent it immediately
            lowerLimit = 1;
            upperLimit = 2;

        } else if (daysLeft < 7) { // last week
            lowerLimit = 12; // 12 hours
            upperLimit = 1 + (int) (23 * (daysLeft - 1)); // example 4 days : 3 * 23 + 1 = 70 hours

        } else {
            lowerLimit = 24; // 1 day
            upperLimit = 24 * 6; // 6 day
        }

        int delayInHours = lowerLimit + random.nextInt(upperLimit); // random between 1-6 days

        if (daysLeft >= 7) {
            delayInHours += (int) ((daysLeft - 7) * 24);
        }

        /* 1-6 days + daysLeft-7
           example : daysLeft = 30
           sentDelayInHours = 24 * 23 (if condition) + 24 * 3 (random) = 26 * 24 (total)
           result : sent before 4 days*/

        return delayInHours;

    }

    public void showExitDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.exit_text))
                .setMessage(activity.getString(R.string.exit_question_text))
                .setPositiveButton(activity.getString(R.string.answer_yes), (dialog, which) -> activity.finish())
                .setNegativeButton(activity.getString(R.string.answer_no), null)
                .show();
    }
}
