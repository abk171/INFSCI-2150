package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;

/**
 * The MedicalRecordViewActivity that is used to view Medical Record.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class MedicalRecordViewActivity extends BaseActivity  {

    private static final String TAG = "MedRecordViewActivity";
    private static final String FIREBASE_DATABASE = "MedicalHistory";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private MedicalHistoryRecord mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_view);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        } else {

            final String ID = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DATABASE).child(ID);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // TODO: 1) acquire the encrypted medical history record from database and decrypt
                    // Tips: similar to encryption part:
                    // 1) create a Decryption Thread extending from Thread to deal with decryption works.
                    // 2) create a Message Handler extending from Handler
                    // 3) implement the callback method, e.g., display decrypted message, of Message Handler in this activity
                    // 4) start the thread here
                    // BEGIN


                    // END
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MedicalRecordViewActivity.this,
                            MedicalRecordEditActivity.class);

                    intent.putExtra("flag", "MedicalRecordViewActivity");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("map", mMessage);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MedicalRecordViewActivity.this,
                MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}



