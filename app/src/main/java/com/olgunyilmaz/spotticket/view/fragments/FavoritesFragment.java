package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.adapter.FavoriteEventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentFavoritesBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;

import java.util.ArrayList;


public class FavoritesFragment extends Fragment {
    FragmentFavoritesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<FavoriteEventModel> myEventList;

    private FavoriteEventAdapter favoriteEventAdapter;


    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");


        binding.myEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myEventList = new ArrayList<>();

        String userEmail = auth.getCurrentUser().getEmail().toString();

        getEvents(userEmail);


    }

    private void getEvents(String userEmail) {
        String path = userEmail + "_Events";
        db.collection(path).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //myEventList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String eventID = (String) document.get("eventID");
                                String imageUrl = (String) document.get("imageUrl");
                                String eventName = (String) document.get("eventName");

                                FavoriteEventModel myEventModel = new FavoriteEventModel(eventID, imageUrl, eventName);
                                myEventList.add(myEventModel);
                            }
                            favoriteEventAdapter = new FavoriteEventAdapter(myEventList);
                            binding.myEventsRecyclerView.setAdapter(favoriteEventAdapter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        favoriteEventAdapter.notifyDataSetChanged();
                    }
                });

    }

}