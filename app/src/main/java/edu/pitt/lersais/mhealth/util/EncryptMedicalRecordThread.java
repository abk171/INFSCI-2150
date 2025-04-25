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