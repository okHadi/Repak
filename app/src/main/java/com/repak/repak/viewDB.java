package com.repak.repak;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class viewDB extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private LatLng currentLatLng;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_db);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the fused location provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // If permission is granted, get the current location
            getCurrentLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // Set marker click listener
            mMap.setOnMarkerClickListener(marker -> {
                Object tag = marker.getTag();
                if (tag instanceof QueryDocumentSnapshot) {
                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) tag;
                    showMarkerDetails(document, marker);
                }
                return false;
            });

            // Dismiss the popup window when map is clicked
            mMap.setOnMapClickListener(latLng -> {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            });

            // Fetch the coordinates from Firestore
            db.collection("crimes")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Iterate over the query snapshot and add markers for each coordinate
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LatLng latLng = new LatLng(
                                        document.getDouble("latitude"),
                                        document.getDouble("longitude")
                                );

                                // Create a custom marker icon
                                BitmapDescriptor defaultMarkerIcon = BitmapDescriptorFactory.defaultMarker();
                                Bitmap resizedBitmap = resizeMarkerIcon(defaultMarkerIcon, R.dimen.custom_marker_width, R.dimen.custom_marker_height);
                                BitmapDescriptor customMarkerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

                                // Add the custom marker to the map
                                mMap.addMarker(new MarkerOptions().position(latLng).icon(customMarkerIcon)).setTag(document);
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                   // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
                } else {
                    Log.d("Location", "Error getting current location");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Log.d("Location", "Location permission denied");
            }
        }
    }

    private void showMarkerDetails(QueryDocumentSnapshot document, Marker marker) {
        String date = document.getString("date");
        String details = document.getString("details");
        String type = document.getString("type");

        String content = "Type of Crime: " + type + "\n" +
                "Date of Crime: " + date + "\n" +
                "Details: " + details;

        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.marker_details_card, null);

        TextView dateTextView = cardView.findViewById(R.id.dateTextView);
        TextView detailsTextView = cardView.findViewById(R.id.detailsTextView);
        TextView typeTextView = cardView.findViewById(R.id.typeTextView);
        dateTextView.setText(content);

        popupWindow = new PopupWindow(cardView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setFocusable(true);

        Projection projection = mMap.getProjection();
        Point markerScreenPosition = projection.toScreenLocation(marker.getPosition());
        int x = markerScreenPosition.x;
        int y = markerScreenPosition.y;

        int markerHeight = getResources().getDimensionPixelSize(R.dimen.custom_marker_height);

        popupWindow.showAtLocation(cardView, Gravity.BOTTOM, 0, 0);

        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = 0.7f;
        window.setAttributes(layoutParams);

        popupWindow.setOnDismissListener(() -> {
            layoutParams.alpha = 1.0f;
            window.setAttributes(layoutParams);
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(viewDB.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Bitmap resizeMarkerIcon(BitmapDescriptor icon, int widthRes, int heightRes) {
        int width = (int) getResources().getDimension(widthRes);
        int height = (int) getResources().getDimension(heightRes);

        // Get the resource ID of the marker icon
        int resourceId = markerIconToResource(icon);

        // Load the marker icon as a Bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);

        // Resize the Bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return resizedBitmap;
    }
    private int markerIconToResource(BitmapDescriptor markerIcon) {
        // Get the resource ID of the marker icon
        if (markerIcon == BitmapDescriptorFactory.defaultMarker()) {
            return android.R.color.holo_red_dark; // Example color resource, replace with your own
        } else {
            // Handle custom marker icons here
            // Return the resource ID of the custom marker icon
            // For example: return R.drawable.custom_marker_icon;
            // Replace "custom_marker_icon" with the name of your custom marker icon resource
            return R.drawable.red_circle;
        }
    }



}
