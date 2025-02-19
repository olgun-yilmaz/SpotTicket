package com.olgunyilmaz.spotticket.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;
import com.olgunyilmaz.spotticket.view.fragments.ChangeCityFragment;

import java.util.HashMap;
import java.util.Map;


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

        fragmentManager = getSupportFragmentManager();
        sharedPreferences = getSharedPreferences("com.olgunyilmaz.spotticket.view", MODE_PRIVATE);

        getSupportActionBar().setTitle(sharedPreferences.getString("city", "Ankara"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.city_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.exit == item.getItemId()){
            auth.signOut();
            goToLoginActivity();
        }else{
            Map<Integer, String> cityMap = new HashMap<>();
            cityMap.put(R.id.tokat, "Tokat");
            cityMap.put(R.id.istanbul, "İstanbul");
            cityMap.put(R.id.berlin, "Berlin");
            cityMap.put(R.id.london, "London");
            cityMap.put(R.id.ankara, "Ankara");
            cityMap.put(R.id.new_york, "New York");
            cityMap.put(R.id.oslo, "Oslo");

            String city = cityMap.getOrDefault(item.getItemId(), "Miami");

            sharedPreferences.edit().putString("city", city).apply();
            getSupportActionBar().setTitle(city);

            replaceFragment(new ChangeCityFragment());
        }
        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    private void goToLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}