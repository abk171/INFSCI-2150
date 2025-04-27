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
public class DecryptMedicalRecordThread extends Thread {
    private static final String TAG = "RecordEncryptThread";

    private Handler handler;

    private MedicalHistoryRecord decryptedRecord;
    private MedicalHistoryRecord encryptedRecord;
    private Context context;
    private String userUid;

    public DecryptMedicalRecordThread(
            MedicalHistoryRecord record,
            String userUid,
            Context context,
            Handler handler) {
        this.encryptedRecord = record;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.decryptedRecord = new MedicalHistoryRecord();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical history record");
        System.out.println("begins:" + currentThread().getName());
        try {

            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            CloudKMS kms = kmsUtil.createAuthorizedClient(this.context);

            decryptedRecord.setName(kmsUtil.decrypt(encryptedRecord.getName(), userUid, kms));
            decryptedRecord.setDob(kmsUtil.decrypt(encryptedRecord.getDob(), userUid, kms));
            decryptedRecord.setSex(kmsUtil.decrypt(encryptedRecord.getSex(), userUid, kms));
            decryptedRecord.setMarital_status(kmsUtil.decrypt(encryptedRecord.getMarital_status(), userUid, kms));
            decryptedRecord.setOccupation(kmsUtil.decrypt(encryptedRecord.getOccupation(), userUid, kms));
            decryptedRecord.setContact(kmsUtil.decrypt(encryptedRecord.getContact(), userUid, kms));
            decryptedRecord.setAllergies(kmsUtil.decrypt(encryptedRecord.getAllergies(), userUid, kms));
            decryptedRecord.setDiseases(kmsUtil.decrypt(encryptedRecord.getDiseases(), userUid, kms));
            decryptedRecord.setComments(kmsUtil.decrypt(encryptedRecord.getComments(), userUid, kms));

            HashMap<String, String> decrypted_family_diseases = new HashMap<>();
            HashMap<String, String> encrypted_family_diseases = encryptedRecord.getFamily_diseases();

            for (String key : encrypted_family_diseases.keySet()) {
//                String decrypted_key = kmsUtil.decrypt(key, userUid, kms);
                String decrypted_value = kmsUtil.decrypt(encrypted_family_diseases.get(key), userUid, kms);
                decrypted_family_diseases.put(key, decrypted_value);
            }

            decryptedRecord.setFamily_diseases(decrypted_family_diseases);

            HashMap<String, String> decrypted_habits = new HashMap<>();
            HashMap<String, String> encrypted_habits = encryptedRecord.getHabits();

            for (String key: encrypted_habits.keySet()) {
//                String decrypted_key = kmsUtil.decrypt(key, userUid, kms);
                String decrypted_value = kmsUtil.decrypt(encrypted_habits.get(key), userUid, kms);
                decrypted_habits.put(key, decrypted_value);
            }

            decryptedRecord.setHabits(decrypted_habits);

            Message msg = new Message();
            msg.obj = decryptedRecord;
            System.out.println("message set finish");
            handler.sendMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}