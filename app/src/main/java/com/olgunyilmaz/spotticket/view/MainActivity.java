package com.olgunyilmaz.spotticket.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.olgunyilmaz.ChangeCityFragment;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;

    public static String BASE_URL;
    public static String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        API_KEY = getString(R.string.api_key);
        BASE_URL = getString(R.string.base_url);

        fragmentManager = getSupportFragmentManager();
        sharedPreferences = getSharedPreferences("com.olgunyilmaz.spotticket.view", MODE_PRIVATE);

        getSupportActionBar().setTitle(sharedPreferences.getString("city","Ankara"));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.city_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String city = "Miami";
        if(item.getItemId() == R.id.tokat){
            city = "Tokat";
        } else if (item.getItemId() == R.id.istanbul) {
            city = "İstanbul";
        }else if (item.getItemId() == R.id.berlin) {
            city = "Berlin";
        }else if (item.getItemId() == R.id.london) {
            city = "London";
        }else if (item.getItemId() == R.id.ankara) {
            city = "Ankara";
        }else if (item.getItemId() == R.id.new_york) {
            city = "New York";
        }

        sharedPreferences.edit().putString("city",city).apply();
        getSupportActionBar().setTitle(city);

        ChangeCityFragment changeCityFragment = new ChangeCityFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView,changeCityFragment).commit();

        return super.onOptionsItemSelected(item);
    }
}