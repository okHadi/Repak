package com.repak.repak;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;

import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
public class AddCrimeMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {
    private LatLng mLatLng;
    private Button saveLocation ;
    private GoogleMap mMap;
    private Marker mMarker;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crime_map);
        saveLocation = findViewById(R.id.saveLocationButton);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        saveLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (mLatLng != null) {
                    // Save the coordinates here
                    double latitude = mLatLng.latitude;
                    double longitude = mLatLng.longitude;
                    Intent intent = new Intent(AddCrimeMap.this, CrimeDetails.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    // Start the next activity with the intent
                    startActivity(intent);

                } else {
                    Log.d("TAG", "Location not set.");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // enable dragging of the marker by setting the draggable property to true
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mMap.setOnCameraMoveListener(this);

        // move the camera to the user's current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }


    @Override
    public void onCameraMove() {
        mLatLng = mMap.getCameraPosition().target;
        mMarker.setPosition(mLatLng);
    }

}

