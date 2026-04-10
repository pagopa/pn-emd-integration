package it.pagopa.pn.emd.integration.config.springbootcfg;

import it.pagopa.pn.emd.integration.config.aws.AwsConfigs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.net.URI;

@Configuration
public class AwsServicesClientsConfigActivation {

    AwsConfigs props;

    @Bean
    @Lazy
    public CloudWatchAsyncClient cloudWatchClient() {
        return configureBuilder(CloudWatchAsyncClient.builder());
    }

    private <C> C configureBuilder(AwsClientBuilder<?, C> builder) {
        if (props != null) {

            String profileName = props.getProfileName();
            if (StringUtils.isNotBlank(profileName)) {
                builder.credentialsProvider(ProfileCredentialsProvider.create(profileName));
            }

            String regionCode = props.getRegionCode();
            if (StringUtils.isNotBlank(regionCode)) {
                builder.region(Region.of(regionCode));
            }

            String endpointUrl = props.getEndpointUrl();
            if (StringUtils.isNotBlank(endpointUrl)) {
                builder.endpointOverride(URI.create(endpointUrl));
            }

        }

        return builder.build();
    }


}
