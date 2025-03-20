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

package com.olgunyilmaz.spotticket.view.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;
import com.olgunyilmaz.spotticket.helper.ProfileHelper;
import com.olgunyilmaz.spotticket.util.UserManager;
import com.olgunyilmaz.spotticket.util.ImageLoader;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Uri imgUri;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private FirebaseStorage storage;
    private ProfileHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        storage = FirebaseStorage.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new ProfileHelper((MainActivity) requireActivity(), binding,this);

        registerLauncher();
        helper.uploadPp();

        db = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        String countryKey = getString(R.string.language_code_key);
        String countryCode = new LocalDataManager(requireActivity()).getStringData(countryKey, "tr");

        auth.setLanguageCode(countryCode);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        binding.emailText.setText(UserManager.getInstance().email);
        binding.usernameText.setText(String.format("%s %s", UserManager.getInstance().name, UserManager.getInstance().surname));
        binding.cityButton.setText(UserManager.getInstance().city);
        binding.creationDateText.setText(helper.getCreationDate(countryCode, user));

        binding.deleteMyAccountButton.setOnClickListener(v -> helper.showDeleteAccountDialog());

        binding.editButton.setOnClickListener(v -> editMode());

        binding.profileBackButton.setOnClickListener(v -> helper.goBackToSettings());
    }

    private void editMode() {
        binding.editButton.setVisibility(View.GONE);
        binding.deleteMyAccountButton.setVisibility(View.GONE);
        binding.saveButton.setVisibility(View.VISIBLE);
        binding.profileImage.setEnabled(true);
        binding.cityButton.setEnabled(true);

        binding.profileImage.setOnClickListener(this::changeProfileImage);

        binding.saveButton.setOnClickListener(v -> {
            helper.displayMode();

            String city = binding.cityButton.getText().toString();

            if(!city.equals(UserManager.getInstance().city)){
                helper.updateCityData(db,city);
            }

            uploadImage2db();
        });

    }

    private void updateProfileImage(String email, String ppUrl) {
        db.collection(getString(R.string.users_collection_key))
                .whereEqualTo(getString(R.string.email_key), email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) { // update
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        db.collection(getString(R.string.users_collection_key)).document(documentId)
                                .update(getString(R.string.profile_image_url_key), ppUrl)
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Profile image updated successfully."));
                    } else { // add new
                        Map<String, Object> user = new HashMap<>();
                        user.put(getString(R.string.email_key), email);
                        user.put(getString(R.string.profile_image_url_key), ppUrl);

                        db.collection(getString(R.string.users_collection_key)).add(user)
                                .addOnSuccessListener(documentReference ->
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()));
                    }
                });
    }


    private void uploadImage2db() {
        if (imgUri != null) {
            binding.profileImage.setImageResource(R.drawable.loading);
            String dir_name = "pp";
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images").child(dir_name).child(UserManager.getInstance().email + ".jpg");

            ImageLoader imageLoader = new ImageLoader(requireActivity(), imgUri, 500);
            imgUri = imageLoader.getResizedImageUri();

            imageRef.putFile(imgUri).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(getContext(), getString(R.string.successfully_image_change_text),
                        Toast.LENGTH_LONG).show();

                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateProfileImage(UserManager.getInstance().email, uri.toString());
                    binding.profileImage.setImageBitmap(UserManager.getInstance().profileImage);
                    imgUri = null; // reset uri for next
                });

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void changeProfileImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermission(view, android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            askPermission(view, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void askPermission(View view, String permission) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
                Snackbar.make(view, getString(R.string.gallery_permission_text),
                        Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.give_permission_text),
                        v -> permissionLauncher.launch(permission)).show();
            } else {
                permissionLauncher.launch(permission);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.
                StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intentFromResult = result.getData();
                        if (intentFromResult != null) {
                            imgUri = intentFromResult.getData();
                            binding.profileImage.setImageURI(imgUri);
                        }

                    }
                });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                //permission granted
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            } else {
                //permission denied
                Toast.makeText(getActivity(), getString(R.string.gallery_permission_text), Toast.LENGTH_LONG).show();
            }
        });
    }

}