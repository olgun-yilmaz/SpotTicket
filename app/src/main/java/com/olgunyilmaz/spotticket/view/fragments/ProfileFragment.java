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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;
import com.olgunyilmaz.spotticket.service.UserManager;
import com.olgunyilmaz.spotticket.util.ImageLoader;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Uri imgUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private FirebaseStorage storage;
    private FirebaseUser user;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        storage = FirebaseStorage.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerLauncher();

        uploadPp();

        displayMode();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        user = FirebaseAuth.getInstance().getCurrentUser();

        binding.emailText.setText(user.getDisplayName());
        binding.usernameText.setText(user.getEmail());
        binding.creationDateText.setText(getCreationDate());

        binding.deleteMyAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        binding.editButton.setOnClickListener(v -> editMode());

    }

    private void showDeleteAccountDialog() {
        if (isAdded() && getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.delete_account_text))
                    .setMessage(getString(R.string.delete_account_question))
                    .setNegativeButton(getString(R.string.answer_no), null)
                    .setPositiveButton(getString(R.string.answer_yes), (dialogInterface, i) -> {
                        ReAuthenticateDialogFragment dialog = new ReAuthenticateDialogFragment();
                        dialog.show(getParentFragmentManager(), getString(R.string.re_authenticate_tag));
                    }).show();
        }
    }

    private String getCreationDate() {
        if (user.getMetadata() != null) {
            long creationTime = user.getMetadata().getCreationTimestamp();
            Date creationDate = new Date(creationTime);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("tr", "TR"));
            return sdf.format(creationDate);
        }
        return getString(R.string.date_not_founded_text);

    }

    private void uploadPp() {
        if (!UserManager.getInstance().ppUrl.isEmpty()) {
            Picasso.get()
                    .load(UserManager.getInstance().ppUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(binding.profileImage);
        }
    }

    private void editMode() {
        binding.editButton.setVisibility(View.GONE);
        binding.deleteMyAccountButton.setVisibility(View.GONE);
        binding.saveButton.setVisibility(View.VISIBLE);
        binding.profileImage.setEnabled(true);

        binding.profileImage.setOnClickListener(this::changeProfileImage);

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMode();
                uploadImage2db();
            }
        });

    }

    private void displayMode() {
        binding.deleteMyAccountButton.setVisibility(View.VISIBLE);
        binding.editButton.setVisibility(View.VISIBLE);
        binding.saveButton.setVisibility(View.GONE);
        binding.profileImage.setEnabled(false);
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
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Profile image updated successfully.");
                                });
                    } else { // add new
                        Map<String, Object> user = new HashMap<>();
                        user.put(getString(R.string.email_key), email);
                        user.put(getString(R.string.profile_image_url_key), ppUrl);

                        db.collection(getString(R.string.users_collection_key)).add(user)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        });
                    }
                });
    }


    private void uploadImage2db() {
        if (imgUri != null) {
            binding.profileImage.setImageResource(R.drawable.loading);
            String dir_name = "pp";
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images").child(dir_name)
                    .child(user.getEmail() + ".jpg");

            ImageLoader imageLoader = new ImageLoader(requireActivity(), imgUri, 500);
            imgUri = imageLoader.getResizedImageUri();

            imageRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), getString(R.string.successfully_image_change_text),
                            Toast.LENGTH_LONG).show();

                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            updateProfileImage(auth.getCurrentUser().getEmail().toString(), uri.toString());
                            UserManager.getInstance().ppUrl = imgUri.toString();
                            binding.profileImage.setImageURI(imgUri);
                            imgUri = null; // reset uri for next
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
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
                        Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.give_permission_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(permission);
                    }
                }).show();
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
                StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        imgUri = intentFromResult.getData();
                        binding.profileImage.setImageURI(imgUri);
                    }

                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {

            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    //permission denied
                    Toast.makeText(getActivity(), getString(R.string.gallery_permission_text), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}