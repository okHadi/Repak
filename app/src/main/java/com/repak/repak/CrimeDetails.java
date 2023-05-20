package com.repak.repak;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrimeDetails extends AppCompatActivity {
    Button submitButton;
    Spinner crimeSpinner;
    Spinner dateSpinner;
    TextView dateTextView;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_details);
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        submitButton = findViewById(R.id.submitCrimeReport);
        crimeSpinner = findViewById(R.id.crimeSpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        dateTextView = findViewById(R.id.dateOfCrime);
        calendar = Calendar.getInstance();

        String[] crimes = {"Robbery", "Snatching", "Fraud", "Sexual Assault"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, crimes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        crimeSpinner.setAdapter(adapter);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                openCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Retrieve the latitude and longitude values from the intent
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String selectedCrime = crimeSpinner.getSelectedItem().toString();
                String selectedDate = dateTextView.getText().toString();

                Map<String, Object> crimeData = new HashMap<>();
                crimeData.put("latitude", latitude);
                crimeData.put("longitude", longitude);
                crimeData.put("type", selectedCrime);
                crimeData.put("date", selectedDate);

                db.collection("crimes")
                        .add(crimeData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG", "Error adding document", e);
                            }
                        });
            }
        });
    }

    public void openCalendar() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTextView();
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Set maximum date to current date
        datePickerDialog.show();
    }

    private void updateDateTextView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDate = dateFormat.format(calendar.getTime());
        dateTextView.setText(selectedDate);
    }
}
