package com.olgunyilmaz.spotticket.view.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.olgunyilmaz.spotticket.databinding.ActivityEmailPasswordBinding;

public class EmailPasswordActivity extends AppCompatActivity {
    private ActivityEmailPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }
}