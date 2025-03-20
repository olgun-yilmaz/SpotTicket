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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.ReAuthenticateDialogFragment;
import com.olgunyilmaz.spotticket.view.fragments.SelectCityFragment;
import com.olgunyilmaz.spotticket.view.fragments.SettingsFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileHelper implements SelectCityFragment.CitySelectListener {
    private final MainActivity activity;
    private final FragmentProfileBinding binding;
    private final Fragment fragment;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;


    public ProfileHelper(MainActivity activity, FragmentProfileBinding binding, Fragment fragment) {
        this.activity = activity;
        this.binding = binding;
        this.fragment = fragment;
        ArrayList<String> cities = new HomePageHelper(activity, null).getCities();
        binding.cityButton.setOnClickListener(v -> showCityPicker(cities));
    }

    public void goBackToSettings() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.fragmentContainerView, fragment).commit();
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
        binding.cityButton.setEnabled(false);
    }

    public void uploadPp() {
        if (!UserManager.getInstance().ppUrl.isEmpty()) {
            Picasso.get()
                    .load(UserManager.getInstance().ppUrl)
                    .resize(1024, 1024)
                    .onlyScaleDown() // if smaller don't resize
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding.profileImage);
        }
        displayMode();
    }

    private void showCityPicker(ArrayList<String> cities) {
        SelectCityFragment fragment = new SelectCityFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(activity.getString(R.string.cities_key), cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(activity.getSupportFragmentManager(), activity.getString(R.string.city_picker_tag));
    }

    @Override
    public void onCitySelected(String city) {
        binding.cityButton.setText(city);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = () -> {
            if (fragment.isAdded()){
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.cityButton.setText(String.format("%s%s", activity.getString(R.string.please_wait_text), numPointText));
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    public void updateCityData(FirebaseFirestore db, String city) {
        updateLoadingText();
        db.collection(activity.getString(R.string.users_collection_key))
                .whereEqualTo(activity.getString(R.string.email_key), UserManager.getInstance().email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    String documentId = documentSnapshot.getId();

                    db.collection(activity.getString(R.string.users_collection_key)).document(documentId)
                            .update(activity.getString(R.string.city_key), city)
                            .addOnSuccessListener(aVoid -> {
                                UserManager.getInstance().city = city;
                                handler.removeCallbacks(runnable);
                                binding.cityButton.setText(city);
                                Toast.makeText(activity, activity.getString(R.string.successfully_city_updated_text), Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e ->
                                    Toast.makeText(activity, activity.getString(R.string.error_text), Toast.LENGTH_SHORT).show());
                });
    }
}
