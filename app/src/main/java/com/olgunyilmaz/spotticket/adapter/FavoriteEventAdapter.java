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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.R;

import com.olgunyilmaz.spotticket.databinding.FavoriteRowBinding;
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
        FavoriteRowBinding binding =FavoriteRowBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new FavoriteEventHolder(binding);
    }

    @Override
    public void onBindViewHolder(FavoriteEventHolder holder, int position) {
        MainActivity activity = (MainActivity) holder.itemView.getContext();

        FavoriteEventModel favoriteEvent = favoriteEventList.get(position);
        holder.binding.favoriteEventName.setText(favoriteEvent.getEventName());

        if (!favoriteEvent.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(favoriteEvent.getImageUrl())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(holder.binding.favoriteEventImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsFragment fragment = new EventDetailsFragment();

                Bundle args = new Bundle();

                args.putString(activity.getString(R.string.event_id_key), favoriteEvent.getEventId());
                args.putString(activity.getString(R.string.image_url_key), favoriteEvent.getImageUrl());
                args.putString(activity.getString(R.string.event_name_key), favoriteEvent.getEventName());
                args.putString(activity.getString(R.string.event_date_key), favoriteEvent.getDate());

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
        if(favoriteEventList == null){
            return 0;
        }
        return favoriteEventList.size();
    }

    public class FavoriteEventHolder extends RecyclerView.ViewHolder {
        private FavoriteRowBinding binding;

        public FavoriteEventHolder(FavoriteRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}