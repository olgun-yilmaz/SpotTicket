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

import android.app.AlertDialog;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.ReAuthenticateDialogFragment;
import com.olgunyilmaz.spotticket.view.fragments.SettingsFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileHelper {
    private final MainActivity activity;
    private final FragmentProfileBinding binding;

    public ProfileHelper(MainActivity activity, FragmentProfileBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }

    public void goBackToSettings(){
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.fragmentContainerView,fragment).commit();
    }

    public void showDeleteAccountDialog() {
        if (activity != null) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.delete_account_text))
                    .setMessage(activity.getString(R.string.delete_account_question))
                    .setNegativeButton(activity.getString(R.string.answer_no), null)
                    .setPositiveButton(activity.getString(R.string.answer_yes), (dialogInterface, i) -> {
                        ReAuthenticateDialogFragment dialog = new ReAuthenticateDialogFragment();
                        dialog.show(activity.getSupportFragmentManager(), activity.getString(R.string.re_authenticate_tag));
                    }).show();
        }
    }

    public String getCreationDate(String countryCode, FirebaseUser user) {
        if (user.getMetadata() != null) {
            long creationTime = user.getMetadata().getCreationTimestamp();
            Date creationDate = new Date(creationTime);

            String country = countryCode.toUpperCase();

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale(countryCode, country));
            return sdf.format(creationDate);
        }
        return activity.getString(R.string.date_not_founded_text);

    }

    public void displayMode() {
        binding.deleteMyAccountButton.setVisibility(View.VISIBLE);
        binding.editButton.setVisibility(View.VISIBLE);
        binding.saveButton.setVisibility(View.GONE);
        binding.profileImage.setEnabled(false);
    }

    public void uploadPp() {
        if (!UserManager.getInstance().ppUrl.isEmpty()) {
            Picasso.get()
                    .load(UserManager.getInstance().ppUrl)
                    .resize(1024,1024)
                    .onlyScaleDown() // if smaller don't resize
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding.profileImage);
        }
        displayMode();
    }
}
