package com.olgunyilmaz.spotticket.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olgunyilmaz.spotticket.databinding.EventItemBinding;
import com.olgunyilmaz.spotticket.model.MyEventModel;
import com.olgunyilmaz.spotticket.view.activities.EventDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.MyEventHolder> {

    ArrayList<MyEventModel> myEventList;

    public MyEventAdapter(ArrayList<MyEventModel> myEventList) {
        this.myEventList = myEventList;
    }

    @NonNull
    @Override
    public MyEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EventItemBinding itemBinding = EventItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new MyEventHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(MyEventAdapter.MyEventHolder holder, int position) {
        MyEventModel myEvent = myEventList.get(position);
        holder.binding.eventName.setText(myEvent.getEventName());

        if (!myEvent.getImageUrl().isEmpty()) {
            Picasso.get().load(myEvent.getImageUrl()).into(holder.binding.eventImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), EventDetailsActivity.class);
                intent.putExtra("eventID",myEvent.getEventId());
                intent.putExtra("imageUrl",myEvent.getImageUrl());
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return myEventList.size();
    }

    public class MyEventHolder extends RecyclerView.ViewHolder {
        private EventItemBinding binding;

        public MyEventHolder(EventItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}