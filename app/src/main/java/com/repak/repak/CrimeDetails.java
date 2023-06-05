package com.repak.repak;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrimeDetails extends AppCompatActivity {
    Button submitButton;
    TextView dateTextView;
    EditText detailsEditText, crimesSelector, dayEditText, monthEditText, yearEditText;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_details);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        submitButton = findViewById(R.id.submitCrimeReport);
        dateTextView = findViewById(R.id.dateOfCrime);
        detailsEditText = findViewById(R.id.CrimeDetailsInputText);
        crimesSelector = findViewById(R.id.crimesSelector);
        dayEditText = findViewById(R.id.dayEditText);
        monthEditText = findViewById(R.id.monthEditText);
        yearEditText = findViewById(R.id.yearEditText);

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle date selection if needed
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String crimeType = crimesSelector.getText().toString();
                String crimeDetails = detailsEditText.getText().toString();
                String day = dayEditText.getText().toString();
                String month = monthEditText.getText().toString();
                String year = yearEditText.getText().toString();

                if (TextUtils.isEmpty(crimeType) || TextUtils.isEmpty(crimeDetails) ||
                        TextUtils.isEmpty(day) || TextUtils.isEmpty(month) || TextUtils.isEmpty(year)) {
                    Toast.makeText(CrimeDetails.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int dayValue = Integer.parseInt(day);
                int monthValue = Integer.parseInt(month);
                int yearValue = Integer.parseInt(year);

                // Check for valid date range
                if (dayValue < 1 || dayValue > 31 || monthValue < 1 || monthValue > 12 || yearValue < 00 || yearValue > 99) {
                    Toast.makeText(CrimeDetails.this, "Invalid date. Please enter a valid date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String crimeDate = day + "/" + month + "/" + year;

                // Retrieve the latitude and longitude values from the intent
                double latitude = getIntent().getDoubleExtra("latitude", 0);
                double longitude = getIntent().getDoubleExtra("longitude", 0);

                Map<String, Object> crimeData = new HashMap<>();
                crimeData.put("latitude", latitude);
                crimeData.put("longitude", longitude);
                crimeData.put("type", crimeType);
                crimeData.put("details", crimeDetails);
                crimeData.put("date", crimeDate);

                db.collection("crimes")
                        .add(crimeData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(CrimeDetails.this, "Crime report submitted successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CrimeDetails.this, viewDB.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG", "Error adding document", e);
                                Toast.makeText(CrimeDetails.this, "Failed to submit crime report.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
