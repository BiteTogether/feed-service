package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LikeRepository extends MongoRepository<Like, String> {
  Page<Like> findByUserId(Long userId, Pageable pageable);

  Page<Like> findByPostId(String postId, Pageable pageable);

  Page<Like> findByCommentId(String commentId, Pageable pageable);

  Page<Like> findByUserIdAndPostId(Long userId, String postId, Pageable pageable);

  Page<Like> findByUserIdAndCommentId(Long userId, String commentId, Pageable pageable);
}
