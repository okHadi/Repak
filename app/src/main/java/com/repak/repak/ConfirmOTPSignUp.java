package com.repak.repak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class ConfirmOTPSignUp extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_otpsign_up);

        otpEditText = findViewById(R.id.mobileOTPeditText);
        verifyButton = findViewById(R.id.verifyButton);

        mAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();
                if (otp.isEmpty()) {
                    otpEditText.setError("Please enter OTP.");
                    otpEditText.requestFocus();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(ConfirmOTPSignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Verification successful, redirect user to main activity
                                        Intent intent = new Intent(ConfirmOTPSignUp.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Verification failed, display a message to the user
                                        Toast.makeText(ConfirmOTPSignUp.this, "Failed to verify OTP.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
