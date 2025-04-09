package edu.pitt.lersais.mhealth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordEditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nameEditText, dobEditText, occupationEditText, contactEditText, allergiesEditText;
    private RadioGroup genderGroup, maritalStatusGroup, alcoholGroup, cannabisGroup;
    private CheckBox heartAttackCheck, rheumaticFeverCheck, heartMurmurCheck;
    private EditText fatherEditText, motherEditText, siblingEditText, commentsEditText;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medical_record);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        nameEditText = findViewById(R.id.name);
        dobEditText = findViewById(R.id.date_of_birth);
        occupationEditText = findViewById(R.id.occupation);
        contactEditText = findViewById(R.id.contact);
        genderGroup = findViewById(R.id.gender_group);
        maritalStatusGroup = findViewById(R.id.marital_status_group);
        allergiesEditText = findViewById(R.id.allergies);
        heartAttackCheck = findViewById(R.id.checkbox_heart_attack);
        rheumaticFeverCheck = findViewById(R.id.checkbox_rheumatic_fever);
        heartMurmurCheck = findViewById(R.id.checkbox_heart_murmur);
        fatherEditText = findViewById(R.id.family_father);
        motherEditText = findViewById(R.id.family_mother);
        siblingEditText = findViewById(R.id.family_sibling);
        alcoholGroup = findViewById(R.id.alcohol_group);
        cannabisGroup = findViewById(R.id.cannabis_group);
        commentsEditText = findViewById(R.id.comments);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(this);
        fetchMedicalRecord();
    }

    private void fetchMedicalRecord() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String uid = user.getUid();
        //saveButton.setEnabled(false);

        mDatabase.child("MedicalHistory").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> record = (Map<String, Object>) snapshot.getValue();
                    if (record != null) {
                        nameEditText.setText((String) record.get("name"));
                        dobEditText.setText((String) record.get("dob"));
                        occupationEditText.setText((String) record.get("occupation"));
                        contactEditText.setText((String) record.get("contact"));
                        allergiesEditText.setText((String) record.get("allergies"));
                        commentsEditText.setText((String) record.get("comments"));

                        Map<String, String> familyDiseases = (Map<String, String>) record.get("family_diseases");
                        if (familyDiseases != null) {
                            fatherEditText.setText(familyDiseases.get("Father"));
                            motherEditText.setText(familyDiseases.get("Mother"));
                            siblingEditText.setText(familyDiseases.get("Sibling"));
                        }

                        Map<String, String> habits = (Map<String, String>) record.get("habits");
                        if (habits != null) {
                            setRadioButton(alcoholGroup, habits.get("Alcohol"));
                            setRadioButton(cannabisGroup, habits.get("Cannabis"));
                        }

                        setRadioButton(genderGroup, (String) record.get("sex"));
                        setRadioButton(maritalStatusGroup, (String) record.get("marital_status"));

                        List<String> diseases = (List<String>) record.get("diseases");
                        if (diseases != null) {
                            heartAttackCheck.setChecked(diseases.contains("Heart attack"));
                            rheumaticFeverCheck.setChecked(diseases.contains("Rheumatic Fever"));
                            heartMurmurCheck.setChecked(diseases.contains("Heart murmur"));
                        }

                        saveButton.setEnabled(true);
                    }
                } else {
                    Toast.makeText(MedicalRecordEditActivity.this, "No medical record found. Please fill out the form.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MedicalRecordEditActivity.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_button) {
            saveMedicalRecord();
        }
    }

    private void saveMedicalRecord() {
        if (!validateForm()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String uid = user.getUid();

        Map<String, Object> record = new HashMap<>();
        record.put("name", nameEditText.getText().toString().trim());
        record.put("dob", dobEditText.getText().toString().trim());
        record.put("occupation", occupationEditText.getText().toString().trim());
        record.put("contact", contactEditText.getText().toString().trim());
        record.put("sex", getSelectedRadioText(genderGroup));
        record.put("marital_status", getSelectedRadioText(maritalStatusGroup));
        record.put("allergies", allergiesEditText.getText().toString().trim());
        record.put("comments", commentsEditText.getText().toString().trim());

        List<String> diseases = new ArrayList<>();
        if (heartAttackCheck.isChecked()) diseases.add("Heart attack");
        if (rheumaticFeverCheck.isChecked()) diseases.add("Rheumatic Fever");
        if (heartMurmurCheck.isChecked()) diseases.add("Heart murmur");
        record.put("diseases", diseases);

        Map<String, String> familyDiseases = new HashMap<>();
        familyDiseases.put("Father", fatherEditText.getText().toString().trim());
        familyDiseases.put("Mother", motherEditText.getText().toString().trim());
        familyDiseases.put("Sibling", siblingEditText.getText().toString().trim());
        record.put("family_diseases", familyDiseases);

        Map<String, String> habits = new HashMap<>();
        habits.put("Alcohol", getSelectedRadioText(alcoholGroup));
        habits.put("Cannabis", getSelectedRadioText(cannabisGroup));
        record.put("habits", habits);

        mDatabase.child("MedicalHistory").child(uid).setValue(record)
                .addOnCompleteListener(MedicalRecordEditActivity.this, new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MedicalRecordEditActivity.this, "Medical record saved.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MedicalRecordEditActivity.this, "Failed to save record.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        return !TextUtils.isEmpty(nameEditText.getText()) &&
                !TextUtils.isEmpty(dobEditText.getText()) &&
                !TextUtils.isEmpty(occupationEditText.getText()) &&
                !TextUtils.isEmpty(contactEditText.getText()) &&
                genderGroup.getCheckedRadioButtonId() != -1 &&
                maritalStatusGroup.getCheckedRadioButtonId() != -1;
    }

    private String getSelectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            return selectedButton.getText().toString();
        }
        return "";
    }

    private void setRadioButton(RadioGroup group, String value) {
        if (value == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton button = (RadioButton) child;
                if (value.equalsIgnoreCase(button.getText().toString())) {
                    button.setChecked(true);
                    break;
                }
            }
        }
    }
}