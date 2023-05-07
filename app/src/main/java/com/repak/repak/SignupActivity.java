package com.repak.repak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText, passwordEditText, otpEditText;
    private Button signUpButton, verifyOTPButton;
    private static final String PHONE_REGEX = "^\\+(?:[0-9] ?){6,14}[0-9]$";

    // TextWatcher implementation to validate input as the user types
    //********************Sign up form validation***************************************//
    private static class InputValidator implements TextWatcher {

        private final EditText editText;
        private final String pattern;
        private final String errorMessage;

        InputValidator(EditText editText, String pattern, String errorMessage) {
            this.editText = editText;
            this.pattern = pattern;
            this.errorMessage = errorMessage;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString().trim();
            if (!input.matches(pattern)) {
                editText.setError(errorMessage);
            } else {
                editText.setError(null);
            }
        }
    }

    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        emailEditText = findViewById(R.id.email);
        phoneEditText = findViewById(R.id.phone);
        passwordEditText = findViewById(R.id.signUpPass);

        // Add TextWatcher to validate input as the user types
        firstNameEditText.addTextChangedListener(new InputValidator(firstNameEditText, "^[\\p{L}]+$", "Invalid first name"));
        lastNameEditText.addTextChangedListener(new InputValidator(lastNameEditText, "^[\\p{L}]+$", "Invalid last name"));
        emailEditText.addTextChangedListener(new InputValidator(emailEditText, Patterns.EMAIL_ADDRESS.pattern(), "Invalid email"));
        phoneEditText.addTextChangedListener(new InputValidator(phoneEditText, "^\\+[0-9]{10,12}$", "Invalid phone number"));
        passwordEditText.addTextChangedListener(new InputValidator(passwordEditText, ".{6,}", "Password must be at least 6 characters long"));

        //************Go back to login page*****************************************//
        TextView loginButton = findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });


        //**********************************Firebase authentication*****************************////
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        signUpButton = findViewById(R.id.SignUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check if required fields are filled in
                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, send verification email
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (!task1.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Failed to send verification email to " + user.getEmail(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(),
                                        "Authentication failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });



            }
        });

    }
}
