package com.olgunyilmaz.spotticket.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.databinding.ActivityOnBoardingBinding;

public class OnBoardingActivity extends AppCompatActivity {
    private ActivityOnBoardingBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null){
                    goToActivity(MainActivity.class);
                }else{
                    goToActivity(LoginActivity.class);
                }
            }
        });
    }

    private void goToActivity(Class<?> activityClass){
        Intent intent = new Intent(OnBoardingActivity.this,activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


}