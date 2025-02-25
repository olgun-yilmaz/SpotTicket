package com.olgunyilmaz.spotticket.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.databinding.CityRowBinding;

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
        return new CityAdapter.CityHolder(binding);
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

    public class CityHolder extends RecyclerView.ViewHolder {
        private CityRowBinding binding;

        public CityHolder(CityRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

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
