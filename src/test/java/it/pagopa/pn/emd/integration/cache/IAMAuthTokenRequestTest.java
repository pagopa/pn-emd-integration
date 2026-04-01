package it.pagopa.pn.emd.integration.cache;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IAMAuthTokenRequestTest {

    @Test
    void toSignedRequestUri_returnsCorrectUri() {
        AwsCredentials credentials = AwsBasicCredentials.create("accessKey", "secretKey");
        IAMAuthTokenRequest request = new IAMAuthTokenRequest("userId", "cacheName", "region", false);

        String signedUri = request.toSignedRequestUri(credentials);

        assertTrue(signedUri.contains("cacheName/"));
        assertTrue(signedUri.contains("Action=connect"));
        assertTrue(signedUri.contains("User=userId"));
    }

    @Test
    void toSignedRequestUri_includesServerlessParameter() {
        AwsCredentials credentials = AwsBasicCredentials.create("accessKey", "secretKey");
        IAMAuthTokenRequest request = new IAMAuthTokenRequest("userId", "cacheName", "region", true);

        String signedUri = request.toSignedRequestUri(credentials);

        assertTrue(signedUri.contains("cacheName/"));
        assertTrue(signedUri.contains("Action=connect"));
        assertTrue(signedUri.contains("User=userId"));
        assertTrue(signedUri.contains("ResourceType=ServerlessCache"));
    }
}
