package com.olgunyilmaz.spotticket.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private List<ImageView> menuButtons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getMenuButtons();

        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        fragmentManager = getSupportFragmentManager();

        binding.homeButton.setEnabled(false); // first page button disabled

        binding.displayButton.setOnClickListener(v -> replaceFragment(new DisplayFragment(), v));

        binding.homeButton.setOnClickListener(v-> replaceFragment(new HomePageFragment(), v));

        binding.myEventsButton.setOnClickListener(v -> replaceFragment(new FavoritesFragment(), v));

    }

    private void getMenuButtons(){
        menuButtons = new ArrayList<>();
        menuButtons.add(binding.profileButton);
        menuButtons.add(binding.myEventsButton);
        menuButtons.add(binding.homeButton);
        menuButtons.add(binding.displayButton);
        menuButtons.add(binding.signOutButton);
    }

    private void replaceFragment(Fragment fragment, View sender) {
        disableButton(sender);
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

    private void disableButton(View selectedButton){ // disable chosen button
        for(ImageView button : menuButtons){
            if (button == selectedButton){
                button.setEnabled(false);
            }else{
                button.setEnabled(true);
            }

        }
    }

    public void goToProfileScreen(View view) {
        replaceFragment(new ProfileFragment(), view);
    }

}