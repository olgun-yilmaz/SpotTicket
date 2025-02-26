package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentSelectCategoryBinding;


public class SelectCategoryFragment extends Fragment {
    private FragmentSelectCategoryBinding binding;
    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelectCategoryBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);
        fragmentManager = getActivity().getSupportFragmentManager();

        binding.basketballImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Basketbol").apply();
                goToDisplayScreen();

            }
        });
        binding.carnavalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Festivaller").apply();
                goToDisplayScreen();

            }
        });
        binding.danceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Dans").apply();
                goToDisplayScreen();

            }
        });
        binding.esportsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","E-Spor").apply();
                goToDisplayScreen();

            }
        });
        binding.familyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Aile").apply();
                goToDisplayScreen();

            }
        });

        binding.foodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Yemek Festivali").apply();
                goToDisplayScreen();

            }
        });
        binding.rockImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Rock").apply();
                goToDisplayScreen();

            }
        });
        binding.soccerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Futbol").apply();
                goToDisplayScreen();

            }
        });
        binding.theaterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("category","Tiyatro").apply();
                goToDisplayScreen();

            }
        });
    }

    private void goToDisplayScreen(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DisplayFragment displayFragment = new DisplayFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView, displayFragment).commit();
    }
}