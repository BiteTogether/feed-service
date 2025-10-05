package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends MongoRepository<Feed, String> {
  Page<Feed> findByUserId(Long userId, Pageable pageable);

  Page<Feed> findByPlaceId(Long placeId, Pageable pageable);

  // Custom query với sorting mặc định theo createdAt descending
  @Query(value = "{}", sort = "{ 'createdAt': -1 }")
  Page<Feed> findAllOrderByCreatedAtDesc(Pageable pageable);

  @Query(value = "{ 'userId': ?0 }", sort = "{ 'createdAt': -1 }")
  Page<Feed> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
