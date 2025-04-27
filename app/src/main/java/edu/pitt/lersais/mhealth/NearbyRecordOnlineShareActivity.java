package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Random;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;
import edu.pitt.lersais.mhealth.util.DecryptMessageHandler;
import tgio.rncryptor.RNCryptorNative;
/**
 * The NearbyRecordOnlineShareActivity that is used to share record to others nearby.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class NearbyRecordOnlineShareActivity extends BaseActivity implements DecryptMessageHandler.Callback {

    private final static String TAG = "SHARE_RECORD_ACTIVITY";
    private final static String MESSAGE_DEFAULT = "DEFAULT MESSAGE";
    private static final String FIREBASE_DATABASE = "MedicalHistory";

    private final static String CURRENT_STATUS_SHARE = "Ready to Share";
    private final static String CURRENT_STATUS_RECEIVE = "Ready to Receive";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView mTextViewPasscode;
    private EditText mEditTextPasscode;
    private Switch mSwitch;
    private TextView mTextViewStatus;
    private RadioGroup mRadioGroupRecordChoose;

    private Message mMessage;
    private MessageListener mMessageListener;

    private String mPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {

            mDatabase = FirebaseDatabase.getInstance().getReference(FIREBASE_DATABASE).child(currentUser.getUid());

            mTextViewPasscode = findViewById(R.id.share_text_passcode);
            mEditTextPasscode = findViewById(R.id.share_receive_passcode);
            mSwitch = findViewById(R.id.share_switch);
            mTextViewStatus = findViewById(R.id.share_receive_status);

            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mTextViewStatus.setText(CURRENT_STATUS_RECEIVE);
                        findViewById(R.id.receive_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_record_component).setVisibility(View.GONE);
                        findViewById(R.id.share_component).setVisibility(View.GONE);
                    } else {
                        mTextViewStatus.setText(CURRENT_STATUS_SHARE);
                        findViewById(R.id.share_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_component).setVisibility(View.GONE);
                    }
                }
            });

            findViewById(R.id.share_button_generate).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO: task 2.1.1 Generate a random alphanumeric passcode (6-8 bit) and display in the mTextViewPasscode
                    // BEGIN
                    int random = new Random().nextInt(1000000);
                    mTextViewPasscode.setText("" + random);
                    // END
                }
            });

            mRadioGroupRecordChoose = findViewById(R.id.share_radio_button_group);

            findViewById(R.id.share_button_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // TODO: Task 2.1.2 Send the record using nearby message
                    // Begin
                    mPasscode = mTextViewPasscode.getText().toString();
                    if (mPasscode.isEmpty()) {
                        snackbarNotify(view, "Please generate the passcode first");
                        return;
                    }

                    int selectedMedicalRecord = mRadioGroupRecordChoose.getCheckedRadioButtonId();
                    if (selectedMedicalRecord != R.id.share_radio_button_medical_record) {
                        mMessage = new Message(MESSAGE_DEFAULT.getBytes());
                        Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this).publish(mMessage);
                        return;
                    }

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                Log.e(TAG, "Snapshot doesn't exist.");
                                return;
                            }

                            MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                            if (!dataSnapshot.exists()) {
                                Log.e(TAG, "Medical record doesn't exist.");
                                Toast.makeText(NearbyRecordOnlineShareActivity.this, "Please create a medical record", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DecryptMessageHandler messageHandler = new DecryptMessageHandler(Looper.getMainLooper());
                            messageHandler.setCallback(NearbyRecordOnlineShareActivity.this);

                            Thread decryptionThread = new DecryptMedicalRecordThread(
                                    encryptedRecord,
                                    currentUser.getUid(),
                                    getApplicationContext(),
                                    messageHandler
                            );

                            decryptionThread.start();
                            showProgressDialog();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Database error " + databaseError.getMessage());
                            Toast.makeText(NearbyRecordOnlineShareActivity.this, "Failed to retrieve medical record", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // END
                }
            });

            findViewById(R.id.share_button_receive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // TODO: Task 2.1.3 Receive the record
                    // BEGIN
                    mPasscode = mEditTextPasscode.getText().toString();
                    if (mPasscode.isEmpty()) {
                        return;
                    }
                    mMessageListener = new MessageListener() {
                        @Override
                        public void onFound(Message message) {
                            Log.d(TAG, "Received medical record");
                            // display the record
                            displayRecord(secureMessageToMedicalRecord(message, mPasscode));
                        }

                        @Override
                        public void onLost(Message message) {
                            Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
                        }
                    };

                    Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this)
                            .subscribe(mMessageListener)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Received medical record");
                                    }
                                }
                            });
                    // END
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void displayRecord(MedicalHistoryRecord medicalHistoryRecord) {
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

        nameView.setText(medicalHistoryRecord.getName());
        dobView.setText(medicalHistoryRecord.getDob());
        sexView.setText(medicalHistoryRecord.getSex());
        marital_statusView.setText(medicalHistoryRecord.getMarital_status());
        occupationView.setText(medicalHistoryRecord.getOccupation());
        contactView.setText(medicalHistoryRecord.getContact());
        allergiesView.setText(medicalHistoryRecord.getAllergies());

        StringBuilder diseaseBuilder = new StringBuilder();
        String[] diseases = medicalHistoryRecord.getDiseases().split(",");

        for (String disease : diseases) {
            if (diseaseBuilder.length() > 0) {
                diseaseBuilder.append(", ");
            }
            diseaseBuilder.append(disease);
        }

        diseasesView.setText(diseaseBuilder.toString());

        HashMap<String, String> family_diseases = medicalHistoryRecord.getFamily_diseases();
        fatherView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        motherView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        siblingView.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));

        HashMap<String, String> habits = medicalHistoryRecord.getHabits();

        alcoholView.setText(habits.get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY));
        cannabisView.setText(habits.get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY));

        commentsView.setText(medicalHistoryRecord.getComments());


    }
    private Message secureMedicalRecordToMessage(MedicalHistoryRecord medicalHistoryRecord, String passcode) {
        // TODO: Task 2.1.2
        // BEGIN
        RNCryptorNative rnCryptorNative = new RNCryptorNative();
        Gson gson = new Gson();
        String recordJson = gson.toJson(medicalHistoryRecord);
        return new Message(rnCryptorNative.encrypt(recordJson, passcode));
        // END
    }

    private MedicalHistoryRecord secureMessageToMedicalRecord(Message message, String passcode) {
        // TODO: Task 2.1.3
        // BEGIN
        RNCryptorNative rnCryptorNative = new RNCryptorNative();
        Gson gson  = new Gson();
        String decryptedMessageJson = rnCryptorNative.decrypt(message.toString(), passcode);
        return gson.fromJson(decryptedMessageJson, MedicalHistoryRecord.class);
        // END
    }



    @Override
    public void onStop() {
        if (mMessage != null) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
        }
        if (mMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        }
        super.onStop();
    }

    @Override
    public void processDecryptRecord(Object record) {
        MedicalHistoryRecord medicalHistoryRecord = (MedicalHistoryRecord) record;
        // send the message here
        mMessage = secureMedicalRecordToMessage(medicalHistoryRecord, mPasscode);
        Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this).
                publish(mMessage).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Record sent successfully");
                        }
                        else {
                            Log.d(TAG, "Unable to send record");
                        }
                    }
                });

        hideProgressDialog();
    }
}
