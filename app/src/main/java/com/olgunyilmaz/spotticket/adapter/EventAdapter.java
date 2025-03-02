package com.olgunyilmaz.spotticket.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.databinding.EventRowBinding;
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
        EventResponse.Event event = eventList.get(position);
        holder.binding.eventName.setText(event.getName());

        if(event.getHighQualityImage() != null){
            Picasso.get()
                    .load(event.getHighQualityImage())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(holder.binding.eventImage);
        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsFragment fragment = new EventDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventID", event.getId());
                args.putString("imageUrl", event.getImages().get(0).getUrl()); // get first(selected) image
                args.putString("imageUrl", event.getImages().get(0).getUrl()); // get first(selected) image
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
        private final EventRowBinding binding;

        public EventViewHolder(EventRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

