package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
/**
 * The NearbyRecordOnlineShareActivity that is used to share record to others nearby.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class NearbyRecordOnlineShareActivity extends BaseActivity{

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

                    // END
                }
            });

            findViewById(R.id.share_button_receive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // TODO: Task 2.1.3 Receive the record
                    // BEGIN

                    // END
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private Message secureMedicalRecordToMessage(MedicalHistoryRecord medicalHistoryRecord, String passcode) {
        // TODO: Task 2.1.2
        // BEGIN

        return null;
        // BEGIN
    }

    private MedicalHistoryRecord secureMessageToMedicalRecord(Message message, String passcode) {
        // TODO: Task 2.1.3
        // BEGIN

        return null;
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

}
