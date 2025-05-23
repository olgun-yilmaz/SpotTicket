/*
 * Project: EventBox
 * Copyright 2025 Olgun Yılmaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olgunyilmaz.eventbox.view.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.eventbox.util.UserFavoritesManager;
import com.olgunyilmaz.eventbox.adapter.FavoriteEventAdapter;
import com.olgunyilmaz.eventbox.databinding.FragmentFavoritesBinding;
import com.olgunyilmaz.eventbox.model.FavoriteEventModel;

import java.util.ArrayList;


public class FavoritesFragment extends Fragment {
    FragmentFavoritesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.myEventsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        displayEvents();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayEvents() {
        ArrayList<FavoriteEventModel> favorites = UserFavoritesManager.getInstance().userFavorites;

        FavoriteEventAdapter favoriteEventAdapter = new FavoriteEventAdapter(favorites);
        binding.myEventsRecyclerView.setAdapter(favoriteEventAdapter);
        favoriteEventAdapter.notifyDataSetChanged();
    }

}