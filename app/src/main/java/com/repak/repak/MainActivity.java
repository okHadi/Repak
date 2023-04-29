package com.repak.repak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Declare the two buttons we want to set up onClickListeners for
    private Button viewDbButton;
    private Button reportCrimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the two buttons in our layout using findViewById
        viewDbButton = findViewById(R.id.viewDB);
        reportCrimeButton = findViewById(R.id.reportCrime);

        // Set up an OnClickListener for the viewDBButton
        viewDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the SecondActivity
                Intent intent = new Intent(MainActivity.this, viewDB.class);
                // Call startActivity with the Intent to start the new activity
                startActivity(intent);
            }
        });

        // Set up an OnClickListener for the reportCrimeButton
        reportCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the ThirdActivity
                Intent intent = new Intent(MainActivity.this, login.class);
                // Call startActivity with the Intent to start the new activity
                startActivity(intent);
            }
        });
    }
}
