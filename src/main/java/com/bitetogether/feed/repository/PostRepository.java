package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Post;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
  Page<Post> findByUserId(Long userId, Pageable pageable);

  Page<Post> findByPlaceId(Long placeId, Pageable pageable);

  // Custom query với sorting mặc định theo createdAt descending
  @Query(value = "{}", sort = "{ 'createdAt': -1 }")
  Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);

  @Query(value = "{ 'userId': ?0 }", sort = "{ 'createdAt': -1 }")
  Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Post> findByUserIdInAndCreatedAtAfter(
      List<Long> userIds, Instant createdAt, Pageable pageable);
}
