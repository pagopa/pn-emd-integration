package it.pagopa.pn.emd.integration.cache;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A class to generate an IAM auth token. This implementation is based on the AWS User Guide: <a href="https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth-iam.html">...</a>
 */
public class IAMAuthTokenRequest {
    private static final SdkHttpMethod REQUEST_METHOD = SdkHttpMethod.GET;
    private static final String REQUEST_PROTOCOL = "http://";
    private static final String PARAM_ACTION = "Action";
    private static final String PARAM_USER = "User";
    private static final String PARAM_RESOURCE_TYPE = "ResourceType";
    private static final String RESOURCE_TYPE_SERVERLESS_CACHE = "ServerlessCache";
    private static final String ACTION_NAME = "connect";
    private static final String SERVICE_NAME = "elasticache";
    private static final long TOKEN_EXPIRY_SECONDS = 900;

    private final String userId;
    private final String cacheName;
    private final String region;
    private final boolean isServerless;

    /**
     * Instantiates a new Iam auth token request.
     *
     * @param userId       the user id
     * @param cacheName    the cache name
     * @param region       the region
     * @param isServerless the is serverless
     */
    public IAMAuthTokenRequest(String userId, String cacheName, String region, boolean isServerless) {
        this.userId = userId;
        this.cacheName = cacheName;
        this.region = region;
        this.isServerless = isServerless;
    }

    /**
     * To signed request uri string.
     *
     * @param credentials the credentials
     * @return the string
     */
    public String toSignedRequestUri(AwsCredentials credentials) {
        SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder()
                .method(REQUEST_METHOD)
                .uri(URI.create(REQUEST_PROTOCOL + cacheName + "/"))
                .putRawQueryParameter(PARAM_ACTION, ACTION_NAME)
                .putRawQueryParameter(PARAM_USER, userId);

        if (isServerless) {
            requestBuilder.putRawQueryParameter(PARAM_RESOURCE_TYPE, RESOURCE_TYPE_SERVERLESS_CACHE);
        }

        Aws4PresignerParams presignerParams = Aws4PresignerParams.builder()
                .awsCredentials(credentials)
                .signingRegion(Region.of(region))
                .signingName(SERVICE_NAME)
                .expirationTime(Instant.now().plus(TOKEN_EXPIRY_SECONDS, ChronoUnit.SECONDS))
                .build();

        SdkHttpFullRequest signedRequest = Aws4Signer.create().presign(requestBuilder.build(), presignerParams);
        return signedRequest.getUri().toString().replace(REQUEST_PROTOCOL, "");
    }
}
