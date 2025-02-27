package com.olgunyilmaz.spotticket.view.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olgunyilmaz.spotticket.adapter.CityAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentSelectCityBinding;

import java.util.ArrayList;


public class SelectCityFragment extends DialogFragment {

    public interface CitySelectListener {
        void onCitySelected(String city);
    }

    private FragmentSelectCityBinding binding;
    private CitySelectListener listener;
    private CityAdapter adapter;

    public void setListener(CitySelectListener listener) {
        this.listener = listener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelectCityBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.citiesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Bundle args = getArguments();
        if (args != null) {
            ArrayList<String> cities = args.getStringArrayList("cities");
            adapter = new CityAdapter(cities, new CityAdapter.OnCityClickListener() {
                @Override
                public void onCityClick(String city) {
                    if (listener != null) {
                        listener.onCitySelected(city);
                    }
                    dismiss();
                }
            });
            binding.citiesRecyclerView.setAdapter(adapter);

            binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);
                    return false;
                }
            });
        }
    }

}