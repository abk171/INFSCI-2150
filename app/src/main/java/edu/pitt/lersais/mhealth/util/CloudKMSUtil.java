package edu.pitt.lersais.mhealth.util;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.api.services.cloudkms.v1.CloudKMSScopes;

import java.io.IOException;
import java.io.InputStream;

/**
 * The CloudKMSUtil that is a toolkit to encrypt/decrypt using Google Cloud KMS.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */

public class CloudKMSUtil {

    private CloudKMSUtil() {
    }

    private static class CloudKMSUtilHolder {
        private static final CloudKMSUtil INSTANCE = new CloudKMSUtil();
    }

    public static final CloudKMSUtil getInstance() {
        return CloudKMSUtilHolder.INSTANCE;
    }

    public CloudKMS createAuthorizedClient(Context context) throws IOException {

        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        InputStream input = context.getAssets().open("LERSAIS-mHealth-KMS-bdd9f7acef42.json");
        GoogleCredential credential = GoogleCredential.fromStream(input);

        input.close();

        if (credential.createScopedRequired()) {
            credential = credential.createScoped(CloudKMSScopes.all());
        }

        return new CloudKMS.Builder(transport, jsonFactory, credential)
                .setApplicationName("CloudKMS mHealthDemo")
                .build();
    }

    public String encrypt(String plaintext, String userUid, CloudKMS kms)
            throws IOException {
        //TODO: Task 1.2
        // BEGIN

        return null;
        // END
    }

    public String decrypt(String ciphertext, String userUid, CloudKMS kms)
            throws IOException {
        // TODO: Task 1.2
        // BEGIN

        return null;
        // END
    }
}
