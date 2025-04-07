package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * The SignupActivity is used to handle registration, email-password registration.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class SignupActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SignupActivity";

    // BEGIN
    private FirebaseAuth mAuth;
    // END

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // BEGIN
        mAuth = FirebaseAuth.getInstance();
        // END

        mEmailEditText = findViewById(R.id.field_email);
        mPasswordEditText = findViewById(R.id.field_password);
        mConfirmPasswordEditText = findViewById(R.id.field_password_confirm);

        findViewById(R.id.email_create_account_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            registration();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void registration() {
        // TODO: Task 2.2 implement the registration procedure here.
        // Tips: recommended steps
        // 1) get the email and password from user's input
        String email = mEmailEditText.getText().toString().trim();
        String pass1 = mPasswordEditText.getText().toString().trim();
        String pass2 = mConfirmPasswordEditText.getText().toString().trim();
        // 2) validate the format of user's input
        if (email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all fields correctly",
                    Toast.LENGTH_SHORT).show();
            return;
        }



        if (!pass1.equals(pass2)) {
            Toast.makeText(this,
                    "Passwords must match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 3) present a progress dialog (check in the BaseActivity)
        showProgressDialog();
        // 4) call related register method provided by Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, pass1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Toast.makeText(SignupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());


                        }
                    }
                });
        // 5) close the progress dialog

    }
}
