package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    SharedPreferences sharedPreferences;


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

        sharedPreferences = getActivity().getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        binding.emailText.setText(user.getDisplayName());
        binding.usernameText.setText(user.getEmail());
        binding.cityText.setText("Şehir : " + sharedPreferences.getString("city","ankara")); // will be update

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

    }

    private void save(String city) {
        String msg;
        displayMode();
        if (!city.isEmpty()) {
            sharedPreferences.edit().putString("city",city).apply();
            msg = "Şehriniz başarıyla değiştirildi!";
        }else{
            msg = "Lütfen şehir ismini doğru girdiğinizden emin olun!";
        }

        binding.cityText.setText("Şehir : " + sharedPreferences.getString("city","ankara"));
        Toast.makeText(getContext(),msg,Toast.LENGTH_LONG).show();

    }

    private void displayMode(){
        binding.editButton.setVisibility(View.VISIBLE);
        binding.editTextCity.setVisibility(View.GONE);
        binding.saveButton.setVisibility(View.GONE);
    }

    private void editMode(){
        binding.editButton.setVisibility(View.GONE);
        binding.editTextCity.setVisibility(View.VISIBLE);
        binding.saveButton.setVisibility(View.VISIBLE);
    }

}