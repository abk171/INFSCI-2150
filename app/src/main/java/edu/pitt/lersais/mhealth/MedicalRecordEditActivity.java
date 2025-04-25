package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.EncryptMedicalRecordThread;
import edu.pitt.lersais.mhealth.util.EncryptMessageHandler;
/**
 * The MedicalRecordEditActivity that is used to edit Medical Record.
 *
 * @author Haobing Huang and Runhua Xu.
 */
public class MedicalRecordEditActivity extends BaseActivity implements EncryptMessageHandler.Callback {

    private static final String TAG = "MedicalRecordEditActivity";
    private static final String FIREBASE_DATABASE = "MedicalHistory";

    private List<CheckBox> mDiseasesList = new ArrayList<CheckBox>();
    private HashMap<String, RadioGroup> mHabits = new HashMap<>();


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // If edit existing medical history record
        Intent getIntent = getIntent();
        String flag = getIntent.getStringExtra("flag");
        if (flag.equals("MedicalRecordViewActivity")) {
            Bundle bundle = getIntent.getBundleExtra("data");
            MedicalHistoryRecord record = (MedicalHistoryRecord) bundle.getSerializable("map");
            preSetMedicalHistoryRecordValue(record);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference(FIREBASE_DATABASE).child(mCurrentUser.getUid());

        findViewById(R.id.save_record).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    MedicalHistoryRecord record = getMedicalHistoryRecordFromView();
                    if(!valueValidate(record)){
                        return;
                    }
                    else{
                        encryptAndSaveRecord(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Acquire the medical history record information from view to construct a MedicalHistoryRecord instance.
     * @return A MedicalHistoryRecord instance.
     * @throws IOException
     */
    public MedicalHistoryRecord getMedicalHistoryRecordFromView() throws IOException{
        // TODO: Task 1.3
        // BEGIN


        return null;
        // END
    }

    /**
     * Call an encryption thread to encrypt the record, the thread will callback the message handler to save the encrypted record.
     * @param medicalHistoryRecord
     * @throws IOException
     */
    public void encryptAndSaveRecord(MedicalHistoryRecord medicalHistoryRecord) throws IOException {

        EncryptMessageHandler messageHandler = new EncryptMessageHandler(Looper.getMainLooper());
        messageHandler.setCallback(MedicalRecordEditActivity.this);

        Thread encryptorThread = new EncryptMedicalRecordThread(
                medicalHistoryRecord,
                mCurrentUser.getUid(),
                getApplicationContext(),
                messageHandler);
        encryptorThread.start();

        showProgressDialog();

    }

    /**
     * Save the encrypted record the cloud database
     *
     * @param encryptedRecord
     */
    public void processEncryptRecord(MedicalHistoryRecord encryptedRecord) {

        mDatabase.setValue(encryptedRecord);

        hideProgressDialog();

        Intent intent = new Intent(MedicalRecordEditActivity.this,
                MedicalRecordViewActivity.class);
        startActivity(intent);
    }

    public boolean valueValidate(MedicalHistoryRecord record){
        if(record.getName().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Name can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;

        }
        if(record.getDob().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Date of Birth can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getSex().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Sex can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getMarital_status().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Marital Status can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getOccupation().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Occupation can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getContact().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Contact can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getAllergies().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Allergies can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        for(String habit: record.getHabits().keySet()){
            if(record.getHabits().get(habit).isEmpty()){
                Toast.makeText(MedicalRecordEditActivity.this, "habit-"+habit+" can not be empty.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    /**
     * preset the decrypted medical record for editing
     * @param data
     */
    public void preSetMedicalHistoryRecordValue(MedicalHistoryRecord data) {
        // TODO: Task 1.3


        // END
    }
}
