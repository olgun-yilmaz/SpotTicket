package com.olgunyilmaz.spotticket.view.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.databinding.ActivityOnBoardingBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.service.UserManager;

public class OnBoardingActivity extends AppCompatActivity {
    private ActivityOnBoardingBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        db = FirebaseFirestore.getInstance();

        String email = auth.getCurrentUser().getEmail().toString();

        if (!email.isEmpty()) {
            getFavoriteEvents(email);
            getPp(email);
        }

        binding.nextButton.setEnabled(false);

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null){
                    goToActivity(MainActivity.class);
                }else{
                    goToActivity(EmailPasswordActivity.class);
                }
            }
        });
    }

    private void goToActivity(Class<?> activityClass){
        Intent intent = new Intent(OnBoardingActivity.this,activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void getPp(String email) {
        db.collection("Users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            binding.nextButton.setEnabled(true); // if pp downloaded u can go
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                                String ppUrl = document.getString("profileImageUrl");
                                if (ppUrl != null) {
                                    UserManager.getInstance().ppUrl = ppUrl;
                                }
                            } else {
                                Log.w(TAG, "No documents found.");
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }

                    }
                });
    }

    private void getFavoriteEvents(String userEmail) {
        String path = userEmail + "_Events";
        db.collection(path).orderBy("eventName").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            UserFavoritesManager.getInstance().userFavorites.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String eventID = (String) document.get("eventID");
                                String imageUrl = (String) document.get("imageUrl");
                                String eventName = (String) document.get("eventName");

                                FavoriteEventModel myEventModel = new FavoriteEventModel(eventID, imageUrl, eventName);
                                UserFavoritesManager.getInstance().addFavorite(myEventModel);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


}