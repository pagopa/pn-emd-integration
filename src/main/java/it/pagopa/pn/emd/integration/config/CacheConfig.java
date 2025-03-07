package it.pagopa.pn.emd.integration.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URISyntaxException;

import static it.pagopa.pn.emd.integration.cache.RedisMode.SERVERLESS;


@Configuration
@EnableCaching
@Slf4j
@RequiredArgsConstructor
public class CacheConfig {
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean
    @Primary
    @Profile("!local")
    public JedisConnectionFactory elasticacheConnectionFactory() throws URISyntaxException {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();

        if (redisCache.getMode() == SERVERLESS) {
            jedisClientConfiguration.useSsl();
        } else {
            GenericObjectPoolConfig<Jedis> poolConfig = new JedisPoolConfig();
            poolConfig.setMaxIdle(30);
            poolConfig.setMinIdle(10);
            jedisClientConfiguration.usePooling().poolConfig(poolConfig);
        }

        return new PnEmdIntegrationConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build(), redisCache);
    }

    @Profile("local")
    public JedisConnectionFactory localConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs redisCache = pnEmdIntegrationConfigs.getRedisCache();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCache.getHostName(), redisCache.getPort());
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    @Bean
    public RedisTemplate<String, RetrievalPayload> retrievalPayloadRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, RetrievalPayload> redisTemplate = new RedisTemplate<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<RetrievalPayload> serializer = new Jackson2JsonRedisSerializer<>(RetrievalPayload.class);
        serializer.setObjectMapper(objectMapper);

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
