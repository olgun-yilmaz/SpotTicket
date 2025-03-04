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


package com.olgunyilmaz.spotticket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.CategoryRowBinding;
import com.olgunyilmaz.spotticket.model.CategoryResponse;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.DisplayFragment;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private ArrayList<CategoryResponse> categories;
    private int itemWidth;

    public CategoryAdapter(ArrayList<CategoryResponse> categories, int itemWidth) {
        this.categories = categories;
        this.itemWidth = itemWidth;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CategoryRowBinding binding = CategoryRowBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);

        ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
        params.width = itemWidth/2;
        params.height = itemWidth/2;
        binding.getRoot().setLayoutParams(params);
        
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryResponse category = categories.get(position);
        holder.binding.categoryImage.setImageResource(category.getImageID());
        holder.binding.categoryName.setText(category.getCategoryName());

        holder.binding.categoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayFragment displayFragment = new DisplayFragment();

                MainActivity activity = (MainActivity) holder.itemView.getContext();
                activity.binding.homeButton.setEnabled(true); // for back to home page
                activity.binding.displayButton.setEnabled(false); // we're here

                LocalDataManager localDataManager = new LocalDataManager(activity);

                String categoryKey = holder.itemView.getContext().getString(R.string.category_key);
                localDataManager.updateStringData(categoryKey,category.getCategoryName());

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, displayFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private CategoryRowBinding binding;

        public CategoryViewHolder(CategoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
