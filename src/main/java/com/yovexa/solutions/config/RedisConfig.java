package com.yovexa.solutions.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    public static final String HERO_CACHE = "heroes";
    public static final String ABOUT_CACHE = "about";
    public static final String SERVICES_CACHE = "services";
    public static final String PROJECTS_CACHE = "projects";
    public static final String CASE_STUDIES_CACHE = "caseStudies";
    public static final String BLOGS_CACHE = "blogs";
    public static final String SETTINGS_CACHE = "settings";
    public static final String DASHBOARD_CACHE = "dashboard";
    public static final String ADMINS_CACHE = "admins";

    @Value("${REDIS_URL:${spring.data.redis.url:}}")
    private String redisUrl;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout:200ms}")
    private Duration redisTimeout;

    @Value("${spring.data.redis.connect-timeout:${spring.data.redis.timeout:200ms}}")
    private Duration connectTimeout;

    @Value("${spring.data.redis.lettuce.shutdown-timeout:100ms}")
    private Duration shutdownTimeout;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisConfiguration serverConfig;
        boolean useSsl = sslEnabled;

        if (StringUtils.hasText(redisUrl)) {
            serverConfig = LettuceConnectionFactory.createRedisConfiguration(redisUrl);
            if (redisUrl.startsWith("rediss://")) {
                useSsl = true;
            }
        } else {
            RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (StringUtils.hasText(redisPassword)) {
                standalone.setPassword(RedisPassword.of(redisPassword));
            }
            serverConfig = standalone;
        }

        Duration effectiveConnectTimeout = connectTimeout != null ? connectTimeout : Duration.ofMillis(200);
        Duration effectiveCommandTimeout = redisTimeout != null ? redisTimeout : Duration.ofMillis(200);
        Duration effectiveShutdownTimeout = shutdownTimeout != null ? shutdownTimeout : Duration.ofMillis(100);

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(effectiveConnectTimeout)
                .keepAlive(true)
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .socketOptions(socketOptions)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(effectiveCommandTimeout)
                .shutdownTimeout(effectiveShutdownTimeout);

        if (useSsl) {
            builder.useSsl();
        }

        return new LettuceConnectionFactory(serverConfig, builder.build());
    }

    /**
     * Custom JSON serializer that handles polymorphic types for single DTOs and Collections
     * (including ImmutableCollections returned by Stream.toList() / List.of()).
     */
    public static class RedisJsonSerializer implements RedisSerializer<Object> {
        private final ObjectMapper mapper;

        public RedisJsonSerializer() {
            this.mapper = new ObjectMapper();
            this.mapper.registerModule(new JavaTimeModule());
            this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.mapper.activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.WRAPPER_ARRAY
            );
        }

        @Override
        public byte[] serialize(Object source) throws SerializationException {
            if (source == null) {
                return new byte[0];
            }
            try {
                if (source instanceof Collection<?> col && !(source instanceof ArrayList)) {
                    source = new ArrayList<>(col);
                }
                return mapper.writeValueAsBytes(source);
            } catch (Exception e) {
                throw new SerializationException("Could not serialize object: " + e.getMessage(), e);
            }
        }

        @Override
        public Object deserialize(byte[] source) throws SerializationException {
            if (source == null || source.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(source, Object.class);
            } catch (Exception e) {
                throw new SerializationException("Could not deserialize JSON: " + e.getMessage(), e);
            }
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        RedisSerializer<Object> jsonSerializer = new RedisJsonSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jsonSerializer = new RedisJsonSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // Dashboard stats change and calculate counts; refresh every 5 minutes
        cacheConfigs.put(DASHBOARD_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        // Content caches TTL 60 minutes (invalidated on writes)
        cacheConfigs.put(HERO_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(ABOUT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(SERVICES_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(PROJECTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(CASE_STUDIES_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(BLOGS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(SETTINGS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(ADMINS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis GET failed for cache='{}', key='{}'. Fallback to DB: {}",
                        cache != null ? cache.getName() : "null", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis PUT failed for cache='{}', key='{}: {}",
                        cache != null ? cache.getName() : "null", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis EVICT failed for cache='{}', key='{}: {}",
                        cache != null ? cache.getName() : "null", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis CLEAR failed for cache='{}': {}",
                        cache != null ? cache.getName() : "null", exception.getMessage());
            }
        };
    }
}
