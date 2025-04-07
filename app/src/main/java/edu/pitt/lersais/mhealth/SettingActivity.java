package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The SettingActivity that is used to handle setting features such as email verification and
 * password reset.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Setting_Activity";
    private static final int REQUEST_CODE_FOR_GALLERY = 5201314;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText editTextCurrentPwd;
    private EditText editTextNewPwd;
    private EditText editTextConfirmPwd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            TextView textViewUID = findViewById(R.id.text_view_uid);
            textViewUID.setText(currentUser.getUid());
            TextView textViewEmail = findViewById(R.id.setting_email);
            textViewEmail.setText(currentUser.getEmail());
            TextView textViewEmailStatus = findViewById(R.id.setting_email_status);
            if (currentUser.isEmailVerified()) {
                textViewEmailStatus.setText("EMAIL NOT VERIFIED");
                findViewById(R.id.setting_button_verify_email).setOnClickListener(this);
                findViewById(R.id.setting_button_reset_password_email).setEnabled(false);
            } else {
                textViewEmailStatus.setText("EMAIL VERIFIED");
                findViewById(R.id.setting_button_verify_email).setEnabled(false);
            }
            findViewById(R.id.setting_button_reset_password).setOnClickListener(this);
            findViewById(R.id.setting_button_reset_password_email).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.setting_button_verify_email) {
            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "Verification email is sent.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else if (i == R.id.setting_button_reset_password) {

            resetPasswordByInput();
        } else if (i == R.id.setting_button_reset_password_email) {

            resetPasswordByEmail();
        }
    }

    private void resetPasswordByInput() {
        // TODO: Task 4.1 reset password by user's input
        // Tips: check the usage of related method provided by Firebase Authentication
        String currentPass = editTextCurrentPwd.getText().toString().trim();
        String newPass1 = editTextNewPwd.getText().toString().trim();
        String newPass2 = editTextCurrentPwd.getText().toString().trim();

        if (currentPass.isEmpty() || newPass1.isEmpty() || newPass2.isEmpty()) {
            Log.e(TAG, "Invalid password!");
            Toast.makeText(this, "Invalid password. Try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass1.equals(newPass2)) {
            Log.e(TAG, "Passwords don't match");
            Toast.makeText(this, "Passwords don't match. Try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.updatePassword(newPass1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Password updated successfully");
                Toast.makeText(SettingActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Password could not be updated");
                Toast.makeText(SettingActivity.this, "Password couldn't be updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPasswordByEmail() {
        // TODO: Task 4.2 reset password by user's email
        // Tips: check the usage of related method provided by Firebase Authentication

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.sendPasswordResetEmail(currentUser.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Sent password reset e-mail");
                    Toast.makeText(SettingActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e(TAG, "Password reset e-mail could not be sent");
                    Toast.makeText(SettingActivity.this, "Unable to sent reset email", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
