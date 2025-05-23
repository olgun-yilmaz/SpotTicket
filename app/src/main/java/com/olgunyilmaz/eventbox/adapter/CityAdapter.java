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

package com.olgunyilmaz.eventbox.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.eventbox.databinding.CityRowBinding;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder> {

    private final ArrayList<String> allCities;
    private final ArrayList<String> filteredCities;
    private final OnCityClickListener listener;

    public CityAdapter(ArrayList<String> cities, OnCityClickListener listener) {
        this.allCities = new ArrayList<>(cities);
        this.filteredCities = new ArrayList<>(cities);
        this.listener = listener;
    }

    public interface OnCityClickListener {
        void onCityClick(String city);
    }

    @NonNull
    @Override
    public CityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CityRowBinding binding = CityRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CityHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CityHolder holder, int position) {
        String city = filteredCities.get(position);
        holder.binding.cityNameTextView.setText(city);
        holder.binding.cityNameTextView.setOnClickListener(v -> listener.onCityClick(city));

    }

    @Override
    public int getItemCount() {
        return filteredCities.size();
    }

    public static class CityHolder extends RecyclerView.ViewHolder {
        private final CityRowBinding binding;

        public CityHolder(CityRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        filteredCities.clear();
        if (query.isEmpty()) {
            filteredCities.addAll(allCities);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String city : allCities) {
                if (city.toLowerCase().contains(lowerCaseQuery)) {
                    filteredCities.add(city);
                }
            }
        }
        notifyDataSetChanged();
    }
}
