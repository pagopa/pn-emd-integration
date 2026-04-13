package it.pagopa.pn.emd.integration.cache;

import com.amazonaws.auth.AWSCredentials;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IAMAuthTokenRequestTest {

    @Test
    void toSignedRequestUri_returnsCorrectUri() throws URISyntaxException {
        AWSCredentials credentials = mock(AWSCredentials.class);
        when(credentials.getAWSAccessKeyId()).thenReturn("accessKey");
        when(credentials.getAWSSecretKey()).thenReturn("secretKey");
        IAMAuthTokenRequest request = new IAMAuthTokenRequest("userId", "cacheName", "region", false);

        String signedUri = request.toSignedRequestUri(credentials);

        URIBuilder expectedUri = new URIBuilder("http://cacheName/")
                .addParameter("Action", "connect")
                .addParameter("User", "userId");
        assertTrue(signedUri.contains(expectedUri.build().toString().replace("http://", "")));
    }

    @Test
    void toSignedRequestUri_includesServerlessParameter() throws URISyntaxException {
        AWSCredentials credentials = mock(AWSCredentials.class);
        when(credentials.getAWSAccessKeyId()).thenReturn("accessKey");
        when(credentials.getAWSSecretKey()).thenReturn("secretKey");
        IAMAuthTokenRequest request = new IAMAuthTokenRequest("userId", "cacheName", "region", true);

        String signedUri = request.toSignedRequestUri(credentials);

        URIBuilder expectedUri = new URIBuilder("http://cacheName/")
                .addParameter("Action", "connect")
                .addParameter("User", "userId")
                .addParameter("ResourceType", "ServerlessCache");
        assertTrue(signedUri.contains(expectedUri.build().toString().replace("http://", "")));
    }
}