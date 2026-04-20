package it.pagopa.pn.emd.integration.cache;

import io.lettuce.core.RedisCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RedisIAMAuthCredentialsProviderTest {

    private IAMAuthTokenRequest iamAuthTokenRequest;
    private AwsCredentialsProvider awsCredentialsProvider;
    private RedisIAMAuthCredentialsProvider provider;

    @BeforeEach
    void setUp() {
        iamAuthTokenRequest = mock(IAMAuthTokenRequest.class);
        awsCredentialsProvider = mock(AwsCredentialsProvider.class);
        when(awsCredentialsProvider.resolveCredentials())
                .thenReturn(AwsBasicCredentials.create("accessKey", "secretKey"));
        when(iamAuthTokenRequest.toSignedRequestUri(any())).thenReturn("signed-token");

        provider = new RedisIAMAuthCredentialsProvider("userId", iamAuthTokenRequest, awsCredentialsProvider);
    }

    @Test
    void getRedisCredentials_firstCall_generatesToken() {
        RedisCredentials credentials = provider.getRedisCredentials();

        assertNotNull(credentials);
        assertEquals("userId", credentials.getUsername());
        assertArrayEquals("signed-token".toCharArray(), credentials.getPassword());
        verify(iamAuthTokenRequest, times(1)).toSignedRequestUri(any());
    }

    @Test
    void getRedisCredentials_secondCallBeforeExpiry_reusesToken() {
        provider.getRedisCredentials();
        RedisCredentials credentials = provider.getRedisCredentials();

        assertNotNull(credentials);
        verify(iamAuthTokenRequest, times(1)).toSignedRequestUri(any());
    }

    @Test
    void getRedisCredentials_callAfterExpiry_regeneratesToken() throws Exception {
        provider.getRedisCredentials();

        Field tokenExpiryField = RedisIAMAuthCredentialsProvider.class.getDeclaredField("tokenExpiry");
        tokenExpiryField.setAccessible(true);
        tokenExpiryField.set(provider, Instant.EPOCH);

        when(iamAuthTokenRequest.toSignedRequestUri(any())).thenReturn("new-signed-token");
        RedisCredentials credentials = provider.getRedisCredentials();

        assertArrayEquals("new-signed-token".toCharArray(), credentials.getPassword());
        verify(iamAuthTokenRequest, times(2)).toSignedRequestUri(any());
    }
}
