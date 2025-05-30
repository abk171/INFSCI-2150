package edu.pitt.lersais.mhealth.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.api.services.cloudkms.v1.CloudKMS;

import java.io.IOException;
import java.util.HashMap;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;

/**
 * The EncryptMedicalRecordThread that is used to encrypt.
 *
 * @author Haobing Huang and Runhua Xu.
 */
public class EncryptMedicalRecordThread extends Thread {
    private static final String TAG = "RecordEncryptThread";

    private Handler handler;

    private MedicalHistoryRecord originalRecord;
    private MedicalHistoryRecord encryptedRecord;
    private Context context;
    private String userUid;

    public EncryptMedicalRecordThread(
            MedicalHistoryRecord record,
            String userUid,
            Context context,
            Handler handler) {
        this.originalRecord = record;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.encryptedRecord = new MedicalHistoryRecord();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical history record");
        System.out.println("begins:" + currentThread().getName());
        try {

            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            CloudKMS kms = kmsUtil.createAuthorizedClient(this.context);

            // TODO: Task 1.4
            // TODO: encrypt the content of the record from originalRecord and store in encryptedRecord
            // BEGIN
            encryptedRecord.setName(kmsUtil.encrypt(originalRecord.getName(), userUid, kms));
            encryptedRecord.setDob(kmsUtil.encrypt(originalRecord.getDob(), userUid, kms));
            encryptedRecord.setSex(kmsUtil.encrypt(originalRecord.getSex(), userUid, kms));
            encryptedRecord.setMarital_status(kmsUtil.encrypt(originalRecord.getMarital_status(), userUid, kms));
            encryptedRecord.setOccupation(kmsUtil.encrypt(originalRecord.getOccupation(), userUid, kms));
            encryptedRecord.setContact(kmsUtil.encrypt(originalRecord.getContact(), userUid, kms));
            encryptedRecord.setAllergies(kmsUtil.encrypt(originalRecord.getAllergies(), userUid, kms));
            encryptedRecord.setDiseases(kmsUtil.encrypt(originalRecord.getDiseases(), userUid, kms));
            encryptedRecord.setComments(kmsUtil.encrypt(originalRecord.getComments(), userUid, kms));

            HashMap<String, String> encrypted_family_diseases = new HashMap<>();
            HashMap<String, String> family_diseases = originalRecord.getFamily_diseases();

            for (String key: family_diseases.keySet()) {
//                String encrypted_key = kmsUtil.encrypt(key, userUid, kms);
                String encrypted_value = kmsUtil.encrypt(family_diseases.get(key), userUid, kms);
                encrypted_family_diseases.put(key, encrypted_value);
            }

            encryptedRecord.setFamily_diseases(encrypted_family_diseases);

            HashMap <String, String> encrypted_habits = new HashMap<>();
            HashMap <String, String> habits = originalRecord.getHabits();

            for (String key: habits.keySet()) {
//                String encrypted_key = kmsUtil.encrypt(key, userUid, kms);
                String encrypted_value = kmsUtil.encrypt(habits.get(key), userUid, kms);
                encrypted_habits.put(key, encrypted_value);
            }

            encryptedRecord.setHabits(encrypted_habits);
            // END


            Message msg = new Message();
            msg.obj = encryptedRecord;
            System.out.println("message set finish");
            handler.sendMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}