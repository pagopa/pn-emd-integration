package it.pagopa.pn.emd.integration.cache;

import io.lettuce.core.RedisCredentials;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.time.Instant;

/**
 * Provides IAM-based Redis credentials for Lettuce, caching the signed token for up to 15 minutes.
 */
@Slf4j
public class RedisIAMAuthCredentialsProvider {

    private static final long TOKEN_TTL_SECONDS = 900L;

    private final String userId;
    private final IAMAuthTokenRequest iamAuthTokenRequest;
    private final AwsCredentialsProvider awsCredentialsProvider;

    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public RedisIAMAuthCredentialsProvider(String userId,
                                           IAMAuthTokenRequest iamAuthTokenRequest,
                                           AwsCredentialsProvider awsCredentialsProvider) {
        this.userId = userId;
        this.iamAuthTokenRequest = iamAuthTokenRequest;
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    /**
     * Returns valid Redis credentials, regenerating the IAM token if expired.
     *
     * @return RedisCredentials containing userId and the current signed IAM token
     */
    public synchronized RedisCredentials getRedisCredentials() {
        if (Instant.now().isAfter(tokenExpiry)) {
            log.debug("IAM token expired or not yet generated — regenerating.");
            cachedToken = iamAuthTokenRequest.toSignedRequestUri(awsCredentialsProvider.resolveCredentials());
            tokenExpiry = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
            log.debug("IAM token regenerated, next expiry: {}", tokenExpiry);
        }
        return RedisCredentials.just(userId, cachedToken);
    }
}
