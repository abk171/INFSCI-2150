package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.CloudKMSUtil;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;
import edu.pitt.lersais.mhealth.util.DecryptMessageHandler;

/**
 * The MedicalRecordViewActivity that is used to view Medical Record.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class MedicalRecordViewActivity extends BaseActivity implements DecryptMessageHandler.Callback {

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
                    MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                    DecryptMessageHandler messageHandler = new DecryptMessageHandler(Looper.getMainLooper());
                    messageHandler.setCallback(MedicalRecordViewActivity.this);

                    Thread decryptionThread = new DecryptMedicalRecordThread(
                            encryptedRecord,
                            mAuth.getCurrentUser().getUid(),
                            getApplicationContext(),
                            messageHandler
                    );
                    decryptionThread.start();

                    showProgressDialog();
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

    public void processDecryptRecord(Object decryptedRecord) {
        mMessage = (MedicalHistoryRecord) decryptedRecord;
        TextView nameView = findViewById(R.id.name);
        TextView dobView = findViewById(R.id.dob);
        TextView sexView = findViewById(R.id.sex);
        TextView marital_statusView = findViewById(R.id.marital_status);
        TextView occupationView = findViewById(R.id.occupation);
        TextView contactView = findViewById(R.id.contact);
        TextView allergiesView = findViewById(R.id.allergies);
        TextView diseasesView = findViewById(R.id.pastdiseases);
        TextView fatherView = findViewById(R.id.father);
        TextView motherView = findViewById(R.id.mother);
        TextView siblingView = findViewById(R.id.sibling);
        TextView alcoholView = findViewById(R.id.Alcohol);
        TextView cannabisView = findViewById(R.id.Cannabis);
        TextView commentsView = findViewById(R.id.comments);

        nameView.setText(mMessage.getName());
        dobView.setText(mMessage.getDob());
        sexView.setText(mMessage.getSex());
        marital_statusView.setText(mMessage.getMarital_status());
        occupationView.setText(mMessage.getOccupation());
        contactView.setText(mMessage.getContact());
        allergiesView.setText(mMessage.getAllergies());

        StringBuilder diseaseBuilder = new StringBuilder();
        String[] diseases = mMessage.getDiseases().split(",");

        for (String disease : diseases) {
            if (diseaseBuilder.length() > 0) {
                diseaseBuilder.append(", ");
            }
            diseaseBuilder.append(disease);
        }

        diseasesView.setText(diseaseBuilder.toString());

        HashMap<String, String> family_diseases = mMessage.getFamily_diseases();
        fatherView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        motherView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        siblingView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));

        HashMap<String, String> habits = mMessage.getHabits();

        alcoholView.setText(habits.get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY));
        cannabisView.setText(habits.get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY));

        commentsView.setText(mMessage.getComments());

        hideProgressDialog();
    }
}



