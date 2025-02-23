package com.olgunyilmaz.spotticket.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.view.activities.MainActivity;
import com.olgunyilmaz.spotticket.view.fragments.EventDetailsFragment;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.EventItemBinding;
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
        EventItemBinding binding = EventItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        EventResponse.Event event = eventList.get(position);
        holder.binding.eventName.setText(event.getName());
        if (event.getImages() != null && !event.getImages().isEmpty()) {
            Picasso.get()
                    .load(event.getImages().get(0).getUrl())
                    .into(holder.binding.eventImage);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsFragment fragment = new EventDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventID", event.getId());
                args.putString("imageUrl", event.getImages().get(0).getUrl());
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
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private final EventItemBinding binding;

        public EventViewHolder(EventItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

