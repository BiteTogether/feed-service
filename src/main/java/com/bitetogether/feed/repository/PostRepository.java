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

  Page<Post> findByPlaceId(String placeId, Pageable pageable);

  // Custom query với sorting mặc định theo createdAt descending
  @Query(value = "{}", sort = "{ 'createdAt': -1 }")
  Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);

  @Query(value = "{ 'userId': ?0 }", sort = "{ 'createdAt': -1 }")
  Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Post> findByUserIdInAndCreatedAtAfter(
      List<Long> userIds, Instant createdAt, Pageable pageable);

  Page<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

  Page<Post> findByUserIdInAndCreatedAtAfterAndLatitudeBetweenAndLongitudeBetween(
      List<Long> userIds,
      Instant createdAt,
      Double minLatitude,
      Double maxLatitude,
      Double minLongitude,
      Double maxLongitude,
      Pageable pageable);

  @Query(
      "{ 'user_id': { $in: ?0 }, '_id': { $ne: ?1 }, 'latitude': { $gte: ?2, $lte: ?3 }, 'longitude': { $gte: ?4, $lte: ?5 } }")
  List<Post> findNearbyPostsByFriends(
      List<Long> userIds,
      String excludePostId,
      Double minLatitude,
      Double maxLatitude,
      Double minLongitude,
      Double maxLongitude);
}
