package com.bitetogether.feed.configuration.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "mongoAuditorProvider")
public class MongoAuditingConfig {}
