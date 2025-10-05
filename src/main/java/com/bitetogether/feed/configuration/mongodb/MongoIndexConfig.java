package com.bitetogether.feed.configuration.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexConfig implements CommandLineRunner {

  @Autowired private MongoTemplate mongoTemplate;

  @Override
  public void run(String... args) {
    IndexOperations indexOps = mongoTemplate.indexOps("feeds");

    // Index cho pagination sorting
    indexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));
    indexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC));
    indexOps.ensureIndex(new Index().on("placeId", Sort.Direction.ASC));

    // Compound index cho common queries
    indexOps.ensureIndex(
        new Index().on("userId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
  }
}
