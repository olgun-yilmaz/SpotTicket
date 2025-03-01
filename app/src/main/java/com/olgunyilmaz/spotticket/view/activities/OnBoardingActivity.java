package com.olgunyilmaz.spotticket.view.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityOnBoardingBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.service.UserManager;

public class OnBoardingActivity extends AppCompatActivity {
    private ActivityOnBoardingBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        boolean isFromLogin = getIntent().getBooleanExtra("fromLogin",false);

        if(isFromLogin){
            String email = getIntent().getStringExtra("userEmail");
            //binding.imageView.setImageResource(R.drawable.loading); // will use a diff background
            downloadData(email);
        }

        boolean isRemember = sharedPreferences.getBoolean("rememberMe",false);

        if (isRemember){
            currentUser = auth.getCurrentUser();
        }

        if (currentUser != null) {

            if (currentUser.getEmail() != null) {

                String email = currentUser.getEmail().toString();

                if (!email.isEmpty()) {
                    downloadData(email);
                }

            }
        }

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentUser != null) {
                    goToActivity(MainActivity.class);
                    finish(); // if user in app, won't back by intent
                } else {
                    goToActivity(EmailPasswordActivity.class);
                }
            }
        });
    }

    private void downloadData(String email){
        binding.nextButton.setEnabled(false);
        getPp(email);
        getFavoriteEvents(email);
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = ". ".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.getStartText.setText("Yükleniyor " + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(OnBoardingActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void getPp(String email) {
        db.collection("Users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
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
        updateLoadingText();
        String path = userEmail + "_Events";
        db.collection(path).orderBy("eventName").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String msg;
                        if (task.isSuccessful()) {
                            if (UserFavoritesManager.getInstance().userFavorites != null){
                                UserFavoritesManager.getInstance().userFavorites.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String eventID = (String) document.get("eventID");
                                String imageUrl = (String) document.get("imageUrl");
                                String eventName = (String) document.get("eventName");

                                FavoriteEventModel myEventModel = new FavoriteEventModel(eventID, imageUrl, eventName);
                                UserFavoritesManager.getInstance().addFavorite(myEventModel);
                            }
                            msg = "Haydi Başlayalım !";
                            binding.nextButton.setEnabled(true); // if downloaded u can go

                        } else {
                            msg = "Bir Sorun Oluştu :/";
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        binding.getStartText.setText(msg);
                        handler.removeCallbacks(runnable);
                    }
                });
    }


}