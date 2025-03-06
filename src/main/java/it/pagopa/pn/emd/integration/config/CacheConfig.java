package it.pagopa.pn.emd.integration.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.lettuce.core.ClientOptions;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.net.URISyntaxException;


@Configuration
@EnableCaching
@Slf4j
@RequiredArgsConstructor
public class CacheConfig {
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean
    @Primary
    @Profile("!local")
    public LettuceConnectionFactory elasticacheConnectionFactory() throws URISyntaxException {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder().autoReconnect(true).build())
                .commandTimeout(redisCache.getCommandTimeout())
                .useSsl()
                .build();

        return new PnEmdIntegrationConnectionFactory(redisStandaloneConfiguration, clientConfiguration, redisCache);
    }

    @Profile("local")
    public ReactiveRedisConnectionFactory localConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder().autoReconnect(true).build())
                .commandTimeout(redisCache.getCommandTimeout())
                .build();
        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
    }

    @Bean
    public ReactiveRedisTemplate<String, RetrievalPayload> retrievalPayloadOps(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<RetrievalPayload> serializer = new Jackson2JsonRedisSerializer<>(RetrievalPayload.class);
        serializer.setObjectMapper(objectMapper);

        RedisSerializationContext<String, RetrievalPayload> serializationContext =
                RedisSerializationContext.<String, RetrievalPayload>newSerializationContext(RedisSerializer.string())
                        .value(serializer)
                        .build();
        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
