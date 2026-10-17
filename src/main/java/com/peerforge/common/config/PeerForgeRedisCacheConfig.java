package com.peerforge.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching(order = 0)
@ConditionalOnProperty(prefix = "peerforge.cache", name = "enabled", havingValue = "true")
public class PeerForgeRedisCacheConfig {

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        var typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.peerforge.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.util.")
                .build();
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        var valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        org.springframework.data.redis.cache.RedisCacheConfiguration defaults =
                org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .disableCachingNullValues()
                        .computePrefixWith(cacheName -> "peerforge::" + cacheName + "::")
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration("publicMentorCards", defaults.entryTtl(Duration.ofMinutes(3)))
                .withCacheConfiguration("publicMentorDetails", defaults.entryTtl(Duration.ofMinutes(3)))
                .withCacheConfiguration("landingStats", defaults.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("featuredReviews", defaults.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
