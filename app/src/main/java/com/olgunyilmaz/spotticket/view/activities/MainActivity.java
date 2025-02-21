package com.olgunyilmaz.spotticket.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;
import com.olgunyilmaz.spotticket.view.fragments.ChangeCityFragment;
import com.olgunyilmaz.spotticket.view.fragments.ProfileFragment;



public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;

    public static String TICKETMASTER_BASE_URL;
    public static String TICKETMASTER_API_KEY;
    public static String MAPS_BASE_URL;
    public static String MAPS_API_KEY;

    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TICKETMASTER_BASE_URL = getString(R.string.ticketmaster_base_url);
        TICKETMASTER_API_KEY = getString(R.string.ticketmaster_api_key);

        MAPS_BASE_URL = getString(R.string.maps_base_url);
        MAPS_API_KEY = getString(R.string.maps_api_key);

        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        fragmentManager = getSupportFragmentManager();
        sharedPreferences = getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProfileScreen();
            }
        });

        binding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new ChangeCityFragment());
            }
        });

        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    private void signOut(){
        auth.signOut();
        goToLoginActivity();
    }

    private void goToLoginActivity(){
        Intent intent = new Intent(MainActivity.this, EmailPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void goToProfileScreen(){
        ProfileFragment profileFragment = new ProfileFragment();
        replaceFragment(profileFragment);
    }

}