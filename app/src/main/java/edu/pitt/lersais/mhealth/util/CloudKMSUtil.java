package edu.pitt.lersais.mhealth.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.api.services.cloudkms.v1.CloudKMSScopes;
import com.google.api.services.cloudkms.v1.model.DecryptRequest;
import com.google.api.services.cloudkms.v1.model.DecryptResponse;
import com.google.api.services.cloudkms.v1.model.EncryptRequest;
import com.google.api.services.cloudkms.v1.model.EncryptResponse;

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

        String keyName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID,
                Constant.KMS_LOCATION,
                Constant.KMS_KEY_RING_ID,
                userUid
        );

        // 2. Prepare plaintext bytes
        byte[] plaintextBytes = plaintext.getBytes("UTF-8");
        String plaintextBase64 = Base64.encodeToString(plaintextBytes, Base64.NO_WRAP);

        // 3. Build the request
        EncryptRequest request = new EncryptRequest();
        request.setPlaintext(plaintextBase64);

        // 4. Call the encrypt API
        CloudKMS.Projects.Locations.KeyRings.CryptoKeys.Encrypt kmsRequest =
                kms.projects().locations().keyRings().cryptoKeys().encrypt(keyName, request);

        EncryptResponse response = kmsRequest.execute();

        // 5. Return ciphertext
        return response.getCiphertext();
        // END
    }

    public String decrypt(String ciphertext, String userUid, CloudKMS kms)
            throws IOException {
        // TODO: Task 1.2
        // BEGIN
        String keyName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID,
                Constant.KMS_LOCATION,
                Constant.KMS_KEY_RING_ID,
                userUid
        );

        DecryptRequest request = new DecryptRequest();
        request.setCiphertext(ciphertext);

//        CloudKMS.Projects.Locations.KeyRings.CryptoKeys.Decrypt kmsRequest =
//                kms.projects().locations().keyRings().cryptoKeys().decrypt(keyName, request);
//
//        DecryptResponse response = kmsRequest.execute();
//
//        byte[] plaintextBytes = Base64.decode(response.getPlaintext(), Base64.NO_WRAP);
//        return new String(plaintextBytes, "UTF-8");

        try {
            CloudKMS.Projects.Locations.KeyRings.CryptoKeys.Decrypt kmsRequest =
                    kms.projects().locations().keyRings().cryptoKeys().decrypt(keyName, request);

            DecryptResponse response = kmsRequest.execute();

            byte[] plaintextBytes = Base64.decode(response.getPlaintext(), Base64.NO_WRAP);
            return new String(plaintextBytes, "UTF-8");
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            Log.e("KMS_DECRYPT", "Decryption failed for ciphertext " + ciphertext + e.getDetails());
            throw new IOException("KMS decryption failed", e);
        }
        // END
    }
}
