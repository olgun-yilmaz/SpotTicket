package com.olgunyilmaz.spotticket.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.CategoryRowBinding;
import com.olgunyilmaz.spotticket.model.CategoryResponse;
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
        params.width = itemWidth;
        params.height = itemWidth;
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

                SharedPreferences sharedPreferences = ((MainActivity) holder.itemView.getContext())
                        .getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

                sharedPreferences.edit().putString("category",category.getCategoryName()).apply();

                ((MainActivity) holder.itemView.getContext())
                        .getSupportFragmentManager()
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
