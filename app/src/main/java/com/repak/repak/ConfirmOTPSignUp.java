package com.repak.repak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ConfirmOTPSignUp extends AppCompatActivity {
    public String mVerificationId;
    private EditText otpEditText;
    private Button verifyButton;
    public PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_otpsign_up);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Get the Intent object that started this activity
        Intent intent = getIntent();

        // Get the values from the Intent extras
        String firstName = intent.getStringExtra("first_name");
        String lastName = intent.getStringExtra("last_name");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        String password = intent.getStringExtra("password");

        otpEditText = findViewById(R.id.mobileOTPeditText);
        verifyButton = findViewById(R.id.verifyButton);

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
    Log.d("log", "verification completed.");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("TAG", "onVerificationFailed", e);
            }

            @Override
            public void onCodeSent(@NonNull String s,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d("TAG", "onCodeSent:" + s);
                mVerificationId = s;
            }
        };

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();
                if (otp.isEmpty()) {
                    otpEditText.setError("Please enter OTP.");
                    otpEditText.requestFocus();
                }
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(ConfirmOTPSignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, start AddCrimeMap activity
                                        Intent intent = new Intent(ConfirmOTPSignUp.this, AddCrimeMap.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Sign in failed
                                        Toast.makeText(ConfirmOTPSignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        //email verification
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(ConfirmOTPSignUp.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ConfirmOTPSignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ConfirmOTPSignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
