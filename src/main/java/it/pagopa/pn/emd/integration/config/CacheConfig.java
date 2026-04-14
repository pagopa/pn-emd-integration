package it.pagopa.pn.emd.integration.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import it.pagopa.pn.emd.integration.cache.IamRedisCredentialsProviderFactory;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import jakarta.annotation.PostConstruct;
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
import reactor.core.publisher.Mono;

@Configuration
@EnableCaching
@Slf4j
@RequiredArgsConstructor
public class CacheConfig {
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @PostConstruct
    public void warmUpRedisConnection(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        if (reactiveRedisConnectionFactory == null) return;
        log.info("[REDIS-WARMUP] Pre-establishing Redis connection (DNS + TLS + HELLO)...");
        long start = System.currentTimeMillis();
        try {
            String response = reactiveRedisConnectionFactory.getReactiveConnection()
                    .ping()
                    .onErrorResume(ex -> {
                        log.warn("[REDIS-WARMUP] Warm-up failed (non-fatal): {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            if (response != null) {
                log.info("[REDIS-WARMUP] Redis connection ready in {} ms", System.currentTimeMillis() - start);
            }
        } catch (Exception ex) {
            log.warn("[REDIS-WARMUP] Warm-up exception (non-fatal): {}", ex.getMessage());
        }
    }

    @Bean
    @Primary
    @Profile("!local")
    public LettuceConnectionFactory elasticacheConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .redisCredentialsProviderFactory(new IamRedisCredentialsProviderFactory(redisCache))
                .useSsl()
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    @Profile("local")
    public LettuceConnectionFactory localConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());
        return new LettuceConnectionFactory(redisConfig, LettuceClientConfiguration.defaultConfiguration());
    }

    @Bean
    public ReactiveRedisTemplate<String, RetrievalPayload> reactiveRetrievalPayloadRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<RetrievalPayload> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, RetrievalPayload.class);
        RedisSerializationContext<String, RetrievalPayload> context =
                RedisSerializationContext.<String, RetrievalPayload>newSerializationContext(RedisSerializer.string())
                        .key(RedisSerializer.string())
                        .value(serializer)
                        .hashKey(RedisSerializer.string())
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
    }

}
