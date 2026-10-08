package com.yovexa.solutions.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.yovexa.solutions.repository")
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .applyToConnectionPoolSettings(pool -> pool
                        .minSize(2)
                        .maxSize(25)
                        .maxWaitTime(5, TimeUnit.SECONDS)
                        .maxConnectionIdleTime(60, TimeUnit.SECONDS))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS));
    }

    @Bean
    public org.springframework.boot.ApplicationRunner initRefreshTokenIndices(
            org.springframework.data.mongodb.core.MongoTemplate mongoTemplate) {
        return args -> {
            try {
                mongoTemplate.indexOps(com.yovexa.solutions.model.RefreshToken.class).ensureIndex(
                        new org.springframework.data.mongodb.core.index.Index()
                                .on("tokenHash", org.springframework.data.domain.Sort.Direction.ASC)
                                .unique()
                );
                mongoTemplate.indexOps(com.yovexa.solutions.model.RefreshToken.class).ensureIndex(
                        new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                                new org.bson.Document("familyId", 1).append("isRevoked", 1)
                        )
                );
                mongoTemplate.indexOps(com.yovexa.solutions.model.RefreshToken.class).ensureIndex(
                        new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                                new org.bson.Document("adminId", 1).append("isRevoked", 1)
                        )
                );
                mongoTemplate.indexOps(com.yovexa.solutions.model.RefreshToken.class).ensureIndex(
                        new org.springframework.data.mongodb.core.index.Index()
                                .on("expiresAt", org.springframework.data.domain.Sort.Direction.ASC)
                                .expire(0)
                );
            } catch (Exception ignored) {
                // Keep startup non-blocking if Mongo is unreachable during build/test phase
            }
        };
    }
}
