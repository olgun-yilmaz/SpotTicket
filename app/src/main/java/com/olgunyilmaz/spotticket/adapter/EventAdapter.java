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

import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.databinding.EventRowBinding;
import com.olgunyilmaz.spotticket.util.EventDetailsHelper;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.EventDetailsFragment;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventResponse.Event> eventList;

    public EventAdapter(List<EventResponse.Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EventRowBinding binding = EventRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        MainActivity activity = (MainActivity) holder.itemView.getContext();

        EventResponse.Event event = eventList.get(position);
        holder.binding.eventName.setText(event.getName());

        EventDetailsHelper helper = new EventDetailsHelper(activity);
        String formattedDate = helper.getFormattedDate(event.getDates().getStart().getDateTime());
        holder.binding.eventDate.setText(formattedDate);

        if(event.getHighQualityImage() != null){
            Picasso.get()
                    .load(event.getHighQualityImage())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(holder.binding.eventImage);
        }

        holder.binding.bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsFragment fragment = new EventDetailsFragment();
                Bundle args = new Bundle();
                args.putString(activity.getString(R.string.event_id_key), event.getId());
                args.putString(activity.getString(R.string.image_url_key), event.getHighQualityImage());
                args.putString(activity.getString(R.string.event_name_key), event.getName());
                args.putString(activity.getString(R.string.event_date_key),event.getDates().getStart().getDateTime());

                String category = helper.getEventSegmentInfo(event,event.getClassifications());

                args.putLong(activity.getString(R.string.category_icon_key),helper.getCategoryIconId(category));
                fragment.setArguments(args);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private final EventRowBinding binding;

        public EventViewHolder(EventRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

