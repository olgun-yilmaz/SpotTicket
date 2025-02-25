package com.olgunyilmaz.spotticket.view.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.service.UserFavoritesManager;
import com.olgunyilmaz.spotticket.databinding.ActivityMainBinding;
import com.olgunyilmaz.spotticket.model.FavoriteEventModel;
import com.olgunyilmaz.spotticket.service.UserManager;
import com.olgunyilmaz.spotticket.view.fragments.DisplayFragment;
import com.olgunyilmaz.spotticket.view.fragments.FavoritesFragment;
import com.olgunyilmaz.spotticket.view.fragments.ProfileFragment;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseFirestore db;

    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;

    public static String TICKETMASTER_BASE_URL;
    public static String TICKETMASTER_API_KEY;
    public static String MAPS_BASE_URL;
    public static String MAPS_API_KEY;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TICKETMASTER_BASE_URL = getString(R.string.ticketmaster_base_url);
        TICKETMASTER_API_KEY = getString(R.string.ticketmaster_api_key);

        MAPS_BASE_URL = getString(R.string.maps_base_url);
        MAPS_API_KEY = getString(R.string.maps_api_key);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        String email = auth.getCurrentUser().getEmail().toString();

        if (!email.isEmpty()) {
            getFavoriteEvents(email);
            getPp(email);
        }

        fragmentManager = getSupportFragmentManager();
        sharedPreferences = getSharedPreferences("com.olgunyilmaz.spotticket", MODE_PRIVATE);

        binding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new DisplayFragment());
            }
        });


        binding.myEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new FavoritesFragment());
            }
        });

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

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit();
    }

    public void signOut(View view) {
        auth.signOut();
        goToLoginActivity();

        UserManager.getInstance().ppUrl = ""; // clean for next user
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, EmailPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goToProfileScreen(View view) {
        ProfileFragment profileFragment = new ProfileFragment();
        replaceFragment(profileFragment);
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