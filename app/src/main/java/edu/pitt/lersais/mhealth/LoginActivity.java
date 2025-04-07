package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
 * The LoginActivity is used to handle login authentication, email password authentication.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Login_Password_Activity";
    private static final int REQUEST_SIGNUP = 123;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    // BEGIN
    private FirebaseAuth mAuth;
    // END

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // BEGIN
        mAuth = FirebaseAuth.getInstance();
        // END

        mEmailEditText = findViewById(R.id.field_email);
        mPasswordEditText = findViewById(R.id.field_password);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.link_signup).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_sign_in_button) {
            login();
        }
        else if (i == R.id.link_signup) {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    private void login() {
        // TODO: Task 2.3 implement the login code here with validation for user inputs.
        // Tips: recommended steps
        // 1) get the email and password from user's input
        String email = mEmailEditText.getText().toString().trim();
        String pass = mPasswordEditText.getText().toString().trim();
        // 2) validate the format of user's input
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter a valid e-mail or password", Toast.LENGTH_SHORT).show();
            return;
        }
        // 3) present a progress dialog (check in the BaseActivity)
        showProgressDialog();
        // 4) call related authentication method provided by Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // 5) close the progress dialog

    }

}
