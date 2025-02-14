package com.olgunyilmaz.spotticket.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private ActivityResultLauncher <String> permissionLauncher;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private SharedPreferences sharedPreferences;
    private Boolean isFirsTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.olgunyilmaz.spotticket.view",MODE_PRIVATE);
        isFirsTime = sharedPreferences.getBoolean("isFirstTime",true);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (isFirsTime){
                    goToLocation(location.getLatitude(),location.getLongitude(),"mevcut konum",15);
                    sharedPreferences.edit().putBoolean("isFirstTime",false).apply();
                }

            }
        };

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }


    private void requestPermission(String permission){
        if (ContextCompat.checkSelfPermission(MapsActivity.this,permission) !=
        PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,permission)){
                Snackbar.make(binding.getRoot(),"Harita için izin gerekli!",Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                permissionLauncher.launch(permission);
                            }
                        }).show();
            }else{
                permissionLauncher.launch(permission);
            }

        }else{
            listenLocation();
            goToLastLocation();
        }
    }

    private void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED){
                        listenLocation();
                    }

                }else{
                    Toast.makeText(MapsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void goToLastLocation(){
        Location lastLocation =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation != null){
            goToLocation(lastLocation.getLatitude(),lastLocation.getLongitude(),"last location",17);
        }

        mMap.setMyLocationEnabled(true);
    }

    private void goToLocation(double latitude, double longitude, String title, int zoom){
        LatLng location = new LatLng(latitude,longitude);
        //mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,zoom));
    }

    @SuppressLint("MissingPermission")
    private void listenLocation(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,100,locationListener);
    }

}