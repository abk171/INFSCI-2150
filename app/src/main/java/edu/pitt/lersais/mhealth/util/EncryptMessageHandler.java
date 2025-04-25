package edu.pitt.lersais.mhealth.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;

/**
 * The EncryptMessageHandler that is used to process the encrypted record.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class EncryptMessageHandler extends Handler {

    private Callback callback;
    public MedicalHistoryRecord record = new MedicalHistoryRecord();

    public EncryptMessageHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        this.record = (MedicalHistoryRecord) msg.obj;
        if (this.callback != null) {
            callback.processEncryptRecord(record);
        }
    }



    public interface Callback {
        void processEncryptRecord(MedicalHistoryRecord record);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
