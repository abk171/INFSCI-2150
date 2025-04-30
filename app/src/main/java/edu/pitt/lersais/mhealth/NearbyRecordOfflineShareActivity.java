package edu.pitt.lersais.mhealth;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Random;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;
import edu.pitt.lersais.mhealth.util.DecryptMessageHandler;

public class NearbyRecordOfflineShareActivity extends NearbyConnectionsActivity implements DecryptMessageHandler.Callback {
    private final static String TAG = "SEND_RECORD_ACTIVITY";
    private final static String SERVICE_ID = "edu.pitt.lersais.mhealth.SERVICE_ID";
    private static final String FIREBASE_DATABASE = "MedicalHistory";
    private final static String CURRENT_STATUS_SHARE = "Now sharing";
    private final static String CURRENT_STATUS_RECEIVE = "Now Receiving";

    private final static String CURRENT_STATUS_UNKNOWN = "Unknown";
    private final static String CURRENT_STATUS_DISCOVERING = "Discovering";
    private final static String CURRENT_STATUS_ADVERTISING = "Advertising";
    private final static String CURRENT_STATUS_CONNECTED = "Connected";


    private Strategy mStrategy = Strategy.P2P_CLUSTER;
    private Switch mSenderSwitch;
    private Button mPairButton;
    private RadioGroup mSelectDocumentRadioGroup;
    private Button mSendButton;
    private TextView mStatusTextView;
    private TextView mRoleTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private State mState;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            mDatabase = FirebaseDatabase.getInstance().getReference(FIREBASE_DATABASE).child(currentUser.getUid());
            mSenderSwitch = findViewById(R.id.share_switch_send);
            mPairButton = findViewById(R.id.nearby_button_shake);
            mSelectDocumentRadioGroup = findViewById(R.id.share_radio_button_group);
            mSendButton = findViewById(R.id.nearby_button_send);
            mRoleTextView = findViewById(R.id.share_receive_status);
            mStatusTextView = findViewById(R.id.send_receive_status);

            mSenderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        mRoleTextView.setText(CURRENT_STATUS_RECEIVE);
                        findViewById(R.id.receive_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_record_component).setVisibility(View.GONE);
                        findViewById(R.id.share_component).setVisibility(View.GONE);


                    } else {
                        mRoleTextView.setText(CURRENT_STATUS_SHARE);
                        findViewById(R.id.share_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_component).setVisibility(View.GONE);

                    }
                    setState(State.UNKNOWN);
                }
            });

            setState(State.UNKNOWN);

            mPairButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getCurrentRoleStatus().equals(CURRENT_STATUS_SHARE)) {
                        setState(State.ADVERTISING);
                    } else {
                        setState(State.DISCOVERING);
                    }
                }
            });

            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int selectedDocument = mSelectDocumentRadioGroup.getCheckedRadioButtonId();
                    if (selectedDocument == R.id.share_radio_button_medical_record) {
                        if (mState == State.CONNECTED) {
                            // TODO: uncomment and implement sharing logic
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                                        if (encryptedRecord != null) {
                                            DecryptMessageHandler messageHandler = new DecryptMessageHandler(Looper.getMainLooper());
                                            messageHandler.setCallback(NearbyRecordOfflineShareActivity.this);

                                            Thread decryptionThread = new DecryptMedicalRecordThread(
                                                    encryptedRecord,
                                                    currentUser.getUid(),
                                                    getApplicationContext(),
                                                    messageHandler
                                            );

                                            decryptionThread.start();
                                            showProgressDialog();
                                        } else {
                                            Log.e(TAG, "Medical record doesn't exist.");
                                            toastNotify("Please create a medical record!");
                                        }
                                    } else {
                                        Log.e(TAG, "Snapshot doesn't exist");
                                        toastNotify("Database could not be contacted!");
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Database error " + databaseError.getMessage());
                                    toastNotify("Failed to retrieve medical record");
                                }
                            });
                        } else {
                            toastNotify("Devices are not connected!");
                            logD("Attempted to share without connection.");
                        }
                    }
                }
            });
        }
    }

    private String getCurrentRoleStatus() {
        return mRoleTextView.getText().toString();
    }

    @Override
    protected String getName() {
        return mAuth.getCurrentUser().getDisplayName() + "_" + mAuth.getCurrentUser().getEmail();
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return mStrategy;
    }

    @Override
    protected void onAdvertisingStarted() {
        String passcode = generateRandomPasscode();
        showPasscodeDialog(passcode);
        Payload passPayload = Payload.fromBytes(passcode.getBytes());
        send(passPayload);
    }

    private String generateRandomPasscode() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6 + random.nextInt(3); i++) {
            if (random.nextBoolean()) {
                builder.append((char) ('a' + random.nextInt(26)));
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    private void showPasscodeDialog(String code) {
        new AlertDialog.Builder(this)
                .setTitle("Share Passcode")
                .setMessage("Your passcode: " + code)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        //TODO: complete authenticate here
        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        if(!isConnecting()) {
            connectToEndpoint(endpoint);
        }
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(this, "Connected to " + endpoint.getName(), Toast.LENGTH_SHORT);
        // TODO: Change status to connected
        setState(State.CONNECTED);
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(this, "Disconnected from " + endpoint.getName(), Toast.LENGTH_SHORT);
        if (getConnectedEndpoints().isEmpty()) {
            // TODO: Change status to discovering
            setState(State.DISCOVERING);
        }
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            String message = new String(payload.asBytes());
            if (message.matches("[a-z0-9]{6,9}")) {
                showAuthPrompt(endpoint, message); // assume it's the passcode
            } else {
                // must be a record
                MedicalHistoryRecord record = new Gson().fromJson(message, MedicalHistoryRecord.class);
                displayRecord(record);
            }
        }
    }

    private void showAuthPrompt(Endpoint endpoint, String code) {
        new AlertDialog.Builder(this)
                .setTitle("Authentication Request")
                .setMessage("Did the sender give you this passcode?\n\n" + code)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    acceptConnection(endpoint); // send accept
                })
                .setNegativeButton("No", (dialog, which) -> {
                    rejectConnection(endpoint); // send reject
                })
                .show();
    }


    private void displayRecord(MedicalHistoryRecord medicalHistoryRecord) {
        findViewById(R.id.receive_record_component).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.name)).setText(medicalHistoryRecord.getName());
        ((TextView) findViewById(R.id.dob)).setText(medicalHistoryRecord.getDob());
        ((TextView) findViewById(R.id.sex)).setText(medicalHistoryRecord.getSex());
        ((TextView) findViewById(R.id.marital_status)).setText(medicalHistoryRecord.getMarital_status());
        ((TextView) findViewById(R.id.occupation)).setText(medicalHistoryRecord.getOccupation());
        ((TextView) findViewById(R.id.contact)).setText(medicalHistoryRecord.getContact());
        ((TextView) findViewById(R.id.allergies)).setText(medicalHistoryRecord.getAllergies());
        ((TextView) findViewById(R.id.pastdiseases)).setText(medicalHistoryRecord.getDiseases());
        ((TextView) findViewById(R.id.father)).setText(medicalHistoryRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        ((TextView) findViewById(R.id.mother)).setText(medicalHistoryRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        ((TextView) findViewById(R.id.sibling)).setText(medicalHistoryRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));
        ((TextView) findViewById(R.id.Alcohol)).setText(medicalHistoryRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY));
        ((TextView) findViewById(R.id.Cannabis)).setText(medicalHistoryRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY));
        ((TextView) findViewById(R.id.comments)).setText(medicalHistoryRecord.getComments());
        hideProgressDialog();
    }

    @Override
    public void processDecryptRecord(Object record) {
       // TODO: Send the message here
        MedicalHistoryRecord medicalHistoryRecord = (MedicalHistoryRecord) record;
        Gson gson = new Gson();
        String recordJson = gson.toJson(medicalHistoryRecord);
        Payload bytesPayload = Payload.fromBytes(recordJson.getBytes());
        send(bytesPayload);
        showProgressDialog();
    }

    public enum State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }

    private State getState() {
        return mState;
    }

    private void setState(State state) {
        if (mState == state) {
            logW("State already in " + state);
            return;
        }
        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(state);
        updateStatusTextView(state);
    }

    private void onStateChanged(State newState) {
        // Update Nearby Connections to the new state.

        switch (newState) {
            case DISCOVERING:
                if (isAdvertising()) {
                    stopAdvertising();
                }
                disconnectFromAllEndpoints();
                startDiscovering();

                break;
            case ADVERTISING:
                if (isDiscovering()) {
                    stopDiscovering();
                }
                disconnectFromAllEndpoints();
                startAdvertising();
                break;
            case CONNECTED:
                if (isDiscovering()) {
                    stopDiscovering();
                } else if (isAdvertising()) {
                    stopAdvertising();
                }
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

        // Update the UI.

    }

    private void updateStatusTextView(State newState) {
        String newStateText = "";
        switch (newState) {
            case DISCOVERING:
                newStateText = CURRENT_STATUS_DISCOVERING;
                break;
            case ADVERTISING:
                newStateText = CURRENT_STATUS_ADVERTISING;
                break;
            case CONNECTED:
                newStateText = CURRENT_STATUS_CONNECTED;
                break;
            case UNKNOWN:
                newStateText = CURRENT_STATUS_UNKNOWN;
                break;
            default:
                // no-op
                break;
        }
        mStatusTextView.setText(newStateText);
    }
}
