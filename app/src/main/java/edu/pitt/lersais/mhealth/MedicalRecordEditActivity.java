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

    private EditText mNameEditText;
    private EditText mDobEditText;
    private RadioGroup mSexGroup;
    private RadioGroup mMaritalStatusGroup;
    private EditText mOccupationEditText;
    private EditText mContactEditText;

    // Medical History
    private EditText mAllergiesEditText;
    private List<CheckBox> mDiseasesList = new ArrayList<>();

    // Family Diseases
    private EditText mFatherEditText;
    private EditText mMotherEditText;
    private EditText mSiblingEditText;

    // Habits
    private HashMap<String, RadioGroup> mHabits = new HashMap<>();

    // Comments
    private EditText mCommentsEditText;

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

        // General Info
        mNameEditText = findViewById(R.id.name_edit);
        mDobEditText = findViewById(R.id.dob_edit);
        mSexGroup = findViewById(R.id.sex_group);
        mMaritalStatusGroup = findViewById(R.id.marital_status_group);
        mOccupationEditText = findViewById(R.id.occupation_edit);
        mContactEditText = findViewById(R.id.contact_edit);

        // Medical History
        mAllergiesEditText = findViewById(R.id.allergies_edit);
        mDiseasesList.add(findViewById(R.id.diseases1));
        mDiseasesList.add(findViewById(R.id.diseases2));
        mDiseasesList.add(findViewById(R.id.diseases3));
        // (Add more if you uncomment extra checkboxes in XML)

        // Family Diseases
        mFatherEditText = findViewById(R.id.father_edit);
        mMotherEditText = findViewById(R.id.mother_edit);
        mSiblingEditText = findViewById(R.id.sibling_edit);

        // Habits
        mHabits.put(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY, (RadioGroup) findViewById(R.id.Alcohol));
        mHabits.put(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY, (RadioGroup) findViewById(R.id.Cannabis));

        // Comments
        mCommentsEditText = findViewById(R.id.comments_edit);


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
        MedicalHistoryRecord data = new MedicalHistoryRecord();

        data.setName(mNameEditText.getText().toString());
        data.setDob(mDobEditText.getText().toString());
        data.setOccupation(mOccupationEditText.getText().toString());
        data.setContact(mContactEditText.getText().toString());
        data.setAllergies(mAllergiesEditText.getText().toString());
        data.setComments(mCommentsEditText.getText().toString());

        String sex = "";
        int sex_id = mSexGroup.getCheckedRadioButtonId();
        if (sex_id != -1) {
            RadioButton selectedRadioButton = findViewById(sex_id);
            sex = selectedRadioButton.getText().toString();
            data.setSex(sex);
        }

        String marital_status = "";
        int marital_status_id = mMaritalStatusGroup.getCheckedRadioButtonId();
        if (marital_status_id != -1) {
            RadioButton selectedRadioButton = findViewById(marital_status_id);
            marital_status = selectedRadioButton.getText().toString();
            data.setMarital_status(marital_status);
        }

        StringBuilder diseases_builder = new StringBuilder();
        for (CheckBox checkBox : mDiseasesList) {
            if (checkBox.isChecked()) {
                if (diseases_builder.length() > 0) {
                    diseases_builder.append(',');
                }
                diseases_builder.append(checkBox.getText().toString());
            }
        }

        data.setDiseases(diseases_builder.toString());

        HashMap<String, String> family_diseases = new HashMap<>();
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY, mFatherEditText.getText().toString());
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY, mMotherEditText.getText().toString());
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY, mSiblingEditText.getText().toString());

        data.setFamily_diseases(family_diseases);

        HashMap<String, String> habits = new HashMap<>();
        for (String key : mHabits.keySet()) {
            RadioGroup group = mHabits.get(key);
            int button_id = group.getCheckedRadioButtonId();
            if (button_id == -1) {
                habits.put(key, "");
                continue;
            }
            RadioButton radioButton = findViewById(button_id);
            habits.put(key, radioButton.getText().toString());
        }

        data.setHabits(habits);

        return data;
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
        mNameEditText.setText(data.getName());
        mDobEditText.setText(data.getDob());
        mOccupationEditText.setText(data.getOccupation());
        mContactEditText.setText(data.getContact());
        mAllergiesEditText.setText(data.getAllergies());
        mCommentsEditText.setText(data.getComments());

        HashMap<String, String> family_diseases = data.getFamily_diseases();
        mFatherEditText.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        mMotherEditText.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        mSiblingEditText.setText(family_diseases.get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));

        HashMap<String, String> habits = data.getHabits();

        for (String key : mHabits.keySet()) {
            RadioGroup radioGroup = mHabits.get(key);
            String habit = habits.get(key);
            if (habit.equals("Yes")) {
                radioGroup.check(R.id.y2);
            }
            else {
                radioGroup.check(R.id.n2);
            }
        }

        String sex = data.getSex();
        if (sex.equals("Male")) {
            mSexGroup.check(R.id.male);
        }
        else {
            mSexGroup.check(R.id.female);
        }

        String marital_status = data.getMarital_status();
        if (marital_status.equals("Single")) {
            mMaritalStatusGroup.check(R.id.single);
        } else if (marital_status.equals("Married")) {
            mMaritalStatusGroup.check(R.id.married);
        } else if (marital_status.equals("divorced")) {
            mMaritalStatusGroup.check(R.id.divorced);
        } else {
            mMaritalStatusGroup.check(R.id.widowed);
        }

        String[] diseases = data.getDiseases().split(",");

        for (String disease: diseases) {
            if (disease.trim().equals("Heart attack")) {
                mDiseasesList.get(0).setChecked(true);
            } else if(disease.trim().equals("Rheumatic Fever")) {
                mDiseasesList.get(1).setChecked(true);
            } else {
                mDiseasesList.get(2).setChecked(true);
            }
        }


        // END
    }
}
