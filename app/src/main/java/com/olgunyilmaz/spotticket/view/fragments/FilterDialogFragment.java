/*
 * Project: SpotTicket
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

package com.olgunyilmaz.spotticket.view.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentFilterDialogBinding;
import com.olgunyilmaz.spotticket.util.Categories;
import com.olgunyilmaz.spotticket.util.HomePageHelper;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.util.UserManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class FilterDialogFragment extends DialogFragment implements SelectCityFragment.CitySelectListener {

    private FragmentFilterDialogBinding binding;
    private FilterDialogListener listener;

    public interface FilterDialogListener {
        void onFiltersSelected(String date, String city, String category);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment instanceof FilterDialogListener) {
            listener = (FilterDialogListener) targetFragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.btnDate.setOnClickListener(v -> showDatePicker());

        setupSpinners();

        getData();

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnApplyFilters.setOnClickListener(v -> {
            String selectedDate = binding.btnDate.getText().toString();
            String selectedCity = binding.selectCityButton.getText().toString();
            String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();

            if (listener != null) {
                listener.onFiltersSelected(selectedDate, selectedCity, selectedCategory);
            }
            dismiss();
        });

        return view;
    }

    private void getData(){
        LocalDataManager dataManager = new LocalDataManager(requireActivity());

        binding.selectCityButton.setText(dataManager.getStringData(getString(R.string.city_key), UserManager.getInstance().city));

        binding.btnDate.setText(dataManager.getStringData(getString(R.string.event_date_key),"Seç..."));

        String categoryData = dataManager.getStringData( getString(R.string.category_key), "");

        if (!categoryData.isEmpty()) {
            SpinnerAdapter adapter = binding.spinnerCategory.getAdapter();
            if (adapter != null) {
                int position = -1;

                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).toString().equals(categoryData)) {
                        position = i;
                        break;
                    }
                }

                if (position != -1) {
                    binding.spinnerCategory.setSelection(position);
                }
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    binding.btnDate.setText(getString(R.string.date_text) + " " + selectedDate);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }


    private void setupSpinners() {
        ArrayList<String> cities = new HomePageHelper(requireActivity()).getCities();
        Categories categories = new Categories(requireActivity());
        categories.loadCategories();

        ArrayList<String> categoryList = new ArrayList<>(categories.CATEGORIES.keySet());
        Collections.sort(categoryList);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);

        binding.selectCityButton.setOnClickListener(v -> showCityPicker(cities));
        binding.spinnerCategory.setAdapter(categoryAdapter);
    }

    private void showCityPicker(ArrayList<String> cities) {
        SelectCityFragment fragment = new SelectCityFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.cities_key), cities);

        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(getActivity().getSupportFragmentManager(), getString(R.string.city_picker_tag));
    }

    @Override
    public void onCitySelected(String city) {
        binding.selectCityButton.setText(city);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}