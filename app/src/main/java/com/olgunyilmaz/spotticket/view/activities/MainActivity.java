package com.olgunyilmaz.spotticket.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.service.UserManager;
import com.olgunyilmaz.spotticket.view.fragments.DisplayFragment;
import com.olgunyilmaz.spotticket.view.fragments.FavoritesFragment;
import com.olgunyilmaz.spotticket.view.fragments.ProfileFragment;
import com.olgunyilmaz.spotticket.view.fragments.HomePageFragment;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
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

        binding.displayButton.setOnClickListener(v -> replaceFragment(new DisplayFragment()));

        binding.homeButton.setOnClickListener(v-> replaceFragment(new HomePageFragment()));

        binding.myEventsButton.setOnClickListener(v -> replaceFragment(new FavoritesFragment()));

    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    public void signOut(View view) {
        auth.signOut();
        goToLoginActivity();

        UserManager.getInstance().ppUrl = ""; // clean for next user
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, EmailPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goToProfileScreen(View view) {
        replaceFragment(new ProfileFragment());
    }

}