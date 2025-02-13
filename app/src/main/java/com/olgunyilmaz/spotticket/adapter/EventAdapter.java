package com.olgunyilmaz.spotticket.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.EventItemBinding;
import com.olgunyilmaz.spotticket.model.EventResponse;
import com.olgunyilmaz.spotticket.view.EventDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventResponse.Event> eventList;

    public EventAdapter(List<EventResponse.Event> eventList) {
        this.eventList = eventList;
    }

    public void updateData(List<EventResponse.Event> newEvents) {
        this.eventList = newEvents;
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
                Intent intent = new Intent(holder.itemView.getContext(), EventDetailsActivity.class);
                intent.putExtra("eventID",event.getId());
                intent.putExtra("imageUrl",event.getImages().get(0).getUrl());
                holder.itemView.getContext().startActivity(intent);
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

