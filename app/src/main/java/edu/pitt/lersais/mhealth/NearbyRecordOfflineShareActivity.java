package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;

public class NearbyRecordOfflineShareActivity extends NearbyConnectionsActivity {
    private final static String TAG = "SEND_RECORD_ACTIVITY";
    private final static String SERVICE_ID = "edu.pitt.lersais.mhealth.SERVICE_ID";
    private static final String FIREBASE_DATABASE = "MedicalHistory";
    private final static String CURRENT_STATUS_SHARE = "Now sharing";
    private final static String CURRENT_STATUS_RECEIVE = "Now Receiving";


    private Strategy mStrategy = Strategy.P2P_CLUSTER;
    private Switch mSenderSwitch;
    private Button mPairButton;
    private RadioGroup mSelectDocumentRadioGroup;
    private Button mSendButton;
    private TextView mStatusTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // TODO: Private variable mState of enum State
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
            mStatusTextView = findViewById(R.id.send_receive_status);

            mSenderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        mStatusTextView.setText(CURRENT_STATUS_RECEIVE);
                        findViewById(R.id.receive_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_record_component).setVisibility(View.GONE);
                        findViewById(R.id.share_component).setVisibility(View.GONE);

                    } else {
                        mStatusTextView.setText(CURRENT_STATUS_SHARE);
                        findViewById(R.id.share_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_component).setVisibility(View.GONE);
                    }
                }
            });

            mPairButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int selectedDocument = mSelectDocumentRadioGroup.getCheckedRadioButtonId();
                    if (selectedDocument == R.id.share_radio_button_medical_record) {
                        // complete sharing part
//                        if (mState.equals(NetworkInfo.State.CONNECTED)) {
//                            // TODO: uncomment and implement sharing logic
//                        }
                    }
                }
            });
        }
    }

    @Override
    protected String getName() {
        String name;
        if (mSenderSwitch.isActivated()) {
            name = "Receiver";
        } else {
            name = "Sender";
        }
        return name;
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
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        //authenticate here
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
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(this, "Disconnected from " + endpoint.getName(), Toast.LENGTH_SHORT);
        if (getConnectedEndpoints().isEmpty()) {
            // TODO: Change status to discovering
        }
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
//        hideProgressDialog();
    }



}
