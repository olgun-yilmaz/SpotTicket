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
import com.olgunyilmaz.spotticket.adapter.MyEventAdapter;
import com.olgunyilmaz.spotticket.databinding.FragmentMyEventsBinding;
import com.olgunyilmaz.spotticket.model.MyEventModel;

import java.util.ArrayList;
import java.util.List;


public class MyEventsFragment extends Fragment {
    FragmentMyEventsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<MyEventModel> myEventList;

    private MyEventAdapter myEventAdapter;


    public MyEventsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyEventsBinding.inflate(getLayoutInflater(),container,false);
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

    private void getEvents(String userEmail){
        String path = userEmail+"_Events";
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

                                MyEventModel myEventModel = new MyEventModel(eventID,imageUrl,eventName);
                                myEventList.add(myEventModel);
                            }
                            myEventAdapter = new MyEventAdapter(myEventList);
                            binding.myEventsRecyclerView.setAdapter(myEventAdapter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        myEventAdapter.notifyDataSetChanged();
                    }
                });

    }

}