package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
  Page<Comment> findByPostId(String postId, Pageable pageable);

  Page<Comment> findByUserId(Long userId, Pageable pageable);
}
