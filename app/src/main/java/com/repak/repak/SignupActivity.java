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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText, passwordEditText;
    private Button signUpButton;
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}


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
        phoneEditText.addTextChangedListener(new InputValidator(phoneEditText, PHONE_REGEX, "Invalid phone number"));
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




                // Create a new user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(SignupActivity.this, AddCrimeMap.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }
}
