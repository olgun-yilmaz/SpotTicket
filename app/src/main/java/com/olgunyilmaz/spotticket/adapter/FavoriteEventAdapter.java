package com.olgunyilmaz.spotticket.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.R;

import com.olgunyilmaz.spotticket.databinding.EventRowBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.EventDetailsFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavoriteEventAdapter extends RecyclerView.Adapter<FavoriteEventAdapter.FavoriteEventHolder> {

    ArrayList<FavoriteEventModel> favoriteEventList;

    public FavoriteEventAdapter(ArrayList<FavoriteEventModel> favoriteEventList) {
        this.favoriteEventList = favoriteEventList;
    }

    @NonNull
    @Override
    public FavoriteEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EventRowBinding binding = EventRowBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new FavoriteEventHolder(binding);
    }

    @Override
    public void onBindViewHolder(FavoriteEventHolder holder, int position) {
        FavoriteEventModel favoriteEvent = favoriteEventList.get(position);
        holder.binding.eventName.setText(favoriteEvent.getEventName());

        if (!favoriteEvent.getImageUrl().isEmpty()) {
            Picasso.get().load(favoriteEvent.getImageUrl()).into(holder.binding.eventImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsFragment fragment = new EventDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventID", favoriteEvent.getEventId());
                args.putString("imageUrl",favoriteEvent.getImageUrl());
                fragment.setArguments(args);

                ((MainActivity) holder.itemView.getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return favoriteEventList.size();
    }

    public class FavoriteEventHolder extends RecyclerView.ViewHolder {
        private EventRowBinding binding;

        public FavoriteEventHolder(EventRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}