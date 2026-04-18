package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.SavePost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SavePostRepository extends MongoRepository<SavePost, String> {
  Page<SavePost> findByUserId(Long userId, Pageable pageable);

  boolean existsByUserIdAndPostId(Long userId, String postId);

  List<SavePost> findByUserIdAndPostIdIn(Long userId, List<String> postIds);

  Optional<SavePost> findByUserIdAndPostId(Long userId, String postId);

  void deleteAllByPostId(String postId);
}
