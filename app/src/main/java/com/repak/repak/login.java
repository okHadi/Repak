package com.repak.repak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.LoginButton);
        progressBar = findViewById(R.id.progressBar);

        // Handle Login Button Click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    emailEditText.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Enter a valid email");
                    emailEditText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE); // Show the loading indicator

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE); // Hide the loading indicator

                            if (task.isSuccessful()) {
                                // Check if the email is verified
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user.isEmailVerified()) {
                                    // Email is verified, proceed with login
                                    Intent intent = new Intent(login.this, AddCrimeMap.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Email is not verified, display an error message and delete the user
                                    Toast.makeText(login.this, "Email not verified, create a new account.",
                                            Toast.LENGTH_SHORT).show();
                                    mAuth.getCurrentUser().delete();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(login.this, "Email or password is incorrect.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Handle Signup Text Click
        TextView signupText = findViewById(R.id.SignUpButton);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
