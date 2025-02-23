package com.olgunyilmaz.spotticket.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.adapter.FavoriteEventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentFavoritesBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;

import java.util.ArrayList;


public class FavoritesFragment extends Fragment {
    FragmentFavoritesBinding binding;
    private FavoriteEventAdapter favoriteEventAdapter;


    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.myEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        displayEvents();


    }

    private void displayEvents() {
        ArrayList<FavoriteEventModel> favorites = UserFavoritesManager.getInstance().userFavorites;
        
        favoriteEventAdapter = new FavoriteEventAdapter(favorites);
        binding.myEventsRecyclerView.setAdapter(favoriteEventAdapter);
        favoriteEventAdapter.notifyDataSetChanged();
    }

}