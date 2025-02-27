package com.olgunyilmaz.spotticket.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.adapter.CategoryAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentSelectCategoryBinding;
import com.olgunyilmaz.spotticket.model.CategoryResponse;

import java.util.ArrayList;


public class SelectCategoryFragment extends Fragment {
    private FragmentSelectCategoryBinding binding;
    private CategoryAdapter categoryAdapter;
    private ArrayList<CategoryResponse> categories;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelectCategoryBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        categories = new ArrayList<>();
        getCategories();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager
                (requireContext(), 3, GridLayoutManager.HORIZONTAL, false);

        binding.categoryRecyclerView.setLayoutManager(gridLayoutManager);

        binding.categoryRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.categoryRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = binding.categoryRecyclerView.getWidth();
                int itemWidth = width / 3;

                categoryAdapter = new CategoryAdapter(categories, itemWidth);
                binding.categoryRecyclerView.setAdapter(categoryAdapter);
            }
        });
    }

    private void getCategories() {
        categories.add(new CategoryResponse(R.drawable.family, "Aile"));
        categories.add(new CategoryResponse(R.drawable.ballet, "Bale"));
        categories.add(new CategoryResponse(R.drawable.basketball, "Basketbol"));
        categories.add(new CategoryResponse(R.drawable.baseball, "Beyzbol"));
        categories.add(new CategoryResponse(R.drawable.blues, "Blues"));
        categories.add(new CategoryResponse(R.drawable.ice_show, "Buz Gösterileri"));
        categories.add(new CategoryResponse(R.drawable.dancing, "Dans"));
        categories.add(new CategoryResponse(R.drawable.fest, "Festivaller"));
        categories.add(new CategoryResponse(R.drawable.movie, "Film"));

        categories.add(new CategoryResponse(R.drawable.movie_fest, "Film Festivalleri"));
        categories.add(new CategoryResponse(R.drawable.folk, "Folk"));
        categories.add(new CategoryResponse(R.drawable.soccer, "Futbol"));
        categories.add(new CategoryResponse(R.drawable.esports, "E-Spor"));
        categories.add(new CategoryResponse(R.drawable.hip_hop, "Hip-Hop/Rap"));
        categories.add(new CategoryResponse(R.drawable.jazz, "Jazz"));
        categories.add(new CategoryResponse(R.drawable.comedy, "Komedi"));
        categories.add(new CategoryResponse(R.drawable.cultural, "Kültür Festivali"));
        categories.add(new CategoryResponse(R.drawable.mma, "MMA/Dövüş Sporları"));

        categories.add(new CategoryResponse(R.drawable.motosports, "Motorsporları"));
        categories.add(new CategoryResponse(R.drawable.musical, "Müzikal"));
        categories.add(new CategoryResponse(R.drawable.opera, "Opera"));
        categories.add(new CategoryResponse(R.drawable.rock, "Rock"));
        categories.add(new CategoryResponse(R.drawable.pop_music, "Pop"));
        categories.add(new CategoryResponse(R.drawable.circus, "Sirk"));
        categories.add(new CategoryResponse(R.drawable.tennis, "Tenis"));
        categories.add(new CategoryResponse(R.drawable.theater, "Tiyatro"));
        categories.add(new CategoryResponse(R.drawable.food, "Yemek Festivali"));

    }
}