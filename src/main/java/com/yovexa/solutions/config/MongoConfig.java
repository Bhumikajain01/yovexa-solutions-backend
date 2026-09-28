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
}
