package com.olgunyilmaz.spotticket.view.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    SharedPreferences sharedPreferences;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayMode();

        registerLauncher();

        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        binding.emailText.setText(user.getDisplayName());
        binding.usernameText.setText(user.getEmail());
        binding.cityText.setText("Şehir : " + sharedPreferences.getString("city", "ankara")); // will be update

        binding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit();
            }
        });

    }

    private void edit() {
        editMode();
        binding.cityText.setText("Şehir : ");

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(binding.editTextCity.getText().toString());
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfileImage(view);
            }
        });

    }

    private void save(String city) {
        String msg;
        displayMode();
        if (!city.isEmpty()) {
            sharedPreferences.edit().putString("city", city).apply();
            msg = "Şehriniz başarıyla değiştirildi!";
        } else {
            msg = "Lütfen şehir ismini doğru girdiğinizden emin olun!";
        }

        binding.cityText.setText("Şehir : " + sharedPreferences.getString("city", "ankara"));
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

    }

    private void displayMode() {
        binding.editButton.setVisibility(View.VISIBLE);
        binding.editTextCity.setVisibility(View.GONE);
        binding.saveButton.setVisibility(View.GONE);

        binding.profileImage.setEnabled(false);
    }

    private void editMode() {
        binding.editButton.setVisibility(View.GONE);
        binding.editTextCity.setVisibility(View.VISIBLE);
        binding.saveButton.setVisibility(View.VISIBLE);

        binding.profileImage.setEnabled(true);
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
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
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
                        Uri imgData = intentFromResult.getData();
                        binding.profileImage.setImageURI(imgData);

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
                    Toast.makeText(getActivity(), "Permission needed!", Toast.LENGTH_LONG).show();
                }


            }
        });
    }

}