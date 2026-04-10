package it.pagopa.pn.emd.integration.config.springbootcfg;

import it.pagopa.pn.commons.configs.RuntimeMode;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.commons.configs.aws.AwsServicesClientsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.lang.reflect.Proxy;

/**
 * Attiva solo i client AWS effettivamente usati da pn-emd-integration.
 * CloudWatch: usato da CloudWatchMetricHandler (metriche) — lasciato al parent.
 * DynamoDB, SQS, SSM, EventBridge: non usati — stub no-op per evitare
 * l'inizializzazione di Netty event loop e HTTP client inutili allo startup.
 */
@Configuration
public class AwsServicesClientsConfigActivation extends AwsServicesClientsConfig {

    public AwsServicesClientsConfigActivation(AwsConfigs props) {
        super(props, RuntimeMode.PROD);
    }

    @Bean
    @Override
    public DynamoDbAsyncClient dynamoDbAsyncClientWithMDC() {
        return stubClient(DynamoDbAsyncClient.class);
    }

    @Bean
    @Override
    public DynamoDbClient dynamoDbClient() {
        return stubClient(DynamoDbClient.class);
    }

    @Bean
    @Override
    public SqsClient sqsClient() {
        return stubClient(SqsClient.class);
    }

    @Bean
    @Override
    public SsmClient ssmClient() {
        return stubClient(SsmClient.class);
    }

    @Bean
    @Override
    public EventBridgeAsyncClient eventBridgeClient() {
        return stubClient(EventBridgeAsyncClient.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T stubClient(Class<T> clientInterface) {
        return (T) Proxy.newProxyInstance(
                clientInterface.getClassLoader(),
                new Class[]{ clientInterface },
                (proxy, method, args) -> switch (method.getName()) {
                    case "serviceName" -> clientInterface.getSimpleName().toLowerCase();
                    case "close"       -> null;
                    case "toString"    -> clientInterface.getSimpleName() + "[stub - not used in pn-emd-integration]";
                    case "hashCode"    -> System.identityHashCode(proxy);
                    case "equals"      -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            clientInterface.getSimpleName() + "." + method.getName() +
                            "() — client non attivo in pn-emd-integration");
                }
        );
    }
}
