package com.modern.kitchensink.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final MongoProperties mongoProperties;

    @Override
    @Nonnull
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

    @Override
    @Nonnull
    public MongoClient mongoClient() {
        String connectionString = String.format(
                "mongodb://%s:%s@%s:%d/%s?authSource=%s",
                mongoProperties.getUsername(),
                mongoProperties.getPassword(),
                mongoProperties.getHost(),
                mongoProperties.getPort(),
                mongoProperties.getDatabase(),
                mongoProperties.getAuthenticationDatabase()
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        return MongoClients.create(settings);
    }
}