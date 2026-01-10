package com.modern.member.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Getter
@Setter
public class MongoProperties {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String authenticationDatabase;
}